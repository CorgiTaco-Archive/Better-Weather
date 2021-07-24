package corgitaco.betterweather.client.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import corgitaco.betterweather.block.weathervane.WeatherVane;
import corgitaco.betterweather.blockentity.WeatherVaneBlockEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class WeatherVaneBlockEntityRenderer extends TileEntityRenderer<WeatherVaneBlockEntity> {

    private final WeatherVaneModel model = new WeatherVaneModel();

    public WeatherVaneBlockEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(WeatherVaneBlockEntity weatherVane, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        matrixStackIn.translate(0.5, -0.5, 0.5);
        WeatherVane block = (WeatherVane) weatherVane.getBlockState().getBlock();
        IVertexBuilder buffer = bufferIn.getBuffer(this.model.getRenderType(block.getWeatherVaneType().getTextureLocation()));
        model.render(matrixStackIn, buffer, combinedLightIn, combinedOverlayIn, 1, 1, 1, 1);
        model.setRotationAngle(0, weatherVane.getRotation(), 0);
        matrixStackIn.pop();
    }
}
