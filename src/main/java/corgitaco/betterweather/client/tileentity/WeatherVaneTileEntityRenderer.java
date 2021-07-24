package corgitaco.betterweather.client.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.blockentity.WeatherVaneTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;

public class WeatherVaneTileEntityRenderer extends TileEntityRenderer<WeatherVaneTileEntity> {

    private final WeatherVaneModel model = new WeatherVaneModel();

    public WeatherVaneTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(WeatherVaneTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        matrixStackIn.translate(0.5, -0.5, 0.5);
        IVertexBuilder buffer = bufferIn.getBuffer(this.model.getRenderType(new ResourceLocation(BetterWeather.MOD_ID, "entity/weathervane/copper_weather_vane.png")));
        model.render(matrixStackIn, buffer, combinedLightIn, combinedOverlayIn, 1, 1, 1, 1);
        matrixStackIn.pop();

    }
}
