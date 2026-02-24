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

//        defaultRecipe = new AgglomerationRecipe(
//                ImmutableList.of(
//                        new ItemStack(BotaniaItems.manaDiamond),
//                        new ItemStack(BotaniaItems.manaSteel),
//                        new ItemStack(BotaniaItems.manaPearl)
//                ),
//                new ItemStack(BotaniaItems.terrasteel),
//                500_000,
//                BotaniaBlocks.livingrock.defaultBlockState(),
//                Blocks.LAPIS_BLOCK.defaultBlockState(),
//                BotaniaBlocks.livingrock.defaultBlockState(),
//                null, null, null
//        );
//
//        register(defaultRecipe);
        AgglomerationRecipe reinforcedManaSteel = new AgglomerationRecipe(
                ImmutableList.of(
                        new ItemStack(BotaniaItems.manaSteel),          // 魔力钢锭
                        new ItemStack(Blocks.IRON_BLOCK),               // 铁块
                        new ItemStack(Blocks.GOLD_BLOCK),
                        new ItemStack(Items.APPLE)// 金块
                ),
                ImmutableList.of(
                        new ItemStack(BotaniaItems.manaSteel)       // 魔力钢锭
                                      // 金块
                ),            // 输出：泰拉钢（示例，可自定义）
                800_000,                                             // 魔力消耗                 // 深橙色
                BotaniaBlocks.livingrock.defaultBlockState(),       // 中心：活岩石
                Blocks.DIAMOND_BLOCK.defaultBlockState(),           // 边缘：钻石块
                Blocks.WATER.defaultBlockState(),              // 角落：金块
                Blocks.WATER.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState()
                // 不替换
        );

        register(reinforcedManaSteel);

        AgglomerationRecipe replaceExample = new AgglomerationRecipe(
                ImmutableList.of(
                        new ItemStack(BotaniaItems.manaDiamond), // 魔力钻石
                        new ItemStack(Items.OAK_LOG),   // 魔力珍珠,   // 魔力钢
                        new ItemStack(Items.OAK_LOG)   // 魔力珍珠
                ),
                ImmutableList.of(
                        new ItemStack(BotaniaItems.terrasteel), // 主输出
                        new ItemStack(BotaniaItems.manaDiamond),
                        new ItemStack(Items.OAK_PLANKS)// 魔力钻石不消耗，返还
                ),     // 输出：泰拉钢
                500_000,                                          // 魔力消耗// 绿色光束
                // 所需的多方块结构
                BotaniaBlocks.livingrock.defaultBlockState(),     // 中心：活岩石
                Blocks.DIAMOND_BLOCK.defaultBlockState(),         // 边缘：钻石块
                Blocks.GOLD_BLOCK.defaultBlockState(),            // 角落：金块
                // 合成后替换成的方块（null 表示不替换）
                Blocks.OBSIDIAN.defaultBlockState(),              // 中心替换为黑曜石
                Blocks.WATER.defaultBlockState(),              // 边缘替换为玻璃
                Blocks.SAND.defaultBlockState()          // 角落替换为海晶灯
        );
        register(replaceExample);
    }


    public static void register(AgglomerationRecipe recipe) {
        RECIPES.add(recipe);
    }

    public static void unregister(AgglomerationRecipe recipe) {
        RECIPES.remove(recipe);
    }
}