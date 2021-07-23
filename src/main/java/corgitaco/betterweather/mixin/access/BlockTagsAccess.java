package corgitaco.betterweather.mixin.access;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockTags.class)
public interface BlockTagsAccess {

    @Accessor
    static TagRegistry<Block> getREGISTRY() {
        throw new Error("Mixin did not apply!");
    }
}
