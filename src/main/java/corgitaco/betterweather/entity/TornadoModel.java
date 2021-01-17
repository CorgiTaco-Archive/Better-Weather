package corgitaco.betterweather.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class TornadoModel<T extends Entity> extends SegmentedModel<T> {

    private final ModelRenderer[] tornadoObjects;

    private final ImmutableList<ModelRenderer> modelRenderers;

    public TornadoModel() {
        this.tornadoObjects = new ModelRenderer[7680];

        for (int i = 0; i < this.tornadoObjects.length; ++i) {
            this.tornadoObjects[i] = new ModelRenderer(this, 0, 16);
            this.tornadoObjects[i].addBox(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F, 5, 5F, 5F);
        }

        ImmutableList.Builder<ModelRenderer> builder = ImmutableList.builder();
        builder.addAll(Arrays.asList(this.tornadoObjects));

        modelRenderers = builder.build();
    }


    @Override
    public Iterable<ModelRenderer> getParts() {
        return this.modelRenderers;
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float spinSpeed = ((float) Math.PI / 4F) + ageInTicks * (float) Math.PI * 0.06F;

        for (int multiplier = 1; multiplier <= 30; multiplier++) {
            int multipliedIDX = 256 * multiplier;
            float yHeight = -655 + (multiplier * 20);
            float xzDistance = 260 - (multiplier * 7);

            for (int idx = multipliedIDX - 256; idx < multipliedIDX; ++idx) {
                this.tornadoObjects[idx].rotationPointY = yHeight + MathHelper.cos(((idx * 1.5F) + ageInTicks) * 0.25F);
                this.tornadoObjects[idx].rotationPointX = MathHelper.cos(spinSpeed) * xzDistance;
                this.tornadoObjects[idx].rotationPointZ = MathHelper.sin(spinSpeed) * xzDistance;
                ++spinSpeed;
            }
        }
    }
}
