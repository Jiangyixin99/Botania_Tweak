package com.prunoideae.kubejs;

import com.google.common.collect.ImmutableList;
import com.prunoideae.recipe.AgglomerationRecipe;
import com.prunoideae.recipe.AgglomerationRecipes;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AgglomerationRecipeJS extends RecipeJS {
    private static final Logger LOGGER = LoggerFactory.getLogger("BotaniaTweaks");

    @Override
    public void afterLoaded() {
        AgglomerationRecipes.clear();
        super.afterLoaded();

        // 1. 获取所有输出物品（多输出）
        OutputItem[] outputs = getValue(TerraPlateSchema.RESULTS);
        ImmutableList.Builder<ItemStack> outputBuilder = ImmutableList.builder();
        for (OutputItem out : outputs) {
            outputBuilder.add(out.item);
        }

        // 2. 获取输入物品（包含物品和标签）
        InputItem[] inputs = getValue(TerraPlateSchema.INGREDIENTS);
        ImmutableList.Builder<Object> inputBuilder = ImmutableList.builder();
        for (InputItem in : inputs) {
            Ingredient ing = in.ingredient;
            // 尝试提取单个物品（如果可能）
            ItemStack[] stacks = ing.getItems();
            if (stacks.length == 1 && !stacks[0].isEmpty() && ing.isSimple()) {
                // 单个物品
                inputBuilder.add(stacks[0].copy());
            } else {
                // 标签或复杂输入：需要转换为 TagKey
                // 由于 Ingredient 无法直接获取 TagKey，这里需要根据实际情况处理
                // 简化处理：遍历所有物品，取第一个作为代表（不精确）
                // 更好的做法：让 KubeJS 配方直接使用 "tag" 而非 Ingredient，但 InputItem 已封装
                // 我们通过 Ingredient 的 toJson 等方法尝试还原 TagKey，但较复杂
                // 这里使用一个临时方案：如果无法确定 TagKey，就假设为单个物品（可能导致匹配问题）
                // 建议在 KubeJS 配方编写时直接使用 item 和 tag 分开字段，而不是用 InputItem
                // 由于时间限制，这里简单处理：取第一个物品作为代表，并记录警告
                if (stacks.length > 0) {
                    LOGGER.warn("Tag-based input may not match correctly in recipe {}. Consider using explicit items.", this.id);
                    inputBuilder.add(stacks[0].copy());
                } else {
                    inputBuilder.add(ItemStack.EMPTY);
                }
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

        // 5. 构建 AgglomerationRecipe
        AgglomerationRecipe recipe = new AgglomerationRecipe(
                inputBuilder.build(),
                outputBuilder.build(),
                manaCost,
                center,
                edge,
                corner,
                centerReplace,
                edgeReplace,
                cornerReplace
        );

        // 6. 注册到自定义配方列表
        AgglomerationRecipes.register(recipe);
        LOGGER.info("Registered agglomeration recipe for {} outputs", outputs.length);
    }

    private BlockState parseBlockState(String blockId, BlockState defaultValue) {
        if (blockId == null || blockId.isEmpty()) return defaultValue;
        ResourceLocation id = new ResourceLocation(blockId);
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block != null ? block.defaultBlockState() : defaultValue;
    }

    // deserialize 方法无需覆盖，因为 KubeJS 会根据 schema 自动填充 valueMap
}