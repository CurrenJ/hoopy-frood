package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.core.HoopyFroodTut;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends BlockTagsProvider {
    public BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, HoopyFroodTut.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(HoopyFroodTutBlocks.LAVA_NEUTRALIZER.get());
    }
}
