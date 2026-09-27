package com.sourceblock.block.entity;

import com.sourceblock.block.SourceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SourceBlockEntity extends BlockEntity implements IFluidHandler, IEnergyStorage {
    private int tickCounter = 0;
    private final Map<Direction, Boolean> fastMode = new HashMap<>();

    private static final int SLOW_INTERVAL = 20;
    private static final int FAST_INTERVAL = 1;
    private static final int TRANSFER_AMOUNT = Integer.MAX_VALUE;
    public static final int CAPACITY = Integer.MAX_VALUE;

    private static Fluid cachedMilkFluid = null;
    private static boolean milkFluidChecked = false;

    public SourceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SOURCE_BLOCK_ENTITY.get(), pos, blockState);
        for (Direction direction : Direction.values()) {
            fastMode.put(direction, true);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SourceBlockEntity blockEntity) {
        if (level.isClientSide) return;

        SourceBlock.FluidType fluidType = SourceBlock.getFluidTypeFromState(state);
        if (fluidType == SourceBlock.FluidType.EMPTY) return;

        FluidStack fluidStack = getFluidStackFromType(fluidType);
        if (fluidStack.isEmpty()) return;

        blockEntity.tickCounter++;
        for (Direction direction : Direction.values()) {
            boolean isFastMode = blockEntity.fastMode.get(direction);
            int interval = isFastMode ? FAST_INTERVAL : SLOW_INTERVAL;

            if (blockEntity.tickCounter % interval == 0) {
                BlockPos neighborPos = pos.relative(direction);
                boolean success = tryTransferFluid(level, neighborPos, direction.getOpposite(), fluidStack);
                blockEntity.fastMode.put(direction, success);
            }
        }
        blockEntity.setChanged();
    }

    private static FluidStack getFluidStackFromType(SourceBlock.FluidType type) {
        return switch (type) {
            case WATER -> new FluidStack(Fluids.WATER, TRANSFER_AMOUNT);
            case LAVA  -> new FluidStack(Fluids.LAVA, TRANSFER_AMOUNT);
            case MILK  -> getMilkFluidStack();
            default    -> FluidStack.EMPTY;
        };
    }

    private static FluidStack getMilkFluidStack() {
        if (milkFluidChecked && cachedMilkFluid == null) return FluidStack.EMPTY;
        if (cachedMilkFluid != null && cachedMilkFluid != Fluids.EMPTY) {
            return new FluidStack(cachedMilkFluid, TRANSFER_AMOUNT);
        }

        if (NeoForgeMod.MILK.isBound()) {
            Fluid forgeMilk = NeoForgeMod.MILK.value();
            if (forgeMilk != Fluids.EMPTY) {
                cachedMilkFluid = forgeMilk;
                milkFluidChecked = true;
                return new FluidStack(forgeMilk, TRANSFER_AMOUNT);
            }
        }

        String[] milkFluidIds = {
                "minecraft:milk", "create:milk", "createbigcannons:milk",
                "create_confectionery:milk", "ad_astra:milk", "thermal:milk", "productivebees:milk"
        };
        for (String fluidId : milkFluidIds) {
            Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidId));
            if (fluid != Fluids.EMPTY) {
                cachedMilkFluid = fluid;
                milkFluidChecked = true;
                return new FluidStack(fluid, TRANSFER_AMOUNT);
            }
        }

        for (var entry : BuiltInRegistries.FLUID.entrySet()) {
            String id = entry.getKey().location().toString();
            String name = id.split(":")[1];
            if (name.equals("milk")) {
                Fluid fluid = entry.getValue();
                if (fluid != null && fluid != Fluids.EMPTY) {
                    cachedMilkFluid = fluid;
                    milkFluidChecked = true;
                    return new FluidStack(fluid, TRANSFER_AMOUNT);
                }
            }
        }

        milkFluidChecked = true;
        cachedMilkFluid = Fluids.EMPTY;
        return FluidStack.EMPTY;
    }

    static boolean tryTransferFluid(Level level, BlockPos pos, Direction direction, FluidStack fluidStack) {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction);
        if (handler != null) {
            int filled = handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            return filled > 0;
        }
        return false;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
    }

    // ========== IFluidHandler ==========

    public IFluidHandler createFluidHandler() { return this; }
    public IEnergyStorage createEnergyStorage() { return this; }

    @Override
    public int getTanks() { return 1; }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank != 0 || level == null) return FluidStack.EMPTY;
        SourceBlock.FluidType fluidType = SourceBlock.getFluidTypeFromState(getBlockState());
        return switch (fluidType) {
            case WATER -> new FluidStack(Fluids.WATER, CAPACITY);
            case LAVA  -> new FluidStack(Fluids.LAVA, CAPACITY);
            case MILK  -> getMilkFluidStack();
            default    -> FluidStack.EMPTY;
        };
    }

    @Override
    public int getTankCapacity(int tank) { return CAPACITY; }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (level == null) return false;
        return SourceBlock.getFluidTypeFromState(getBlockState()) == SourceBlock.FluidType.EMPTY;
    }

    @Override
    public int fill(FluidStack resource, @NotNull FluidAction action) {
        if (resource.isEmpty() || level == null) return 0;
        if (SourceBlock.getFluidTypeFromState(getBlockState()) == SourceBlock.FluidType.EMPTY) {
            return resource.getAmount();
        }
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, @NotNull FluidAction action) {
        if (resource.isEmpty() || level == null) return FluidStack.EMPTY;
        FluidStack stored = getFluidInTank(0);
        if (!stored.isEmpty() && stored.getFluid() == resource.getFluid()) {
            return new FluidStack(resource.getFluid(), resource.getAmount());
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, @NotNull FluidAction action) {
        if (maxDrain <= 0 || level == null) return FluidStack.EMPTY;
        FluidStack stored = getFluidInTank(0);
        if (stored.isEmpty()) return FluidStack.EMPTY;
        return new FluidStack(stored.getFluid(), maxDrain);
    }

    // ========== IEnergyStorage ==========

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (level == null) return 0;
        if (SourceBlock.getFluidTypeFromState(getBlockState()) == SourceBlock.FluidType.EMPTY) {
            return maxReceive;
        }
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) { return 0; }

    @Override
    public int getEnergyStored() { return 0; }

    @Override
    public int getMaxEnergyStored() {
        if (level == null) return 0;
        if (SourceBlock.getFluidTypeFromState(getBlockState()) == SourceBlock.FluidType.EMPTY) {
            return Integer.MAX_VALUE;
        }
        return 0;
    }

    @Override
    public boolean canExtract() { return false; }

    @Override
    public boolean canReceive() {
        if (level == null) return false;
        return SourceBlock.getFluidTypeFromState(getBlockState()) == SourceBlock.FluidType.EMPTY;
    }
}