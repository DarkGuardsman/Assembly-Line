package fluidmech.client.render;

import hydraulic.api.ColorCode;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import fluidmech.client.model.ModelLargePipe;
import fluidmech.common.FluidMech;
import fluidmech.common.machines.pipes.TileEntityNetworkPipe;
import fluidmech.common.machines.pipes.TileEntityPipe;

public class RenderNetworkPipe extends TileEntitySpecialRenderer
{
    private ModelLargePipe SixPipe;
    private boolean[] renderSide = new boolean[6];

    public RenderNetworkPipe()
    {
        SixPipe = new ModelLargePipe();
    }

    public void renderAModelAt(TileEntity te, double d, double d1, double d2, float f)
    {
        // Texture file
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        
        int meta = 0;
        
        if (te instanceof TileEntityNetworkPipe)
        {
            meta = te.getBlockMetadata();
            this.renderSide = ((TileEntityNetworkPipe) te).renderConnection;
        }
        this.render(meta);
        GL11.glPopMatrix();

    }
    public static String getPipeTexture(int meta)
    {
        return FluidMech.MODEL_TEXTURE_DIRECTORY + "pipes/"+ColorCode.get(meta).getName()+"Pipe.png";
    }
    public void render(int meta)
    {
        bindTextureByName(this.getPipeTexture(meta));
        if (renderSide[0])
            SixPipe.renderBottom();
        if (renderSide[1])
            SixPipe.renderTop();
        if (renderSide[3])
            SixPipe.renderFront();
        if (renderSide[2])
            SixPipe.renderBack();
        if (renderSide[5])
            SixPipe.renderRight();
        if (renderSide[4])
            SixPipe.renderLeft();
        SixPipe.renderMiddle();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
    {
        this.renderAModelAt(tileEntity, var2, var4, var6, var8);
    }

}