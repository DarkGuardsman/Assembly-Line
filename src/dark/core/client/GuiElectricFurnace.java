package dark.core.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dark.core.common.DarkMain;
import dark.core.common.machines.ContainerElectricFurnace;
import dark.core.common.machines.TileEntityElectricFurnace;

@SideOnly(Side.CLIENT)
public class GuiElectricFurnace extends GuiContainer
{
    private static final ResourceLocation electricFurnaceTexture = new ResourceLocation(DarkMain.getInstance().DOMAIN,DarkMain.GUI_DIRECTORY+ "electric_furnace.png");

    private TileEntityElectricFurnace tileEntity;

    private int containerWidth;
    private int containerHeight;

    public GuiElectricFurnace(InventoryPlayer par1InventoryPlayer, TileEntityElectricFurnace tileEntity)
    {
        super(new ContainerElectricFurnace(par1InventoryPlayer, tileEntity));
        this.tileEntity = tileEntity;
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        this.fontRenderer.drawString(this.tileEntity.getInvName(), 45, 6, 4210752);
        this.fontRenderer.drawString("Smelting:", 10, 28, 4210752);
        this.fontRenderer.drawString("Battery:", 10, 53, 4210752);
        String displayText = "";

        if (this.tileEntity.processTicks > 0)
        {
            displayText = "Smelting";
        }
        else
        {
            displayText = "Idle";
        }

        this.fontRenderer.drawString("Status: " + displayText, 82, 45, 4210752);
        this.fontRenderer.drawString(ElectricityDisplay.getDisplay(tileEntity.WATTS_PER_TICK * 20, ElectricUnit.WATT), 82, 56, 4210752);
        this.fontRenderer.drawString("Voltage: " + (int) (this.tileEntity.getVoltage() * 1000), 82, 68, 4210752);
        this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        this.mc.renderEngine.func_110577_a(electricFurnaceTexture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        containerWidth = (this.width - this.xSize) / 2;
        containerHeight = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);

        if (this.tileEntity.processTicks > 0)
        {
            int scale = (int) (((double) this.tileEntity.processTicks / (double) TileEntityElectricFurnace.PROCESS_TIME_REQUIRED) * 23);
            this.drawTexturedModalRect(containerWidth + 77, containerHeight + 24, 176, 0, 23 - scale, 20);
        }
    }
}