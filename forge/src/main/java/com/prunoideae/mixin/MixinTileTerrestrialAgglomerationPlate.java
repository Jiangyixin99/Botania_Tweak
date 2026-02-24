package com.prunoideae.mixin;

import com.prunoideae.recipe.AgglomerationRecipe;
import com.prunoideae.recipe.AgglomerationRecipes;
import com.prunoideae.recipe.CustomTerrestrialAgglomerationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(TerrestrialAgglomerationPlateBlockEntity.class)
public class MixinTileTerrestrialAgglomerationPlate {

    @Shadow(remap = false)
    private boolean hasValidPlatform() {
        // 仅用于 @Shadow，实际由 Mixin 注入目标类的方法
        throw new AssertionError();
    }

    @Unique
    private AgglomerationRecipe botaniatweaks$currentRecipe;

    /**
     * 重写 hasValidPlatform：先检查原版结构，如果不匹配则尝试自定义结构。
     * 若自定义结构匹配，则根据结构确定对应的配方（此时不考虑输入物品），并缓存。
     */
    @Inject(method = "hasValidPlatform", at = @At("RETURN"), cancellable = true, remap = false)
    private void onHasValidPlatformReturn(CallbackInfoReturnable<Boolean> cir) {
        // 如果原版结构已经匹配，我们仍希望使用自定义结构？根据需求，我们需要完全替换，所以原版匹配也应视为无效
        // 为了完全抛弃原版，这里直接忽略原版结果，只检查自定义结构
        TerrestrialAgglomerationPlateBlockEntity self = (TerrestrialAgglomerationPlateBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            cir.setReturnValue(false);
            return;
        }

        BlockPos pos = self.getBlockPos();
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        BlockState side = level.getBlockState(belowPos.north());
        BlockState corner = level.getBlockState(belowPos.north().east());

        // 根据结构查找配方（此时传入空输入，或者专门的结构查找方法）
        Optional<AgglomerationRecipe> recipeOpt = AgglomerationRecipes.findByStructure(level, pos, below, side, corner);
        if (recipeOpt.isPresent()) {
            botaniatweaks$currentRecipe = recipeOpt.get();
            cir.setReturnValue(true); // 平台有效
        } else {
            botaniatweaks$currentRecipe = null; // 清除缓存
            cir.setReturnValue(false);
        }
    }

    /**
     * 重写 getCurrentRecipe：完全忽略原版配方，只返回缓存的配方（如果输入物品匹配）
     */
    @Inject(method = "getCurrentRecipe", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetCurrentRecipe(SimpleContainer container, CallbackInfoReturnable<TerrestrialAgglomerationRecipe> cir) {
        // 如果缓存中没有配方，直接返回 null
        if (botaniatweaks$currentRecipe == null) {
            cir.setReturnValue(null);
            return;
        }

        // 将容器内容转换为物品列表（Botania 传入的已经是展平后的）
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) inputs.add(stack);
        }

        // 检查输入物品是否匹配当前缓存的配方
        if (botaniatweaks$currentRecipe.itemsMatch(inputs)) {
            // 生成一个临时 ID（实际应使用配方本身的 ID，此处仅为示例）
            ResourceLocation fakeId = new ResourceLocation("botaniatweaks", "custom_" + System.identityHashCode(botaniatweaks$currentRecipe));
            cir.setReturnValue(new CustomTerrestrialAgglomerationRecipe(botaniatweaks$currentRecipe, fakeId));
        } else {
            cir.setReturnValue(null);
        }
    }
    @Inject(method = "serverTick",
            at = @At(value = "FIELD",
                    target = "Lvazkii/botania/common/block/block_entity/TerrestrialAgglomerationPlateBlockEntity;mana:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER),
            remap = false)
    private static void onCraftComplete(Level level, BlockPos worldPosition, BlockState state,
                                        TerrestrialAgglomerationPlateBlockEntity self, CallbackInfo ci) {
        System.out.println("[BotaniaTweaks] onCraftComplete triggered (FIELD)");
        AgglomerationRecipe aggRecipe = ((MixinTileTerrestrialAgglomerationPlate)(Object)self).botaniatweaks$currentRecipe;
        if (aggRecipe != null) {
            System.out.println("[BotaniaTweaks] Recipe found, generating outputs and replacing multiblock");

            // 获取所有输出物品
            List<ItemStack> outputs = aggRecipe.getOutputCopies();
            if (!outputs.isEmpty()) {
                // 第一个输出由原版生成，我们从第二个开始生成
                for (int i = 1; i < outputs.size(); i++) {
                    ItemStack stack = outputs.get(i);
                    if (!stack.isEmpty()) {
                        ItemEntity outputItem = new ItemEntity(level,
                                worldPosition.getX() + 0.5,
                                worldPosition.getY() + 0.3,
                                worldPosition.getZ() + 0.5,
                                stack.copy());
                        outputItem.setDeltaMovement(0, 0, 0);
                        level.addFreshEntity(outputItem);
                    }
                }
            }

            // 结构替换（原代码已存在）
            replaceMultiblock(level, worldPosition, aggRecipe);
        } else {
            System.out.println("[BotaniaTweaks] No cached recipe");
        }
    }

    private static void replaceMultiblock(Level level, BlockPos platePos, AgglomerationRecipe recipe) {
        if (recipe.multiblockCenterReplace != null) {
            replaceBlock(level, platePos.below(), recipe.multiblockCenterReplace);
        }
        if (recipe.multiblockEdgeReplace != null) {
            for (Direction horiz : Direction.Plane.HORIZONTAL) {
                replaceBlock(level, platePos.below().relative(horiz), recipe.multiblockEdgeReplace);
            }
        }
        if (recipe.multiblockCornerReplace != null) {
            for (Direction horiz : Direction.Plane.HORIZONTAL) {
                replaceBlock(level, platePos.below().relative(horiz).relative(horiz.getClockWise()), recipe.multiblockCornerReplace);
            }
        }
    }

    private static void replaceBlock(Level level, BlockPos pos, BlockState newState) {
        level.levelEvent(2001, pos, Block.getId(level.getBlockState(pos))); // 破坏效果
        level.setBlockAndUpdate(pos, newState);
    }
}
    /**
     * 可选：当配方完成或平台失效时清除缓存，避免残留。
     * 可以在 serverTick 完成后或其他时机清除，但通常 getCurrentRecipe 每次 tick 都会调用，
     * 只要 hasValidPlatform 在 tick 前被调用，缓存就会更新。为了保险，可以在 tick 末尾清除。
     * 但 Botania 的 serverTick 是静态方法，需要额外 Mixin。
     * 简单起见，让缓存随 hasValidPlatform 更新即可。
     */
