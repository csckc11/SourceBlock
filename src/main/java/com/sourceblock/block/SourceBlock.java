package com.sourceblock.block;

import com.mojang.serialization.MapCodec;
import com.sourceblock.block.entity.SourceBlockEntity;
import com.sourceblock.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SourceBlock extends BaseEntityBlock {
    public static final MapCodec<SourceBlock> CODEC = simpleCodec(SourceBlock::new);

    private final FluidType fluidType;

    /** codec 反序列化用，默认 EMPTY（实际方块由注册名区分） */
    public SourceBlock(Properties properties) {
        this(properties, FluidType.EMPTY);
    }

    public SourceBlock(Properties properties, FluidType fluidType) {
        super(properties);
        this.fluidType = fluidType;
    }

    public FluidType getFluidType() {
        return this.fluidType;
    }

    /** 从 BlockState 取流体类型（供 BlockEntity 使用） */
    public static FluidType getFluidTypeFromState(BlockState state) {
        return state.getBlock() instanceof SourceBlock sb ? sb.getFluidType() : FluidType.EMPTY;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, BlockState state, @NotNull Level level,
                                                       @NotNull BlockPos pos, @NotNull Player player,
                                                       @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        FluidType currentType = this.fluidType;
        ContainerFluid containerFluid = getSupportedContainerFluid(stack);

        // 空源方块 + 支持的流体容器 -> 替换为对应流体方块
        if (currentType == FluidType.EMPTY && containerFluid != null) {
            if (!level.isClientSide) {
                ItemStack result = drainOneContainer(stack, containerFluid.fluidStack());
                if (!result.isEmpty()) {
                    Block newBlock = ModBlocks.getSourceBlock(containerFluid.fluidType()).get();
                    level.setBlockAndUpdate(pos, newBlock.defaultBlockState());
                    if (!player.isCreative()) {
                        replaceHeldContainer(player, hand, stack, result);
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // 非空源方块 + 流体容器 -> 抽取
        if (currentType != FluidType.EMPTY && hasFluidContainerCapability(stack)) {
            if (level.isClientSide) {
                return ItemInteractionResult.sidedSuccess(true);
            }
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, hitResult.getDirection());
            if (handler != null && FluidUtil.interactWithFluidHandler(player, hand, handler)) {
                return ItemInteractionResult.sidedSuccess(false);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    private static ContainerFluid getSupportedContainerFluid(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return FluidUtil.getFluidContained(stack)
                .filter(fluidStack -> fluidStack.getAmount() >= 1000)
                .map(fluidStack -> {
                    FluidType fluidType = getSupportedFluidType(fluidStack.getFluid());
                    return fluidType == null ? null : new ContainerFluid(fluidType, fluidStack.copyWithAmount(1000));
                })
                .orElse(null);
    }

    @Nullable
    private static FluidType getSupportedFluidType(Fluid fluid) {
        if (fluid == Fluids.WATER) return FluidType.WATER;
        if (fluid == Fluids.LAVA) return FluidType.LAVA;
        if (isMilkFluid(fluid)) return FluidType.MILK;
        return null;
    }

    private static boolean isMilkFluid(Fluid fluid) {
        if (fluid == Fluids.EMPTY) return false;
        if (NeoForgeMod.MILK.isBound() && fluid == NeoForgeMod.MILK.value()) return true;
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        return "milk".equals(id.getPath());
    }

    private static boolean hasFluidContainerCapability(ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    private static ItemStack drainOneContainer(ItemStack stack, FluidStack requested) {
        ItemStack singleContainer = stack.copyWithCount(1);
        IFluidHandler handler = singleContainer.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null || requested.isEmpty()) return ItemStack.EMPTY;

        FluidStack simulated = handler.drain(requested, IFluidHandler.FluidAction.SIMULATE);
        if (simulated.getAmount() < requested.getAmount()
                || !FluidStack.isSameFluidSameComponents(simulated, requested)) {
            return ItemStack.EMPTY;
        }
        handler.drain(requested, IFluidHandler.FluidAction.EXECUTE);
        return handler instanceof net.neoforged.neoforge.fluids.capability.IFluidHandlerItem itemHandler
                ? itemHandler.getContainer() : ItemStack.EMPTY;
    }

    private static void replaceHeldContainer(Player player, InteractionHand hand, ItemStack original, ItemStack result) {
        if (original.getCount() == 1) {
            player.setItemInHand(hand, result);
            return;
        }
        original.shrink(1);
        if (!player.addItem(result)) {
            player.drop(result, false);
        }
    }

    private record ContainerFluid(FluidType fluidType, FluidStack fluidStack) {}

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SourceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType,
                com.sourceblock.block.entity.ModBlockEntities.SOURCE_BLOCK_ENTITY.get(),
                SourceBlockEntity::serverTick);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.@NotNull Builder builder) {
        return Collections.singletonList(new ItemStack(getDropItem()));
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult target,
                                                net.minecraft.world.level.LevelReader level,
                                                @NotNull BlockPos pos, @NotNull Player player) {
        return new ItemStack(getDropItem());
    }

    private Item getDropItem() {
        return switch (fluidType) {
            case WATER -> ModItems.WATER_SOURCE_BLOCK.get();
            case LAVA  -> ModItems.LAVA_SOURCE_BLOCK.get();
            case MILK  -> ModItems.MILK_SOURCE_BLOCK.get();
            default    -> ModItems.EMPTY_SOURCE_BLOCK.get();
        };
    }

    public enum FluidType implements net.minecraft.util.StringRepresentable {
        EMPTY("empty"),
        WATER("water"),
        LAVA("lava"),
        MILK("milk");

        private final String name;

        FluidType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}