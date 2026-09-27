package com.sourceblock.block;

import com.mojang.serialization.MapCodec;
import com.sourceblock.block.entity.ItemSourceBlockEntity;
import com.sourceblock.block.entity.ModBlockEntities;
import com.sourceblock.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ItemSourceBlock extends BaseEntityBlock {
    public static final MapCodec<ItemSourceBlock> CODEC = simpleCodec(ItemSourceBlock::new);

    private final ItemType itemType;

    /** codec 反序列化用，默认 EMPTY（实际方块由注册名区分） */
    public ItemSourceBlock(Properties properties) {
        this(properties, ItemType.EMPTY);
    }

    public ItemSourceBlock(Properties properties, ItemType itemType) {
        super(properties);
        this.itemType = itemType;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    /** 从 BlockState 取物品类型（供 BlockEntity 使用） */
    public static ItemType getItemTypeFromState(BlockState state) {
        return state.getBlock() instanceof ItemSourceBlock sb ? sb.getItemType() : ItemType.EMPTY;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,
                                                       @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        // 空手右键已填充的方块，可以获得物品
        if (stack.isEmpty() && this.itemType != ItemType.EMPTY) {
            if (!level.isClientSide && !player.isCreative()) {
                player.addItem(new ItemStack(getProducedItem()));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private Item getProducedItem() {
        return switch (this.itemType) {
            case COBBLESTONE -> Items.COBBLESTONE;
            case STONE -> Items.STONE;
            case SMOOTH_STONE -> Items.SMOOTH_STONE;
            case OBSIDIAN -> Items.OBSIDIAN;
            default -> Items.AIR;
        };
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ItemSourceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType,
                ModBlockEntities.ITEM_SOURCE_BLOCK_ENTITY.get(),
                ItemSourceBlockEntity::serverTick);
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(BlockState state, LootParams.@NotNull Builder builder) {
        return Collections.singletonList(new ItemStack(getDropItem()));
    }

    private Item getDropItem() {
        return switch (this.itemType) {
            case COBBLESTONE -> ModItems.COBBLESTONE_SOURCE_BLOCK.get();
            case STONE -> ModItems.STONE_SOURCE_BLOCK.get();
            case SMOOTH_STONE -> ModItems.SMOOTH_STONE_SOURCE_BLOCK.get();
            case OBSIDIAN -> ModItems.OBSIDIAN_SOURCE_BLOCK.get();
            default -> ModItems.EMPTY_ITEM_SOURCE_BLOCK.get();
        };
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult target,
                                                net.minecraft.world.level.LevelReader level,
                                                @NotNull BlockPos pos, @NotNull Player player) {
        return new ItemStack(getDropItem());
    }

    public enum ItemType implements StringRepresentable {
        EMPTY("empty"),
        COBBLESTONE("cobblestone"),
        STONE("stone"),
        SMOOTH_STONE("smooth_stone"),
        OBSIDIAN("obsidian");

        private final String name;

        ItemType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}