package com.prunoideae.modules.botania.handler;

import com.prunoideae.modules.botania.config.BotaniaConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

public class NonGOGWaterBowlHandler {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        // 只在服务端处理
        if (event.getLevel().isClientSide) return;

        // 检查 Garden of Glass 是否加载（通过 ModList）
        boolean isGogLoaded = ModList.get().isLoaded("gardenofglass");
        // 如果 GOG 已加载，或者配置未启用，则直接返回
        if (isGogLoaded || !BotaniaConfig.NON_GOG_WATER_BOWL.get()) {
            return;
        }

        Player player = event.getEntity();
        if (player == null || player instanceof FakePlayer) return;

        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() != Items.BOWL) return;

        Level world = event.getLevel();
        BlockPos pos = event.getPos(); // 这个 pos 是被右击的方块位置，但我们需要射线追踪来精确检测水源

        // 使用 ToolCommons.raytraceFromEntity 进行射线追踪，距离 4.5 格，检测流体
        HitResult hitResult = ToolCommons.raytraceFromEntity(player, 4.5D, true);
        if (hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos hitPos = blockHit.getBlockPos();
        BlockState hitState = world.getBlockState(hitPos);
        FluidState fluidState = hitState.getFluidState();

        // 检查是否为水源（流体为水且是水源方块）
        if (!fluidState.isEmpty() && fluidState.getType() == Fluids.WATER) {
            // 消耗碗，给予水碗
            heldItem.shrink(1);
            ItemStack waterBowl = new ItemStack(BotaniaItems.waterBowl);

            if (heldItem.isEmpty()) {
                player.setItemInHand(hand, waterBowl);
            } else {
                ItemHandlerHelper.giveItemToPlayer(player, waterBowl);
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}