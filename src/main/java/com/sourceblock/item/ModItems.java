package com.sourceblock.item;

import com.sourceblock.SourceBlockMod;
import com.sourceblock.block.ItemSourceBlock;
import com.sourceblock.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = 
        DeferredRegister.createItems(SourceBlockMod.MODID);

    public static final DeferredItem<BlockItem> EMPTY_SOURCE_BLOCK = ITEMS.register("empty_source_block",
            () -> new BlockItem(ModBlocks.EMPTY_SOURCE_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> WATER_SOURCE_BLOCK = ITEMS.register("water_source_block",
            () -> new BlockItem(ModBlocks.WATER_SOURCE_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> LAVA_SOURCE_BLOCK = ITEMS.register("lava_source_block",
            () -> new BlockItem(ModBlocks.LAVA_SOURCE_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> MILK_SOURCE_BLOCK = ITEMS.register("milk_source_block",
            () -> new BlockItem(ModBlocks.MILK_SOURCE_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> CREATIVE_SOURCE_BLOCK = ITEMS.register("creative_source_block",
        () -> new BlockItem(ModBlocks.CREATIVE_SOURCE_BLOCK.get(), 
            new Item.Properties()));

    public static final DeferredItem<BlockItem> CREATIVE_ITEM_SOURCE_BLOCK = ITEMS.register("creative_item_source_block",
        () -> new BlockItem(ModBlocks.CREATIVE_ITEM_SOURCE_BLOCK.get(), 
            new Item.Properties()));

    public static final DeferredItem<BlockItem> EMPTY_ITEM_SOURCE_BLOCK = ITEMS.register("empty_item_source_block",
        () -> new ItemSourceBlockItem(ModBlocks.ITEM_SOURCE_BLOCK.get(), 
            new Item.Properties(), ItemSourceBlock.ItemType.EMPTY));

    public static final DeferredItem<BlockItem> COBBLESTONE_SOURCE_BLOCK = ITEMS.register("cobblestone_source_block",
        () -> new ItemSourceBlockItem(ModBlocks.ITEM_SOURCE_BLOCK.get(), 
            new Item.Properties(), ItemSourceBlock.ItemType.COBBLESTONE));

    public static final DeferredItem<BlockItem> STONE_SOURCE_BLOCK = ITEMS.register("stone_source_block",
        () -> new ItemSourceBlockItem(ModBlocks.ITEM_SOURCE_BLOCK.get(),
            new Item.Properties(), ItemSourceBlock.ItemType.STONE));

    public static final DeferredItem<BlockItem> SMOOTH_STONE_SOURCE_BLOCK = ITEMS.register("smooth_stone_source_block",
        () -> new ItemSourceBlockItem(ModBlocks.ITEM_SOURCE_BLOCK.get(),
            new Item.Properties(), ItemSourceBlock.ItemType.SMOOTH_STONE));

    public static final DeferredItem<BlockItem> OBSIDIAN_SOURCE_BLOCK = ITEMS.register("obsidian_source_block",
        () -> new ItemSourceBlockItem(ModBlocks.ITEM_SOURCE_BLOCK.get(), 
            new Item.Properties(), ItemSourceBlock.ItemType.OBSIDIAN));
}

