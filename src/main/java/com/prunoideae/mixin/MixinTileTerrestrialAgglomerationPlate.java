package com.prunoideae.mixin;

import com.prunoideae.KubeJSBotania;
import com.prunoideae.recipe.AgglomerationRecipe;
import com.prunoideae.recipe.CustomTerrestrialAgglomerationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
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
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(TerrestrialAgglomerationPlateBlockEntity.class)
public class MixinTileTerrestrialAgglomerationPlate {

    @Shadow(remap = false)
    private boolean hasValidPlatform() {
        throw new AssertionError();
    }

    @Unique
    private AgglomerationRecipe botaniatweaks$currentRecipe;

    @Unique
    private List<AgglomerationRecipe> botaniatweaks$candidateRecipes = new ArrayList<>();

    @Unique
    private AgglomerationRecipe botaniatweaks$currentMatchedRecipe;

    /**
     * 重写 hasValidPlatform：收集所有结构匹配的配方
     */
    @Inject(method = "hasValidPlatform", at = @At("RETURN"), cancellable = true, remap = false)
    private void onHasValidPlatformReturn(CallbackInfoReturnable<Boolean> cir) {
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

        // 收集所有结构匹配的配方
        botaniatweaks$candidateRecipes.clear();
        RecipeManager manager = level.getRecipeManager();
        for (var recipe : manager.getAllRecipesFor(KubeJSBotania.CUSTOM_TERRA_PLATE_TYPE)) {
            if (recipe instanceof AgglomerationRecipe) {
                if (((AgglomerationRecipe) recipe).structureMatches(level, pos, below, side, corner)) {
                    botaniatweaks$candidateRecipes.add((AgglomerationRecipe) recipe);
                }
            }
        }

        if (!botaniatweaks$candidateRecipes.isEmpty()) {
            cir.setReturnValue(true); // 平台有效
        } else {
            cir.setReturnValue(false);
        }
    }

    /**
     * 重写 getCurrentRecipe：从候选配方中找出物品匹配的
     */
    @Inject(method = "getCurrentRecipe", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetCurrentRecipe(SimpleContainer container, CallbackInfoReturnable<TerrestrialAgglomerationRecipe> cir) {
        if (botaniatweaks$candidateRecipes.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        // 将容器内容转换为物品列表
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) inputs.add(stack);
        }

        // 尝试所有候选配方
        for (AgglomerationRecipe recipe : botaniatweaks$candidateRecipes) {
            if (recipe.itemsMatch(inputs)) {
                botaniatweaks$currentMatchedRecipe = recipe; // 缓存当前匹配的配方
                ResourceLocation fakeId = new ResourceLocation("botaniatweaks", "custom_" + System.identityHashCode(recipe));
                cir.setReturnValue(new CustomTerrestrialAgglomerationRecipe(recipe, fakeId));
                return;
            }
        }

        botaniatweaks$currentMatchedRecipe = null;
        cir.setReturnValue(null);
    }

    /**
     * 获取板上的所有物品（辅助方法）
     */
    @Unique
    private List<ItemStack> getItemsOnPlate(TerrestrialAgglomerationPlateBlockEntity plate) {
        List<ItemStack> items = new ArrayList<>();
        // 获取板上的物品实体
        Level level = plate.getLevel();
        if (level != null) {
            BlockPos pos = plate.getBlockPos();
            level.getEntitiesOfClass(ItemEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(0.5),
                    item -> item.isAlive() && item.getBlockY() == pos.getY() + 1
            ).forEach(item -> {
                items.add(item.getItem().copy());
            });
        }
        return items;
    }

    /**
     * 在合成完成时处理额外输出和结构替换
     */
    @Inject(method = "serverTick",
            at = @At(value = "FIELD",
                    target = "Lvazkii/botania/common/block/block_entity/TerrestrialAgglomerationPlateBlockEntity;mana:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER),
            remap = false)
    private static void onCraftComplete(Level level, BlockPos worldPosition, BlockState state,
                                        TerrestrialAgglomerationPlateBlockEntity self, CallbackInfo ci) {
        System.out.println("[BotaniaTweaks] onCraftComplete triggered (FIELD)");

        // 获取当前实例的 Mixin 接口
        MixinTileTerrestrialAgglomerationPlate mixin = (MixinTileTerrestrialAgglomerationPlate) (Object) self;
        AgglomerationRecipe aggRecipe = mixin.botaniatweaks$currentMatchedRecipe;

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

            // 结构替换
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