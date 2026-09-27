package com.sourceblock;

import com.mojang.logging.LogUtils;
import com.sourceblock.block.ModBlocks;
import com.sourceblock.block.entity.ModBlockEntities;
import com.sourceblock.compat.MekanismCompat;
import com.sourceblock.event.ModEvents;
import com.sourceblock.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(SourceBlockMod.MODID)
public class SourceBlockMod {
    public static final String MODID = "sourceblock";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold CreativeModeTabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SOURCE_BLOCK_TAB = 
        CREATIVE_MODE_TABS.register("source_block_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sourceblock"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> ModItems.EMPTY_SOURCE_BLOCK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.EMPTY_SOURCE_BLOCK.get());
                output.accept(ModItems.WATER_SOURCE_BLOCK.get());
                output.accept(ModItems.LAVA_SOURCE_BLOCK.get());
                output.accept(ModItems.MILK_SOURCE_BLOCK.get());
                output.accept(ModItems.CREATIVE_SOURCE_BLOCK.get());
                output.accept(ModItems.CREATIVE_ITEM_SOURCE_BLOCK.get());
                output.accept(ModItems.EMPTY_ITEM_SOURCE_BLOCK.get());
                output.accept(ModItems.COBBLESTONE_SOURCE_BLOCK.get());
                output.accept(ModItems.STONE_SOURCE_BLOCK.get());
                output.accept(ModItems.SMOOTH_STONE_SOURCE_BLOCK.get());
                output.accept(ModItems.OBSIDIAN_SOURCE_BLOCK.get());
            }).build());

    public SourceBlockMod(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgeMod.enableMilkFluid();

        // 注册方块、物品、方块实体
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // 注册事件监听器
        NeoForge.EVENT_BUS.register(ModEvents.class);

        // 注册创造模式物品栏
        modEventBus.addListener(this::addCreative);
        
        // 注册方块处理能力
        modEventBus.addListener(this::registerCapabilities);
        
        // 初始化Mekanism兼容性（如果Mekanism已安装）
        try {
            MekanismCompat.init(modEventBus);
        } catch (NoClassDefFoundError e) {
            // Mekanism未安装，忽略
            LOGGER.info("Mekanism未安装，跳过气体处理功能");
        }

        LOGGER.info("Source Block Mod initialized");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 注册源方块的流体处理能力
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            ModBlockEntities.SOURCE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.createFluidHandler()
        );
        
        // 注册源方块的能量处理能力（用于销毁能量）
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntities.SOURCE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.createEnergyStorage()
        );

        // 注册创造源方块的流体处理能力
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            ModBlockEntities.CREATIVE_SOURCE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.createFluidHandler()
        );

        // 注册创造物品源方块的物品处理能力
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.CREATIVE_ITEM_SOURCE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.createItemHandler()
        );

        // 注册物品源方块的物品处理能力
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.ITEM_SOURCE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.createItemHandler()
        );

        // 注册物品源方块的流体处理能力（用于销毁流体）
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            ModBlockEntities.ITEM_SOURCE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.createFluidHandler()
        );

        // 注册物品源方块的能量处理能力（用于销毁能量）
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntities.ITEM_SOURCE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.createEnergyStorage()
        );
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.EMPTY_SOURCE_BLOCK.get());
            event.accept(ModItems.WATER_SOURCE_BLOCK.get());
            event.accept(ModItems.LAVA_SOURCE_BLOCK.get());
            event.accept(ModItems.MILK_SOURCE_BLOCK.get());
            event.accept(ModItems.CREATIVE_SOURCE_BLOCK.get());
            event.accept(ModItems.CREATIVE_ITEM_SOURCE_BLOCK.get());
            event.accept(ModItems.EMPTY_ITEM_SOURCE_BLOCK.get());
            event.accept(ModItems.COBBLESTONE_SOURCE_BLOCK.get());
            event.accept(ModItems.STONE_SOURCE_BLOCK.get());
            event.accept(ModItems.SMOOTH_STONE_SOURCE_BLOCK.get());
            event.accept(ModItems.OBSIDIAN_SOURCE_BLOCK.get());
        }
    }
}

