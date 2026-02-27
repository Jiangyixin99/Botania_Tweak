package com.prunoideae.kubejs;

import com.google.common.collect.ImmutableList;
import com.prunoideae.recipe.AgglomerationRecipe;
import com.prunoideae.schema.TerraPlateSchema;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AgglomerationRecipeJS extends RecipeJS {
    private static final Logger LOGGER = LoggerFactory.getLogger("BotaniaTweaks");

    @Override
    public Recipe<?> createRecipe() {
        // 1. 获取所有输出物品（多输出）
        OutputItem[] outputs = getValue(TerraPlateSchema.RESULTS);
        List<ItemStack> outputStacks = new ArrayList<>();
        for (OutputItem out : outputs) {
            outputStacks.add(out.item.copy());
        }

        // 2. 获取输入物品（包含物品和标签）
        InputItem[] inputs = getValue(TerraPlateSchema.INGREDIENTS);
        List<Object> inputList = new ArrayList<>(); // 用于 AgglomerationRecipe 构造器的输入列表（ItemStack 或 TagKey<Item>）
        for (InputItem in : inputs) {
            Ingredient ing = in.ingredient;
            ItemStack[] stacks = ing.getItems();
            // 尝试判断是否是标签（KubeJS 的 InputItem 可能无法直接获取 TagKey，这里简化处理）
            if (stacks.length == 1 && !stacks[0].isEmpty() && ing.isSimple()) {
                // 单个物品
                inputList.add(stacks[0].copy());
            } else {
                // 可能是标签，取第一个物品作为代表（不精确，但暂时如此）
                if (stacks.length > 0) {
                    LOGGER.warn("Tag-based input may not match correctly in recipe {}. Consider using explicit items.", this.id);
                    inputList.add(stacks[0].copy());
                } else {
                    inputList.add(ItemStack.EMPTY);
                }
                // 更精确的做法：如果需要支持标签，需要从 Ingredient 反向解析 TagKey，此处省略
            }
        }

        // 3. 获取魔力消耗
        int manaCost = getValue(TerraPlateSchema.MANA);

        // 4. 获取多方块结构方块
        BlockState center = parseBlockState(getValue(TerraPlateSchema.CENTER), null);
        BlockState edge = parseBlockState(getValue(TerraPlateSchema.EDGE), null);
        BlockState corner = parseBlockState(getValue(TerraPlateSchema.CORNER), null);
        BlockState centerReplace = parseBlockState(getValue(TerraPlateSchema.CENTER_REPLACE), null);
        BlockState edgeReplace = parseBlockState(getValue(TerraPlateSchema.EDGE_REPLACE), null);
        BlockState cornerReplace = parseBlockState(getValue(TerraPlateSchema.CORNER_REPLACE), null);

        // 使用 getOrCreateId() 获取或生成 ID
        ResourceLocation recipeId = getOrCreateId();

        // 检查 ID 是否有效（可选）
        if (recipeId == null) {
            throw new IllegalStateException("Failed to generate recipe ID");
        }
        // 5. 构建原版 AgglomerationRecipe 实例（注意参数顺序）
        return new AgglomerationRecipe(
                ImmutableList.copyOf(inputList),      // recipeInputs
                ImmutableList.copyOf(outputStacks),   // recipeOutputs
                manaCost,
                center,
                edge,
                corner,
                centerReplace,
                edgeReplace,
                cornerReplace,
                recipeId                             // 最后一个参数是 id
        );
    }
    private BlockState parseBlockState(String blockId, BlockState defaultValue) {
        if (blockId == null || blockId.isEmpty()) return defaultValue;
        ResourceLocation id = new ResourceLocation(blockId);
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block != null ? block.defaultBlockState() : defaultValue;
    }


        // 可选：添加日志以便调试
        // System.out.println("AgglomerationRecipeJS loaded with id: " + this.id);
    }
    // 可以保留 afterLoaded 但清空内容，或直接删除
//    @Override
//    public void afterLoaded() {
//        // 不再手动注册到 AgglomerationRecipes
//        super.afterLoaded();
//    }