package corgitaco.betterweather.client.entity.tornado.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class TornadoModel extends EntityModel<Entity> {
	private final ModelRenderer bb_main;

	public TornadoModel() {
		textureWidth = 1024;
		textureHeight = 1024;

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.setTextureOffset(0, 56).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 28).addBox(-8.0F, -20.0F, -8.0F, 16.0F, 12.0F, 16.0F, 0.0F, false);
		bb_main.setTextureOffset(388, 404).addBox(-14.0F, -47.0F, -14.0F, 28.0F, 27.0F, 28.0F, 0.0F, false);
		bb_main.setTextureOffset(212, 404).addBox(-22.0F, -91.0F, -22.0F, 44.0F, 44.0F, 44.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 300).addBox(-32.0F, -142.0F, -32.0F, 64.0F, 51.0F, 64.0F, 0.0F, false);
		bb_main.setTextureOffset(220, 220).addBox(-40.0F, -182.0F, -40.0F, 80.0F, 40.0F, 80.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(float x, float y, float z) {
		this.bb_main.rotateAngleY = y;
	}
}