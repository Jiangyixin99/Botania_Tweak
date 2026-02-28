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
        // 1. 获取所有输出物品（多输出）- 这部分不变
        OutputItem[] outputs = getValue(TerraPlateSchema.RESULTS);
        List<ItemStack> outputStacks = new ArrayList<>();
        for (OutputItem out : outputs) {
            outputStacks.add(out.item.copy());
        }

        // 2. 获取输入物品（包含物品和标签）- 需要修改
        InputItem[] inputs = getValue(TerraPlateSchema.INGREDIENTS);
        List<Object> inputList = new ArrayList<>();

        for (InputItem in : inputs) {
            // 从 InputItem 中获取数量
            int count = in.count;
            Ingredient ing = in.ingredient;

            // 判断是物品还是标签
            // KubeJS 的 InputItem 可以通过 ing 的类型来判断
            if (ing.isSimple()) {
                // 简单物品 - 从 Ingredient 中获取物品
                ItemStack[] stacks = ing.getItems();
                if (stacks.length > 0) {
                    ItemStack stack = stacks[0].copy();
                    stack.setCount(count);  // 设置数量！
                    inputList.add(stack);
                }
            } else {
                // 尝试解析标签
                // 这里需要更复杂的处理，因为 Ingredient 不能直接获取 TagKey
                // 一个可行的方法是检查 ing 的 JSON 表示
                var json = ing.toJson();
                if (json.isJsonObject() && json.getAsJsonObject().has("tag")) {
                    String tagId = json.getAsJsonObject().get("tag").getAsString();
                    ResourceLocation tagResource = ResourceLocation.parse(tagId);
                    TagKey<Item> tag = TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagResource);
                    inputList.add(tag);
                    // 注意：标签不能直接带数量，需要在 itemsMatch 中处理
                } else {
                    // 如果不是标签，退化为物品（带数量）
                    ItemStack[] stacks = ing.getItems();
                    if (stacks.length > 0) {
                        ItemStack stack = stacks[0].copy();
                        stack.setCount(count);
                        inputList.add(stack);
                        LOGGER.warn("Could not extract tag from ingredient in recipe {}, treating as item", this.id);
                    }
                }
            }
        }

        // 3. 获取魔力消耗
        int manaCost = getValue(TerraPlateSchema.MANA);

        // 4. 获取多方块结构方块 - 这部分不变
        BlockState center = parseBlockState(getValue(TerraPlateSchema.CENTER), null);
        BlockState edge = parseBlockState(getValue(TerraPlateSchema.EDGE), null);
        BlockState corner = parseBlockState(getValue(TerraPlateSchema.CORNER), null);
        BlockState centerReplace = parseBlockState(getValue(TerraPlateSchema.CENTER_REPLACE), null);
        BlockState edgeReplace = parseBlockState(getValue(TerraPlateSchema.EDGE_REPLACE), null);
        BlockState cornerReplace = parseBlockState(getValue(TerraPlateSchema.CORNER_REPLACE), null);

        ResourceLocation recipeId = getOrCreateId();
        if (recipeId == null) {
            throw new IllegalStateException("Failed to generate recipe ID");
        }

        return new AgglomerationRecipe(
                ImmutableList.copyOf(inputList),
                ImmutableList.copyOf(outputStacks),
                manaCost,
                center,
                edge,
                corner,
                centerReplace,
                edgeReplace,
                cornerReplace,
                recipeId
        );
    }
    private BlockState parseBlockState(String blockId, BlockState defaultValue) {
        if (blockId == null || blockId.isEmpty()) return defaultValue;
        ResourceLocation id =  ResourceLocation.parse(blockId);
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