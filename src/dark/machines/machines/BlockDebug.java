package dark.machines.machines;

import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.IndustryTabs;
import com.builtbroken.minecraft.prefab.BlockMachine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockDebug extends BlockMachine
{
    public static float DebugWattOut, DebugWattDemand;

    public BlockDebug()
    {
        super(AssemblyLine.CONFIGURATION, "DebugBlock", Material.clay);
        this.setCreativeTab(IndustryTabs.tabIndustrial());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconReg)
    {
        super.registerIcons(iconReg);
        for (DebugBlocks block : DebugBlocks.values())
        {
            if (block.enabled)
            {
                block.icon = iconReg.registerIcon(AssemblyLine.PREFIX + block.getTextureName());
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        if (meta < DebugBlocks.values().length)
        {
            return DebugBlocks.values()[meta].icon;
        }
        return this.blockIcon;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        if (metadata < DebugBlocks.values().length)
        {
            try
            {
                return DebugBlocks.values()[metadata].clazz.newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return super.createTileEntity(world, metadata);
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return null;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        super.onBlockAdded(world, x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        if (meta >= DebugBlocks.values().length || !DebugBlocks.values()[meta].enabled)
        {
            world.setBlock(x, y, z, 0);
        }
    }

    @Override
    public void getSubBlocks(int blockID, CreativeTabs tab, List creativeTabList)
    {
        for (DebugBlocks block : DebugBlocks.values())
        {
            if (block.enabled)
            {
                creativeTabList.add(new ItemStack(blockID, 1, block.ordinal()));
            }
        }
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        for (DebugBlocks block : DebugBlocks.values())
        {
            if (block.enabled && block.clazz != null && block.name != null)
            {
                list.add(new Pair<String, Class<? extends TileEntity>>(block.name, block.clazz));
            }
        }
    }

    @Override
    public void loadExtraConfigs(Configuration config)
    {
        super.loadExtraConfigs(config);
        for (DebugBlocks block : DebugBlocks.values())
        {
            block.enabled = config.get("Blocks", "Enable" + block.name + "Block", true).getBoolean(true);
        }

    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    @Override
    public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    @Override
    public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        return this.onUseWrench(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
    }

    public static enum DebugBlocks
    {
        SOURCE("UnlimitedPower", TileEntityInfSupply.class, "infSource"),
        FLUID("UnlimitedFluid", TileEntityInfFluid.class, "infFluid"),
        VOID("FluidVoid", TileEntityVoid.class, "void"),
        LOAD("PowerVampire", TileEntityInfLoad.class, "infLoad");
        public Icon icon;
        public String name;
        public String texture;
        public boolean enabled;
        Class<? extends TileEntity> clazz;

        private DebugBlocks(String name, Class<? extends TileEntity> clazz)
        {
            this.name = name;
            this.clazz = clazz;
        }

        private DebugBlocks(String name, Class<? extends TileEntity> clazz, String texture)
        {
            this(name, clazz);
            this.texture = texture;
        }

        public String getTextureName()
        {
            if (texture == null || texture.isEmpty())
            {
                return name;
            }
            return texture;
        }

    }

}
