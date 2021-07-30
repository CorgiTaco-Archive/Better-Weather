package corgitaco.betterweather.client.entity.tornado;

import com.mojang.blaze3d.matrix.MatrixStack;
import corgitaco.betterweather.client.entity.tornado.model.TornadoModel;
import corgitaco.betterweather.entity.TornadoEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;

public class TornadoRenderer extends EntityRenderer<TornadoEntity> {

    private final TornadoModel tornadoModel = new TornadoModel();

    public TornadoRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(TornadoEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();
        matrixStackIn.scale(5, 5, 5);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180.0F));
        matrixStackIn.rotate(Vector3f.YN.rotationDegrees(entityIn.getRotation()));
        matrixStackIn.translate(0, -1.5, 0);
        this.tornadoModel.render(matrixStackIn, bufferIn.getBuffer(this.tornadoModel.getRenderType(getEntityTexture(entityIn))), packedLightIn, packedLightIn, 1, 1, 1, 1);
        matrixStackIn.pop();

        int size = entityIn.getCapturedStates().size();
        List<TornadoEntity.StateRotatable> capturedStates = entityIn.getCapturedStates();
        matrixStackIn.scale(1, 1, 1);

        for (int i = 0; i < capturedStates.size(); i++) {
            matrixStackIn.push();

            TornadoEntity.StateRotatable stateRotatable = capturedStates.get(i);
            BlockState blockstate = stateRotatable.getState();
            matrixStackIn.rotate(Vector3f.YN.rotationDegrees(stateRotatable.getRotationDegrees()));

            matrixStackIn.translate(stateRotatable.getXOffset(), stateRotatable.getYOffset(), stateRotatable.getZOffset());
            Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(blockstate, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY);
            matrixStackIn.pop();

        }

    }

    @Override
    public ResourceLocation getEntityTexture(TornadoEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}
