package corgitaco.betterweather;

import corgitaco.betterweather.season.Season;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BetterWeatherUtil {
    public static int removeLeavesFromHeightMap(World world, BlockPos pos) {
        BlockPos.Mutable heightMapOriginalPos = new BlockPos.Mutable(pos.getX(), world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()), pos.getZ());

        while(world.getBlockState(heightMapOriginalPos.down()).getBlock().isIn(BlockTags.LEAVES) || !world.getBlockState(heightMapOriginalPos.down()).getMaterial().blocksMovement() && world.getBlockState(heightMapOriginalPos.down()).getFluidState().isEmpty())
            heightMapOriginalPos.move(Direction.DOWN);

        return heightMapOriginalPos.getY();
    }

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
}