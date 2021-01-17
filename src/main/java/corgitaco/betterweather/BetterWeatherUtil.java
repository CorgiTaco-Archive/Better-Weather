package corgitaco.betterweather;

import corgitaco.betterweather.helper.ViewFrustumGetter;
import corgitaco.betterweather.helper.WeatherViewFrustum;
import corgitaco.betterweather.season.Season;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.potion.Effects;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

public class BetterWeatherUtil {

    public static final Color DEFAULT_RAIN_SKY = new Color(103, 114, 136);
    public static final Color DEFAULT_RAIN_FOG = new Color(89, 100, 142);
    public static final Color DEFAULT_RAIN_CLOUDS = new Color(158, 158, 158);

    public static final Color DEFAULT_THUNDER_SKY = new Color(42, 45, 51);
    public static final Color DEFAULT_THUNDER_FOG = new Color(85, 95, 135);
    public static final Color DEFAULT_THUNDER_CLOUDS = new Color(37, 37, 37);

    public static int parseHexColor(String targetHexColor) {
        if (!targetHexColor.isEmpty()) {
            try {
                return (int) Long.parseLong(targetHexColor.replace("#", "").replace("0x", ""), 16);
            } catch (Exception e) {
                BetterWeather.LOGGER.error("\"" + targetHexColor + "\" was not a hex color value! | Using Defaults...");
            }
        }
        return Integer.MAX_VALUE;
    }

    public static boolean filterRegistryID(ResourceLocation id, Registry<?> registry, String registryTypeName) {
        if (registry.keySet().contains(id))
            return true;
        else {
            BetterWeather.LOGGER.error("\"" + id.toString() + "\" was not a registryID in the " + registryTypeName + "! Skipping entry...");
            return false;
        }
    }

    public static int transformRainOrThunderTimeToCurrentSeason(int rainOrThunderTime, Season.SubSeason previous, Season.SubSeason current) {
        double previousMultiplier = previous.getWeatherEventChanceMultiplier();
        double currentMultiplier = current.getWeatherEventChanceMultiplier();
        double normalTime = rainOrThunderTime * previousMultiplier;

        return (int) (normalTime * 1 / currentMultiplier);
    }

    private static int configReloadIdx = 0;

    @OnlyIn(Dist.CLIENT)
    public static void configReloadKeybind(int key) {
        Minecraft minecraft = Minecraft.getInstance();

        if (InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), 293) && key == 82) {
            if (configReloadIdx == 0) {
                BetterWeather.loadClientConfigs();
                minecraft.worldRenderer.loadRenderers();
                Season.SubSeason.SeasonClient.stopSpamIDXFoliage = 0;
                Season.SubSeason.SeasonClient.stopSpamIDXGrass = 0;
                Season.SubSeason.SeasonClient.stopSpamIDXSky = 0;
                Season.SubSeason.SeasonClient.stopSpamIDXFog = 0;
                printDebugMessage("bw.debug.reloadconfig.message");
                configReloadIdx = 1;
            }
        }
        else
            configReloadIdx = 0;
    }

    @OnlyIn(Dist.CLIENT)
    private static void printDebugWarning(String message, Object... args) {
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage((new StringTextComponent("")).append((new TranslationTextComponent("debug.prefix")).mergeStyle(TextFormatting.RED, TextFormatting.BOLD)).appendString(" ").append(new TranslationTextComponent(message, args)));
    }

    @OnlyIn(Dist.CLIENT)
    public static void printDebugMessage(String message, Object... args) {
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage((new StringTextComponent("")).append((new TranslationTextComponent("debug.prefix")).mergeStyle(new TextFormatting[]{TextFormatting.YELLOW, TextFormatting.BOLD})).appendString(" ").append(new TranslationTextComponent(message, args)));
    }

    public static int modifiedColorValue(int original, int target, double blendStrength) {
        return (int) MathHelper.lerp(blendStrength, original, target);
    }

    public static Color blendColor(Color original, Color target, double blendStrength) {
        int modifiedRed = modifiedColorValue(original.getRed(), target.getRed(), blendStrength);
        int modifiedGreen = modifiedColorValue(original.getGreen(), target.getGreen(), blendStrength);
        int modifiedBlue = modifiedColorValue(original.getBlue(), target.getBlue(), blendStrength);
        return new Color(modifiedRed, modifiedGreen, modifiedBlue);
    }

    public static Color transformFloatColor(Vector3d floatColor) {
        return new Color((int) (floatColor.getX() * 255), (int) (floatColor.getY() * 255), (int) (floatColor.getZ() * 255));
    }

    public static boolean updateLightmap(float partialTicks, float torchFlicker, Minecraft client, GameRenderer entityRenderer, NativeImage nativeImage, DynamicTexture dynamicTexture) {
        client.getProfiler().startSection("lightTex");
        ClientWorld clientworld = client.world;
        if (clientworld != null) {
            float sunBrightness = clientworld.getSunBrightness(1.0F);
            float worldBrightness;
            if (clientworld.getTimeLightningFlash() > 0) {
                worldBrightness = 1.0F;
            } else {
                worldBrightness = sunBrightness * 0.95F + 0.05F;
            }

            float waterBrightness = client.player.getWaterBrightness();
            float generalBrightness;
            if (client.player.isPotionActive(Effects.NIGHT_VISION)) {
                generalBrightness = GameRenderer.getNightVisionBrightness(client.player, partialTicks);
            } else if (waterBrightness > 0.0F && client.player.isPotionActive(Effects.CONDUIT_POWER)) {
                generalBrightness = waterBrightness;
            } else {
                generalBrightness = 0.0F;
            }

            Vector3f surfaceColor = new Vector3f(1.0F, 0, 0);
            surfaceColor.lerp(new Vector3f(1.0F, 0, 0), 0.35F);
            float torchFlicker2 = torchFlicker + 1.5F;
            Vector3f vector3f1 = new Vector3f();

            for (int y = 0; y < 16; ++y) {
                for (int x = 0; x < 16; ++x) {
                    float worldLightBrightness = getLightBrightness(clientworld, y) * worldBrightness;
                    float blockBrightness = getLightBrightness(clientworld, x) * torchFlicker2;
                    float f7 = blockBrightness * ((blockBrightness * 0.6F + 0.4F) * 0.6F + 0.4F);
                    float f8 = blockBrightness * (blockBrightness * blockBrightness * 0.6F + 0.4F);
                    vector3f1.set(blockBrightness, f7, f8);
                    if (clientworld.func_239132_a_().func_241684_d_()) { //shouldRenderSky
                        vector3f1.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
                    } else {
                        Vector3f surfaceColor2 = surfaceColor.copy();
                        surfaceColor2.mul(worldLightBrightness);
                        vector3f1.add(surfaceColor2);
                        vector3f1.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                        if (entityRenderer.getBossColorModifier(partialTicks) > 0.0F) {
                            float bossColorModifier = entityRenderer.getBossColorModifier(partialTicks);
                            Vector3f vector3f3 = vector3f1.copy();
                            vector3f3.mul(0.7F, 0.6F, 0.6F);
                            vector3f1.lerp(vector3f3, bossColorModifier);
                        }
                    }

                    vector3f1.clamp(0.0F, 1.0F);
                    if (generalBrightness > 0.0F) {
                        float f10 = Math.max(vector3f1.getX(), Math.max(vector3f1.getY(), vector3f1.getZ()));
                        if (f10 < 1.0F) {
                            float f12 = 1.0F / f10;
                            Vector3f vector3f5 = vector3f1.copy();
                            vector3f5.mul(f12);
                            vector3f1.lerp(vector3f5, generalBrightness);
                        }
                    }

                    float gamma = (float) client.gameSettings.gamma;
                    Vector3f vector3f4 = vector3f1.copy();
                    vector3f4.apply(BetterWeatherUtil::invGamma);
                    vector3f1.lerp(vector3f4, gamma);
                    vector3f1.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                    vector3f1.clamp(0.0F, 1.0F);
                    vector3f1.mul(255.0F); //Translate back to rgb range of 0-255 as opposed to the 0-1.0 range
                    int k = (int) vector3f1.getX();
                    int l = (int) vector3f1.getY();
                    int i1 = (int) vector3f1.getZ();
                    nativeImage.setPixelRGBA(x, y, -16777216 | i1 << 16 | l << 8 | k);
                }
            }

            dynamicTexture.updateDynamicTexture();
            client.getProfiler().endSection();
        }
        return false;
    }

    private static float invGamma(float valueIn) {
        float f = 1.0F - valueIn;
        return 1.0F - f * f * f * f;
    }

    private static float getLightBrightness(World worldIn, int lightLevelIn) {
        return worldIn.getDimensionType().getAmbientLight(lightLevelIn);
    }

    @OnlyIn(Dist.CLIENT)
    public static void refreshViewFrustum(Minecraft minecraft, int renderDistance) {
        if (!BetterWeather.usingOptifine)
            ((WeatherViewFrustum) ((ViewFrustumGetter) minecraft.worldRenderer).getViewFrustum()).forceRenderDistance(renderDistance, minecraft.player.getPosX(), minecraft.player.getPosY(), minecraft.player.getPosZ());
    }

    public static boolean isOverworld(RegistryKey<World> worldKey) {
        return worldKey == World.OVERWORLD;
    }
}