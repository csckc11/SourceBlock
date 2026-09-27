package com.sourceblock.block.entity;

import com.sourceblock.block.ItemSourceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 物品源方块实体 - 根据 blockstate 提供指定的物品
 */
public class ItemSourceBlockEntity extends BlockEntity {
    // 每个面的计时器
    private final Map<Direction, Integer> tickCounters = new HashMap<>();
    private final Map<Direction, Boolean> fastMode = new HashMap<>();
    
    private static final int SLOW_INTERVAL = 20;
    private static final int FAST_INTERVAL = 1;

    public ItemSourceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ITEM_SOURCE_BLOCK_ENTITY.get(), pos, blockState);
        
        // 初始化所有面的计时器和模式
        for (Direction direction : Direction.values()) {
            tickCounters.put(direction, 0);
            fastMode.put(direction, false);
        }
    }

    public ItemStack getStoredItem() {
        if (level == null) return ItemStack.EMPTY;
        
        BlockState state = getBlockState();
        ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
        
        return switch (itemType) {
            case COBBLESTONE -> new ItemStack(Items.COBBLESTONE, 64);
            case STONE -> new ItemStack(Items.STONE, 64);
            case SMOOTH_STONE -> new ItemStack(Items.SMOOTH_STONE, 64);
            case OBSIDIAN -> new ItemStack(Items.OBSIDIAN, 64);
            default -> ItemStack.EMPTY;
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemSourceBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
        if (itemType == ItemSourceBlock.ItemType.EMPTY) return;

        ItemStack storedItem = blockEntity.getStoredItem();
        if (storedItem.isEmpty()) return;

        // 对每个面进行处理
        for (Direction direction : Direction.values()) {
            int currentTick = blockEntity.tickCounters.get(direction);
            boolean isFastMode = blockEntity.fastMode.get(direction);
            
            currentTick++;
            
            int interval = isFastMode ? FAST_INTERVAL : SLOW_INTERVAL;
            if (currentTick >= interval) {
                currentTick = 0;
                
                BlockPos neighborPos = pos.relative(direction);
                boolean success = tryTransferItem(level, neighborPos, direction.getOpposite(), storedItem);
                
                blockEntity.fastMode.put(direction, success);
            }
            
            blockEntity.tickCounters.put(direction, currentTick);
        }

        blockEntity.setChanged();
    }

    static boolean tryTransferItem(Level level, BlockPos pos, Direction direction, ItemStack itemToTransfer) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
            if (handler != null) {
                ItemStack toInsert = itemToTransfer.copy();
                toInsert.setCount(Integer.MAX_VALUE);
                
                boolean insertedAny = false;
                // 持续尝试插入，直到无法再插入为止
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack remainder = handler.insertItem(i, toInsert, false);
                    if (remainder.getCount() < toInsert.getCount()) {
                        insertedAny = true;
                    }
                }
                return insertedAny;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        // blockstate 已经保存了类型信息，这里不需要额外保存
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        // blockstate 已经加载了类型信息，这里不需要额外加载
    }

    // ========== 客户端同步 ==========

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // 创建内部处理器以支持能力查询
    public IItemHandler createItemHandler() {
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return 1;
            }

            @NotNull
            @Override
            public ItemStack getStackInSlot(int slot) {
                if (slot != 0 || level == null) {
                    return ItemStack.EMPTY;
                }
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块不返回物品
                if (itemType == ItemSourceBlock.ItemType.EMPTY) {
                    return ItemStack.EMPTY;
                }
                
                ItemStack storedItem = getStoredItem();
                if (storedItem.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack result = storedItem.copy();
                result.setCount(Integer.MAX_VALUE);
                return result;
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (stack.isEmpty() || level == null) {
                    return stack;
                }
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块销毁所有输入的物品
                if (itemType == ItemSourceBlock.ItemType.EMPTY) {
                    return ItemStack.EMPTY; // 返回空表示全部接受（实际上是销毁）
                }
                
                // 其他类型不接受输入
                return stack;
            }

            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot != 0 || level == null) {
                    return ItemStack.EMPTY;
                }
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块不提供物品输出
                if (itemType == ItemSourceBlock.ItemType.EMPTY) {
                    return ItemStack.EMPTY;
                }
                
                ItemStack storedItem = getStoredItem();
                if (storedItem.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                
                ItemStack extracted = storedItem.copy();
                extracted.setCount(Math.min(amount, storedItem.getMaxStackSize()));
                return extracted;
            }

            @Override
            public int getSlotLimit(int slot) {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (level == null) return false;
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块接受任何物品（用于销毁）
                return itemType == ItemSourceBlock.ItemType.EMPTY;
            }
        };
    }

    public IFluidHandler createFluidHandler() {
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return 1;
            }

            @NotNull
            @Override
            public FluidStack getFluidInTank(int tank) {
                return FluidStack.EMPTY;
            }

            @Override
            public int getTankCapacity(int tank) {
                if (level == null) return 0;
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块显示无限容量用于销毁流体
                if (itemType == ItemSourceBlock.ItemType.EMPTY) {
                    return Integer.MAX_VALUE;
                }
                
                return 0;
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                if (level == null) return false;
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块接受任何流体（用于销毁）
                return itemType == ItemSourceBlock.ItemType.EMPTY;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                if (resource.isEmpty() || level == null) {
                    return 0;
                }
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块销毁所有输入的流体
                if (itemType == ItemSourceBlock.ItemType.EMPTY) {
                    return resource.getAmount();
                }
                
                return 0;
            }

            @NotNull
            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                return FluidStack.EMPTY;
            }

            @NotNull
            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return FluidStack.EMPTY;
            }
        };
    }

    public IEnergyStorage createEnergyStorage() {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                if (level == null) return 0;
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块销毁所有输入的能量
                if (itemType == ItemSourceBlock.ItemType.EMPTY) {
                    return maxReceive;
                }
                
                return 0;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return 0;
            }

            @Override
            public int getEnergyStored() {
                return 0;
            }

            @Override
            public int getMaxEnergyStored() {
                if (level == null) return 0;
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块显示无限容量用于销毁能量
                if (itemType == ItemSourceBlock.ItemType.EMPTY) {
                    return Integer.MAX_VALUE;
                }
                
                return 0;
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canReceive() {
                if (level == null) return false;
                
                BlockState state = getBlockState();
                ItemSourceBlock.ItemType itemType = ItemSourceBlock.getItemTypeFromState(state);
                
                // 空源方块可以接受能量（用于销毁）
                return itemType == ItemSourceBlock.ItemType.EMPTY;
            }
        };
    }
}

