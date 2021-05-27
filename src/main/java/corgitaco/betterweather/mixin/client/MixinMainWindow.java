package corgitaco.betterweather.mixin.client;

import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.IWindowEventListener;
import net.minecraft.client.renderer.MonitorHandler;
import net.minecraft.client.renderer.ScreenSize;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(MainWindow.class)
public abstract class MixinMainWindow {

    @Inject(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL;createCapabilities()Lorg/lwjgl/opengl/GLCapabilities;", shift = At.Shift.AFTER), method = "<init>")
    public void init(IWindowEventListener mc, MonitorHandler monitorHandler, ScreenSize size, String videoModeName, String titleIn, CallbackInfo ci) {
        GL.getCapabilities(); // todo: check if driver has the correct extensions.
    }
}
