package com.builtbroken.assemblyline.content.belt;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.content.belt.pipe.BeltType;
import com.builtbroken.assemblyline.content.belt.pipe.PipeInventory;
import com.builtbroken.assemblyline.content.belt.pipe.data.BeltSlotState;
import com.builtbroken.assemblyline.content.belt.pipe.data.BeltState;
import com.builtbroken.assemblyline.content.belt.pipe.gui.ContainerPipeBelt;
import com.builtbroken.assemblyline.content.belt.pipe.gui.GuiPipeBelt;
import com.builtbroken.jlib.type.Pair;
import com.builtbroken.mc.api.data.IPacket;
import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.api.tile.access.IGuiTile;
import com.builtbroken.mc.api.tile.provider.IInventoryProvider;
import com.builtbroken.mc.codegen.annotations.TileWrapped;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.framework.block.imp.IWrenchListener;
import com.builtbroken.mc.framework.logic.TileNode;
import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.lib.helper.BlockUtility;
import com.builtbroken.mc.prefab.inventory.BasicInventory;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 11/14/2017.
 */
@TileWrapped(className = ".gen.TileEntityWrappedPipeBelt", wrappers = "ExternalInventory")
public class TilePipeBelt extends TileNode implements IRotatable, IInventoryProvider<PipeInventory>, IGuiTile, IWrenchListener
{
    public static final int PACKET_INVENTORY = 1;
    public static final int PACKET_GUI_BUTTON = 2;

    public static final int BUTTON_ITEM_PULL = 0;
    public static final int BUTTON_RENDER_TOP = 1;
    public static final int BUTTON_ITEM_EJECT = 2;

    //TODO fixed sided slots for inventory
    /** Cached state map of direction to input sides & slots */
    public static BeltSlotState[][][] inputStates;
    /** Cached state map of direction to output sides & slots */
    public static BeltSlotState[][][] outputStates;

    public static int[] centerSlots = new int[]{2};

    //Main inventory
    private PipeInventory inventory;

    //Cached facing direction
    private ForgeDirection _direction;

    //Type of belt
    public BeltType type = BeltType.NORMAL;

    //Belt states, used for filters and inverting belt directions
    private BeltState[] beltStates = new BeltState[4];

    //Send inventory update to client
    private boolean sendInvToClient = true;
    /** Should pipe suck items out of machines from connected inputs */
    public boolean pullItems = false;
    /** Should outputs dump items on the ground if no connections */
    public boolean shouldEjectItems = false;
    /** Should pipe render with its cage top */
    public boolean renderTop = true;
    /** Should the pipe trigger a re-render */
    public boolean shouldUpdateRender = false;

    public BasicInventory renderInventory;

    public TilePipeBelt()
    {
        super("pipeBelt", AssemblyLine.DOMAIN);
    }

    @Override
    public void update(long ticks)
    {
        super.update(ticks);
        if (isServer())
        {
            if (ticks % 20 == 0)
            {
                //Inventory movement
                //1. OUTPUT:                    Push items from output to tiles
                //2. PUSH CENTER -> OUTPUTS:    Move center slots to outputs (sorting)
                //3. PUSH INPUTS -> CENTER:     Move inputs to center (round-robin / priority queue)
                //4. INPUT:                     Pull items into inputs from tiles

                exportItems();
                centerToOutput();
                inputToCenter();
                importItems();
            }

            if (sendInvToClient) //TODO add settings to control packet update times
            {
                sendInvToClient = false;
                sendInventoryPacket();
            }

            if (shouldUpdateRender)
            {
                sendDescPacket();
                shouldUpdateRender = false;
            }
        }
        else
        {
            if (shouldUpdateRender)
            {
                world().unwrap().markBlockRangeForRenderUpdate(xi(), yi(), zi(), xi(), yi(), zi());
                shouldUpdateRender = false;
            }
        }
    }

    /**
     * Pushes output slot items either
     * to connections or into the world
     * if eject is enabled.
     */
    protected void exportItems()
    {
        //Push outputs to next belt or machine
        BeltSlotState[] states = getOutputs();
        if (states != null)
        {
            for (BeltSlotState slotState : states)
            {
                if (slotState != null)
                {
                    //Get output position
                    final Location outputLocation = toLocation().add(slotState.side);

                    //Get stack in slot
                    ItemStack stack = getInventory().getStackInSlot(slotState.slotID);
                    if (stack != null)
                    {
                        //Drop
                        if (outputLocation.isAirBlock())
                        {
                            if (shouldEjectItems) //TODO allow each connection to be customized
                            {
                                final Location ejectPosition = toLocation().add(slotState.side, 0.6f);
                                EntityItem entityItem = InventoryUtility.dropItemStack(ejectPosition, stack);
                                if (entityItem != null)
                                {
                                    //Clear item
                                    stack = null;

                                    //Add some speed just for animation reasons
                                    entityItem.motionX = slotState.side.offsetX * 0.1f;
                                    entityItem.motionY = slotState.side.offsetY * 0.1f;
                                    entityItem.motionZ = slotState.side.offsetZ * 0.1f;
                                }
                            }
                        }
                        //Push into tile
                        else
                        {
                            stack = InventoryUtility.insertStack(outputLocation, stack, slotState.side.getOpposite().ordinal(), false);
                        }
                        getInventory().setInventorySlotContents(slotState.slotID, stack);
                    }
                }
            }
        }
    }

    /**
     * Pushes center slots to output slots.
     * Does sorting if enabled to ensure items get
     * to correct output slots.
     */
    protected void centerToOutput()
    {
        int[] centerSlots = getCenterSlots();
        BeltSlotState[] states = getOutputs();

        //Center to output
        if (states != null)
        {
            for (BeltSlotState slotState : states)
            {
                if (slotState != null)
                {
                    if (getInventory().getStackInSlot(slotState.slotID) == null)
                    {
                        for (int centerSlot : centerSlots)
                        {
                            ItemStack stackToMove = getInventory().getStackInSlot(centerSlot);
                            if (stackToMove != null) //TODO apply filter for sorting
                            {
                                getInventory().setInventorySlotContents(slotState.slotID, stackToMove);
                                getInventory().setInventorySlotContents(centerSlot, null);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Pushes input slots to center. Does round robin
     * logic and sorting to ensure correct order of
     * inputs or prevent jamming if possible.
     */
    protected void inputToCenter()
    {
        //TODO round robin to ensure each belt has a chance to move to center
        //TODO store inputs as list object with current index
        //TODO only move index forward if we moved an item
        //Index will be used to note starting point for loop
        //Example: 0 1 2 (moved item) next tick > 1 2 0 (moved item) > 2 0 1

        BeltSlotState[] states = getInputs();
        if (states != null)
        {
            for (BeltSlotState slotState : states)
            {
                if (slotState != null)
                {
                    ItemStack stackToMove = getInventory().getStackInSlot(slotState.slotID);
                    if (stackToMove != null)
                    {
                        for (int centerSlot : centerSlots)
                        {
                            if (getInventory().getStackInSlot(centerSlot) == null)
                            {
                                getInventory().setInventorySlotContents(centerSlot, stackToMove);
                                getInventory().setInventorySlotContents(slotState.slotID, null);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Pulls items from input connections into the pipe
     */
    protected void importItems()
    {
        BeltSlotState[] states = getInputs();
        if (states != null && pullItems)
        {
            for (BeltSlotState slotState : states)
            {
                if (slotState != null)
                {
                    ItemStack currentItem = getInventory().getStackInSlot(slotState.slotID);
                    if (currentItem == null)
                    {
                        //Get inventory
                        IInventory tileInv = null;
                        TileEntity tile = toLocation().add(slotState.side).getTileEntity();
                        if (tile instanceof IInventory)
                        {
                            tileInv = (IInventory) tile;
                        }

                        //Has inventory try to find item
                        if (tileInv != null)
                        {
                            BeltState beltState = getStateForSide(slotState.side);
                            Pair<ItemStack, Integer> slotData = InventoryUtility.findFirstItemInInventory(tileInv,
                                    slotState.side.getOpposite().ordinal(), getItemsToPullPerCycle(), beltState != null ? beltState.filter : null);
                            if (slotData != null)
                            {
                                ItemStack inputStack = tileInv.decrStackSize(slotData.right(), slotData.left().stackSize);
                                getInventory().setInventorySlotContents(slotState.slotID, inputStack);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onPlayerRightClickWrench(EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (isServer())
        {
            if (player.isSneaking())
            {
                setDirection(getDirection().getOpposite());
            }
            else
            {
                switch (getDirection())
                {
                    case NORTH:
                        setDirection(ForgeDirection.EAST);
                        break;
                    case EAST:
                        setDirection(ForgeDirection.SOUTH);
                        break;
                    case SOUTH:
                        setDirection(ForgeDirection.WEST);
                        break;
                    case WEST:
                    default:
                        setDirection(ForgeDirection.NORTH);
                        break;
                }
            }
        }
        return true;
    }

    public int getItemsToPullPerCycle()
    {
        return 1;
    }

    public BeltState getStateForSide(ForgeDirection direction)
    {
        return beltStates[BlockUtility.directionToRotation(direction)];
    }

    @Override
    public ForgeDirection getDirection()
    {
        if (_direction == null)
        {
            _direction = ForgeDirection.getOrientation(world().unwrap().getBlockMetadata(xi(), yi(), zi()));
        }
        return _direction;
    }

    @Override
    public void setDirection(ForgeDirection direction)
    {
        if (direction != null && direction != getDirection()
                && direction.ordinal() >= 2 && direction.ordinal() < 6)
        {
            _direction = null;
            getHost().setMetaValue(direction.ordinal());
        }
    }

    @Override
    public PipeInventory getInventory()
    {
        if (inventory == null)
        {
            inventory = new PipeInventory(this);
        }
        return inventory;
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
    {
        BeltSlotState[] states = getInputs();
        if (states != null)
        {
            for (BeltSlotState state : states)
            {
                if (state != null && slot == state.slotID)
                {
                    return state.side == side;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canRemove(ItemStack stack, int slot, ForgeDirection side)
    {
        BeltSlotState[] states = getOutputs();
        if (states != null)
        {
            for (BeltSlotState state : states)
            {
                if (state != null && slot == state.slotID)
                {
                    return state.side == side;
                }
            }
        }
        return false;
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        sendInvToClient = true;
    }

    public BeltSlotState[] getInputs()
    {
        if (type != BeltType.T_SECTION && type != BeltType.INTERSECTION)
        {
            return inputStates[type.ordinal()][getDirection().ordinal()];
        }
        return null;
    }

    public BeltSlotState[] getOutputs()
    {
        if (type != BeltType.T_SECTION && type != BeltType.INTERSECTION)
        {
            return outputStates[type.ordinal()][getDirection().ordinal()];
        }
        return null;
    }

    public int[] getCenterSlots()
    {
        return centerSlots;
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        type = BeltType.values()[buf.readInt()];
        shouldUpdateRender = buf.readBoolean();
        shouldEjectItems = buf.readBoolean();
        pullItems = buf.readBoolean();
        renderTop = buf.readBoolean();
        readInvPacket(buf);
    }


    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(type.ordinal());
        buf.writeBoolean(shouldUpdateRender);
        buf.writeBoolean(shouldEjectItems);
        buf.writeBoolean(pullItems);
        buf.writeBoolean(renderTop);
        writeInvPacket(buf);
    }

    @Override
    protected void writeGuiPacket(EntityPlayer player, ByteBuf buf)
    {
        buf.writeBoolean(shouldEjectItems);
        buf.writeBoolean(pullItems);
        buf.writeBoolean(renderTop);
    }

    @Override
    protected void readGuiPacket(EntityPlayer player, ByteBuf buf)
    {
        shouldEjectItems = buf.readBoolean();
        pullItems = buf.readBoolean();
        renderTop = buf.readBoolean();
    }

    public void readInvPacket(ByteBuf buf)
    {
        int size = buf.readInt();
        if (renderInventory == null || renderInventory.getSizeInventory() != size)
        {
            renderInventory = new BasicInventory(size);
        }
        renderInventory.load(ByteBufUtils.readTag(buf));
    }

    public void writeInvPacket(ByteBuf buf)
    {
        buf.writeInt(getInventory().getSizeInventory());
        ByteBufUtils.writeTag(buf, getInventory().save(new NBTTagCompound()));
    }

    public void sendInventoryPacket()
    {
        IPacket packet = getHost().getPacketForData(PACKET_INVENTORY);
        writeInvPacket(packet.data());
        getHost().sendPacketToClient(packet, 64);
    }

    public void sendButtonEvent(int id, boolean checked)
    {
        IPacket packet = getHost().getPacketForData(PACKET_GUI_BUTTON);
        packet.data().writeInt(id);
        packet.data().writeBoolean(checked);
        getHost().sendPacketToServer(packet);
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        if (nbt.hasKey("inventory"))
        {
            getInventory().load(nbt.getCompoundTag("inventory"));
        }
        pullItems = nbt.getBoolean("pullItems");
        shouldEjectItems = nbt.getBoolean("ejectItems");
        renderTop = nbt.getBoolean("renderTubeTop");
        type = BeltType.get(nbt.getInteger("beltType"));
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        super.save(nbt);
        if (!getInventory().isEmpty())
        {
            NBTTagCompound invSave = new NBTTagCompound();
            getInventory().save(invSave);
            nbt.setTag("inventory", invSave);
        }
        nbt.setBoolean("pullItems", pullItems);
        nbt.setBoolean("ejectItems", shouldEjectItems);
        nbt.setBoolean("renderTubeTop", renderTop);
        nbt.setInteger("beltType", type != null ? type.ordinal() : 0);
        return nbt;
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        if (!super.read(buf, id, player, type))
        {
            if (id == PACKET_INVENTORY)
            {
                readInvPacket(buf);
                return true;
            }
            else if (id == PACKET_GUI_BUTTON)
            {
                int buttonID = buf.readInt();
                boolean enabled = buf.readBoolean();

                if (buttonID == BUTTON_ITEM_PULL)
                {
                    this.pullItems = enabled;
                }
                else if (buttonID == BUTTON_RENDER_TOP)
                {
                    this.renderTop = enabled;
                    this.shouldUpdateRender = true;
                }
                else if (buttonID == BUTTON_ITEM_EJECT)
                {
                    this.shouldEjectItems = enabled;
                }
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerPipeBelt(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiPipeBelt(player, this);
    }

    static
    {
        inputStates = new BeltSlotState[3][6][];
        outputStates = new BeltSlotState[3][6][];
        ForgeDirection[] rotations = new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};
        for (ForgeDirection direction : rotations)
        {
            inputStates[BeltType.NORMAL.ordinal()][direction.ordinal()] = new BeltSlotState[]{new BeltSlotState(0, direction)};
            outputStates[BeltType.NORMAL.ordinal()][direction.ordinal()] = new BeltSlotState[]{new BeltSlotState(1, direction.getOpposite())};

            ForgeDirection turn;
            switch (direction)
            {
                case NORTH:
                    turn = ForgeDirection.EAST;
                    break;
                case SOUTH:
                    turn = ForgeDirection.WEST;
                    break;
                case EAST:
                    turn = ForgeDirection.SOUTH;
                    break;
                default:
                    turn = ForgeDirection.NORTH;
                    break;
            }

            outputStates[BeltType.LEFT_ELBOW.ordinal()][direction.ordinal()] = new BeltSlotState[]{new BeltSlotState(0, direction.getOpposite())};
            inputStates[BeltType.LEFT_ELBOW.ordinal()][direction.ordinal()] = new BeltSlotState[]{new BeltSlotState(1, turn)};

            outputStates[BeltType.RIGHT_ELBOW.ordinal()][direction.ordinal()] = new BeltSlotState[]{new BeltSlotState(0, direction.getOpposite())};
            inputStates[BeltType.RIGHT_ELBOW.ordinal()][direction.ordinal()] = new BeltSlotState[]{new BeltSlotState(1, turn)};
        }
    }
}