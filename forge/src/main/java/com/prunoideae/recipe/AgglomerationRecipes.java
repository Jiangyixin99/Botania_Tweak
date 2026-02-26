package com.prunoideae.recipe;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgglomerationRecipes {
    public static final ArrayList<AgglomerationRecipe> RECIPES = new ArrayList<>();

    public static AgglomerationRecipe defaultRecipe;

    /**
     * 根据多方块结构查找配方（不检查物品）
     */
    public static Optional<AgglomerationRecipe> findByStructure(Level level, BlockPos platePos,
                                                                BlockState below, BlockState side, BlockState corner) {
        for (AgglomerationRecipe recipe : RECIPES) {
            if (recipe.structureMatches(level, platePos, below, side, corner)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }
    public static List<AgglomerationRecipe> getAllRecipes() {
        return RECIPES; // 假设你有一个静态列表 RECIPES
    }

    // 在 AgglomerationRecipes 类中
    public static void clear() {
        RECIPES.clear(); // 假设你的静态列表名为 RECIPES
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
        for (AgglomerationRecipe recipe : RECIPES) {
            if (recipe.matches(level, platePos, inputs, below, side, corner)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    /**
     * 检查某个物品是否被任何配方使用
     */
    public static boolean containsItem(ItemStack stack) {
        for (AgglomerationRecipe recipe : RECIPES) {
            for (ItemStack recipeStack : recipe.getRecipeStacks()) {
                if (ItemHandlerHelper.canItemStacksStack(stack, recipeStack)) {
                    return true;
                }
            }
            for (var tag : recipe.getRecipeItemTags()) {
                if (stack.is(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 初始化示例配方（应在 mod 初始化时调用）
     */
    public static void init() {

    }


    public static void register(AgglomerationRecipe recipe) {
        RECIPES.add(recipe);
    }

    public static void unregister(AgglomerationRecipe recipe) {
        RECIPES.remove(recipe);
    }
}