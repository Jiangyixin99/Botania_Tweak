package com.prunoideae.recipe;

import com.prunoideae.KubeJSBotania;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.items.ItemHandlerHelper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AgglomerationRecipes {
    // 不再需要静态列表 RECIPES

    /**
     * 根据多方块结构查找配方（不检查物品）
     */
    public static Optional<AgglomerationRecipe> findByStructure(Level level, BlockPos platePos,
                                                                BlockState below, BlockState side, BlockState corner) {
        if (level == null) return Optional.empty();
        RecipeManager manager = level.getRecipeManager();
        // 改为自定义类型
        for (var recipe : manager.getAllRecipesFor(KubeJSBotania.CUSTOM_TERRA_PLATE_TYPE)) {
            if (recipe instanceof AgglomerationRecipe) {
                if (((AgglomerationRecipe) recipe).structureMatches(level, platePos, below, side, corner)) {
                    return Optional.of((AgglomerationRecipe) recipe);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 获取所有配方（注意：此方法只能在客户端或有世界时调用）
     */
    public static List<AgglomerationRecipe> getAllRecipes() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            RecipeManager manager = mc.level.getRecipeManager();
            return manager.getAllRecipesFor(KubeJSBotania.CUSTOM_TERRA_PLATE_TYPE)
                    .stream()
                    .filter(r -> r instanceof AgglomerationRecipe)
                    .map(r -> (AgglomerationRecipe) r)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * 检查是否存在任意配方匹配当前的多方块结构
     */
    public static boolean hasMatchingStructure(Level level, BlockPos platePos,
                                               BlockState below, BlockState side, BlockState corner) {
        return findByStructure(level, platePos, below, side, corner).isPresent();
    }

    /**
     * 同时检查结构和物品，返回匹配的配方
     */
    public static Optional<AgglomerationRecipe> findMatchingRecipe(Level level, BlockPos platePos,
                                                                   List<ItemStack> inputs,
                                                                   BlockState below, BlockState side, BlockState corner) {
        if (level == null) return Optional.empty();
        RecipeManager manager = level.getRecipeManager();
        for (var recipe : manager.getAllRecipesFor(KubeJSBotania.CUSTOM_TERRA_PLATE_TYPE)) {
            if (recipe instanceof  AgglomerationRecipe) {

                if (((AgglomerationRecipe) recipe).matches(level, platePos, inputs, below, side, corner)) {
                    return Optional.of((AgglomerationRecipe) recipe);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 检查某个物品是否被任何配方使用
     */
    public static boolean containsItem(Level level, ItemStack stack) {
        if (level == null) return false;
        RecipeManager manager = level.getRecipeManager();
        for (var recipe : manager.getAllRecipesFor(KubeJSBotania.CUSTOM_TERRA_PLATE_TYPE)) {
            if (recipe instanceof AgglomerationRecipe) {

                for (ItemStack recipeStack : ((AgglomerationRecipe) recipe).getRecipeStacks()) {
                    if (ItemHandlerHelper.canItemStacksStack(stack, recipeStack)) {
                        return true;
                    }
                }
                for (var tag : ((AgglomerationRecipe) recipe).getRecipeItemTags()) {
                    if (stack.is(tag)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 移除原有的 register、unregister、clear 方法，因为配方现在由原版系统管理
    // 如果仍需要用于缓存，可以保留，但不推荐
}