package com.sourceblock.block;

import com.sourceblock.SourceBlockMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(SourceBlockMod.MODID);

    public static final DeferredBlock<Block> EMPTY_SOURCE_BLOCK = BLOCKS.register("empty_source_block",
            () -> new SourceBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F, 1200.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 0), SourceBlock.FluidType.EMPTY));

    public static final DeferredBlock<Block> WATER_SOURCE_BLOCK = BLOCKS.register("water_source_block",
            () -> new SourceBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F, 1200.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 0), SourceBlock.FluidType.WATER));

    public static final DeferredBlock<Block> LAVA_SOURCE_BLOCK = BLOCKS.register("lava_source_block",
            () -> new SourceBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F, 1200.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 15), SourceBlock.FluidType.LAVA));

    public static final DeferredBlock<Block> MILK_SOURCE_BLOCK = BLOCKS.register("milk_source_block",
            () -> new SourceBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F, 1200.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 0), SourceBlock.FluidType.MILK));

    public static final DeferredBlock<Block> CREATIVE_SOURCE_BLOCK = BLOCKS.register("creative_source_block",
            () -> new CreativeSourceBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 15)));

    public static final DeferredBlock<Block> CREATIVE_ITEM_SOURCE_BLOCK = BLOCKS.register("creative_item_source_block",
            () -> new CreativeItemSourceBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.METAL)));

    public static final DeferredBlock<Block> ITEM_SOURCE_BLOCK = BLOCKS.register("item_source_block",
            () -> new ItemSourceBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F, 1200.0F)
                    .sound(SoundType.METAL)));

    /** 根据流体类型获取对应的源方块 */
    public static DeferredBlock<Block> getSourceBlock(SourceBlock.FluidType type) {
        return switch (type) {
            case WATER -> WATER_SOURCE_BLOCK;
            case LAVA  -> LAVA_SOURCE_BLOCK;
            case MILK  -> MILK_SOURCE_BLOCK;
            default    -> EMPTY_SOURCE_BLOCK;
        };
    }
}