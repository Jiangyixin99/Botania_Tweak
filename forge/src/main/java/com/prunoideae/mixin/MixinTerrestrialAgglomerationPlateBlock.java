package com.prunoideae.mixin;

import com.prunoideae.recipe.AgglomerationRecipe;
import com.prunoideae.recipe.AgglomerationRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.common.block.mana.TerrestrialAgglomerationPlateBlock;

@Mixin(TerrestrialAgglomerationPlateBlock.class)
public class MixinTerrestrialAgglomerationPlateBlock {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, Level world, BlockPos pos, Player player,
                       InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return;

        // 直接调用该静态方法进行判断
        if (!AgglomerationRecipes.containsItem(stack)) return;

        // 执行放置逻辑（仅服务端执行实际修改，客户端返回成功即可）
        if (!world.isClientSide) {
            ItemStack target = stack.split(1);
            ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, target);
            item.setPickUpDelay(40);
            item.setDeltaMovement(Vec3.ZERO);
            world.addFreshEntity(item);
        }

        cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide));
    }
}