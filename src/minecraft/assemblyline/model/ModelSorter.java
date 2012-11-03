// Date: 9/8/2012 7:10:46 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package assemblyline.model;

import net.minecraft.src.ModelBase;
import net.minecraft.src.ModelRenderer;

public class ModelSorter extends ModelBase
{
	// fields
	ModelRenderer Base;
	ModelRenderer Case;
	ModelRenderer Piston;
	ModelRenderer H1;
	ModelRenderer H2;
	ModelRenderer Top;
	ModelRenderer Case2;
	ModelRenderer H3;
	ModelRenderer PistonShaft;
	ModelRenderer PistonFace;

	public ModelSorter()
	{
		textureWidth = 128;
		textureHeight = 128;

		Base = new ModelRenderer(this, 64, 0);
		Base.addBox(-8F, 0F, -8F, 16, 4, 16);
		Base.setRotationPoint(0F, 20F, 0F);
		Base.setTextureSize(64, 32);
		Base.mirror = true;
		setRotation(Base, 0F, 0F, 0F);
		Case = new ModelRenderer(this, 0, 38);
		Case.addBox(-2F, 0F, 0F, 4, 8, 13);
		Case.setRotationPoint(-5F, 12F, -6F);
		Case.setTextureSize(64, 32);
		Case.mirror = true;
		setRotation(Case, 0F, 0F, 0F);
		Piston = new ModelRenderer(this, 0, 22);
		Piston.addBox(-2F, 0F, 0F, 4, 4, 10);
		Piston.setRotationPoint(0F, 15F, -5F);
		Piston.setTextureSize(64, 32);
		Piston.mirror = true;
		setRotation(Piston, 0F, 0F, 0F);
		H1 = new ModelRenderer(this, 29, 23);
		H1.addBox(-2F, 0F, 0F, 2, 1, 8);
		H1.setRotationPoint(1F, 19F, -4F);
		H1.setTextureSize(64, 32);
		H1.mirror = true;
		setRotation(H1, 0F, 0F, 0F);
		H2 = new ModelRenderer(this, 54, 23);
		H2.addBox(-2F, 0F, 0F, 1, 2, 8);
		H2.setRotationPoint(-1F, 16F, -4F);
		H2.setTextureSize(64, 32);
		H2.mirror = true;
		setRotation(H2, 0F, 0F, 0F);
		Top = new ModelRenderer(this, 0, 0);
		Top.addBox(-8F, 0F, -8F, 16, 4, 16);
		Top.setRotationPoint(0F, 8F, 0F);
		Top.setTextureSize(64, 32);
		Top.mirror = true;
		setRotation(Top, 0F, 0F, 0F);
		Case2 = new ModelRenderer(this, 0, 38);
		Case2.addBox(-2F, 0F, 0F, 4, 8, 13);
		Case2.setRotationPoint(5F, 12F, -6F);
		Case2.setTextureSize(64, 32);
		Case2.mirror = true;
		setRotation(Case2, 0F, 0F, 0F);
		H3 = new ModelRenderer(this, 54, 23);
		H3.addBox(-2F, 0F, 0F, 1, 2, 8);
		H3.setRotationPoint(4F, 16F, -4F);
		H3.setTextureSize(64, 32);
		H3.mirror = true;
		setRotation(H3, 0F, 0F, 0F);
		PistonShaft = new ModelRenderer(this, 0, 67);
		PistonShaft.addBox(-1F, -1F, 0F, 2, 2, 10);
		PistonShaft.setRotationPoint(0F, 17F, -6F);
		PistonShaft.setTextureSize(64, 32);
		PistonShaft.mirror = true;
		setRotation(PistonShaft, 0F, 0F, 0F);
		PistonFace = new ModelRenderer(this, 0, 62);
		PistonFace.addBox(-3F, -1F, -1F, 6, 2, 1);
		PistonFace.setRotationPoint(0F, 17F, -6F);
		PistonFace.setTextureSize(64, 32);
		PistonFace.mirror = true;
		setRotation(PistonFace, 0F, 0F, 0F);
	}

	public void renderMain(float f5)
	{
		Base.render(f5);
		Case.render(f5);
		H1.render(f5);
		H2.render(f5);
		Top.render(f5);
		Case2.render(f5);
		H3.render(f5);
	}

	public void renderPiston(float f5, int pos)
	{
		Piston.render(f5);
		PistonShaft.setRotationPoint(0F, 17F, -6F - pos);
		PistonFace.setRotationPoint(0F, 17F, -6F - pos);
		PistonShaft.render(f5);
		PistonFace.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}