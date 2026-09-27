package com.sourceblock.block.entity;

import com.sourceblock.SourceBlockMod;
import com.sourceblock.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, SourceBlockMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SourceBlockEntity>> SOURCE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("source_block_entity", () ->
                    BlockEntityType.Builder.of(SourceBlockEntity::new,
                                    ModBlocks.EMPTY_SOURCE_BLOCK.get(),
                                    ModBlocks.WATER_SOURCE_BLOCK.get(),
                                    ModBlocks.LAVA_SOURCE_BLOCK.get(),
                                    ModBlocks.MILK_SOURCE_BLOCK.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeSourceBlockEntity>> CREATIVE_SOURCE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("creative_source_block_entity", () ->
                    BlockEntityType.Builder.of(CreativeSourceBlockEntity::new, ModBlocks.CREATIVE_SOURCE_BLOCK.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeItemSourceBlockEntity>> CREATIVE_ITEM_SOURCE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("creative_item_source_block_entity", () ->
                    BlockEntityType.Builder.of(CreativeItemSourceBlockEntity::new, ModBlocks.CREATIVE_ITEM_SOURCE_BLOCK.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemSourceBlockEntity>> ITEM_SOURCE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("item_source_block_entity", () ->
                    BlockEntityType.Builder.of(ItemSourceBlockEntity::new, ModBlocks.ITEM_SOURCE_BLOCK.get())
                            .build(null));
}