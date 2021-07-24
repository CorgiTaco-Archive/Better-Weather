package corgitaco.betterweather.client.tileentity;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class WeatherVaneModel extends Model {
    private final ModelRenderer rotational;
    private final ModelRenderer bb_main;

    public WeatherVaneModel() {
        super(RenderType::getEntityCutoutNoCull);
        textureWidth = 32;
        textureHeight = 32;

        rotational = new ModelRenderer(this);
        rotational.setRotationPoint(0.0F, 23.0F, 0.0F);
        rotational.setTextureOffset(0, 2).addBox(0.0F, -15.0F, -7.5F, 0.0F, 15.0F, 15.0F, 0.0F, false);
        rotational.setTextureOffset(0, 24).addBox(-7.5F, -8.0F, 0.0F, 15.0F, 1.0F, 0.0F, 0.0F, true);

        bb_main = new ModelRenderer(this);
        bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
        bb_main.setTextureOffset(4, 5).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
        bb_main.setTextureOffset(6, 0).addBox(-2.5F, -15.5F, -2.5F, 5.0F, 0.0F, 5.0F, 0.0F, false);
        bb_main.setTextureOffset(0, 0).addBox(-0.5F, -16.0F, -0.5F, 1.0F, 15.0F, 1.0F, 0.0F, false);
    }


    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        rotational.render(matrixStack, buffer, packedLight, packedOverlay);
        bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        this.rotational.rotateAngleY = 180;
    }
}