package com.sourceblock.event;

import com.sourceblock.item.ModItems;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ModEvents {
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // 检查是否右键牛
        if (event.getTarget().getType() == EntityType.COW) {
            ItemStack heldItem = event.getItemStack();
            
            // 检查玩家是否手持空槽方块
            if (heldItem.getItem() == ModItems.EMPTY_SOURCE_BLOCK.get()) {
                // 客户端也要返回成功，否则手臂不会挥动
                if (event.getLevel().isClientSide) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
                
                // 服务端处理逻辑
                Cow cow = (Cow) event.getTarget();
                
                // 给予玩家牛奶槽
                ItemStack milkSourceBlock = new ItemStack(ModItems.MILK_SOURCE_BLOCK.get());
                
                if (!event.getEntity().isCreative()) {
                    heldItem.shrink(1);
                }
                
                // 添加牛奶槽到玩家背包
                if (!event.getEntity().addItem(milkSourceBlock)) {
                    event.getEntity().drop(milkSourceBlock, false);
                }
                
                // 移除牛
                cow.discard();
                
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}

