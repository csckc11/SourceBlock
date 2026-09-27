package com.sourceblock.block.entity.compat;

import com.sourceblock.block.SourceBlock;
import com.sourceblock.block.entity.SourceBlockEntity;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Mekanism化学物质处理实现
 * 空源方块可以销毁所有输入的化学物质
 */
public class MekanismGasHandler implements IChemicalHandler {
    
    private final SourceBlockEntity blockEntity;
    
    public MekanismGasHandler(SourceBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }
    
    /**
     * 检查是否是空源方块（可以销毁化学物质）
     */
    private boolean isVoidMode() {
        if (blockEntity.getLevel() == null) return false;
        
        BlockState state = blockEntity.getBlockState();
        return SourceBlock.getFluidTypeFromState(state) == SourceBlock.FluidType.EMPTY;
    }
    
    @Override
    public int getChemicalTanks() {
        return isVoidMode() ? 1 : 0;
    }
    
    @NotNull
    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        // 虚空方块不存储化学物质
        return ChemicalStack.EMPTY;
    }
    
    @Override
    public void setChemicalInTank(int tank, @NotNull ChemicalStack stack) {
        // 不支持设置
    }
    
    @Override
    public long getChemicalTankCapacity(int tank) {
        // 返回最大容量表示可以接受任意数量的化学物质
        return isVoidMode() ? Long.MAX_VALUE : 0;
    }
    
    @Override
    public boolean isValid(int tank, @NotNull ChemicalStack stack) {
        // 空源方块接受所有化学物质用于销毁
        return isVoidMode();
    }
    
    @NotNull
    @Override
    public ChemicalStack insertChemical(int tank, @NotNull ChemicalStack stack, Action action) {
        if (!isVoidMode() || stack.isEmpty()) {
            return stack;
        }
        
        // 销毁所有输入的化学物质（返回空表示全部接受）
        return ChemicalStack.EMPTY;
    }
    
    @NotNull
    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        // 虚空方块不提供化学物质输出
        return ChemicalStack.EMPTY;
    }
}
