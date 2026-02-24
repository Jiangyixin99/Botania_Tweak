package com.prunoideae.kubejs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.prunoideae.recipe.AgglomerationRecipe;
import com.prunoideae.recipe.AgglomerationRecipes;
import com.prunoideae.schema.TerraPlateSchema;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.ArrayList;
import java.util.List;

import static com.prunoideae.schema.TerraPlateSchema.RESULT;

public class AgglomerationRecipeJS extends RecipeJS {
    private static final Logger LOGGER = LoggerFactory.getLogger("BotaniaTweaks");

    // 手动解析的输入列表（混合 ItemStack 和 TagKey）
    private final List<Object> inputs = new ArrayList<>();
    private final List<Object> output = new ArrayList<>();
    private int manaCost = 500_000;
    private BlockState center;
    private BlockState edge;
    private BlockState corner;
    private BlockState centerReplace;
    private BlockState edgeReplace;
    private BlockState cornerReplace;

    @Override
    public void deserialize(boolean merge) {
        super.deserialize(merge);

        JsonObject json = this.json.getAsJsonObject();

        // 1. 解析输出并设置到 valueMap
        JsonElement resultEl = json.get("result");
        ItemStack resultStack = parseItemStack(resultEl);
        if (resultStack.isEmpty()) {
            throw new IllegalArgumentException("Failed to parse result item: " + resultEl);
        }
        setValue(RESULT, OutputItem.of(resultStack));
        LOGGER.info("After setValue, result value = {}", getValue(RESULT)); // 立即读取并打印

        // 2. 解析输入并保存到 inputs 列表，同时构造 InputItem[] 用于 valueMap
        JsonArray inputsArray = json.getAsJsonArray("ingredients");
        List<InputItem> inputItemsList = new ArrayList<>();
        for (JsonElement el : inputsArray) {
            if (el.isJsonObject() && el.getAsJsonObject().has("tag")) {
                ResourceLocation tagId = new ResourceLocation(el.getAsJsonObject().get("tag").getAsString());
                TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
                inputs.add(tag); // 保存标签用于最终配方
                inputItemsList.add(InputItem.of(Ingredient.of(tag))); // 转换为 InputItem 用于 valueMap
            } else {
                ItemStack stack = parseItemStack(el);
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Failed to parse input item: " + el);
                }
                inputs.add(stack); // 保存物品用于最终配方
                inputItemsList.add(InputItem.of(stack)); // 转换为 InputItem 用于 valueMap
            }
        }
        setValue(TerraPlateSchema.INGREDIENTS, inputItemsList.toArray(new InputItem[0]));

        // 3. 解析可选参数并设置到 valueMap（如有）
        if (json.has("mana")) {
            int mana = json.get("mana").getAsInt();
            setValue(TerraPlateSchema.MANA, mana);
            this.manaCost = mana;
        }

        // 4. 多方块方块（不设置到 valueMap，但保留字段）
        center = parseBlockState(json, "center", BotaniaBlocks.livingrock.defaultBlockState());
        edge = parseBlockState(json, "edge", Blocks.LAPIS_BLOCK.defaultBlockState());
        corner = parseBlockState(json, "corner", BotaniaBlocks.livingrock.defaultBlockState());

        // 5. 替换方块（可为 null）
        centerReplace = parseBlockState(json, "centerReplace", null);
        edgeReplace = parseBlockState(json, "edgeReplace", null);
        cornerReplace = parseBlockState(json, "cornerReplace", null);
    }

    @Override
    public void afterLoaded() {
        super.afterLoaded(); // 此时 valueMap 中已有有效值，不会报错

        // 从 valueMap 获取输出（可选）
        OutputItem outputItem = getValue(RESULT);

        // 使用手动解析的 inputs 构建最终配方
        ImmutableList.Builder<Object> inputBuilder = ImmutableList.builder();
        for (Object obj : inputs) {
            inputBuilder.add(obj);
        }
        ImmutableList.Builder<Object> outputBuilder = ImmutableList.builder();
        for (Object obj : output) {
            inputBuilder.add(obj);
        }
        AgglomerationRecipe recipe = new AgglomerationRecipe(
                inputBuilder.build(),
                outputBuilder.build(), // 单个输出包装为列表
                manaCost,
                center,
                edge,
                corner,
                centerReplace,
                edgeReplace,
                cornerReplace
        );
        AgglomerationRecipes.register(recipe);
        LOGGER.info("Registered agglomeration recipe for {}", outputItem.item);
    }

    private ItemStack parseItemStack(JsonElement el) {
        return ItemStackJS.of(el);
    }

    private BlockState parseBlockState(JsonObject json, String key, BlockState defaultValue) {
        if (!json.has(key) || json.get(key).isJsonNull()) return defaultValue;
        JsonElement el = json.get(key);
        if (el.isJsonPrimitive()) {
            ResourceLocation id = new ResourceLocation(el.getAsString());
            Block block = BuiltInRegistries.BLOCK.get(id);
            return block != null ? block.defaultBlockState() : defaultValue;
        }
        return defaultValue;
    }

    private int parseColor(JsonElement el) {
        String s = el.getAsString();
        if (s.startsWith("0x") || s.startsWith("0X")) {
            return Integer.parseInt(s.substring(2), 16);
        } else if (s.startsWith("#")) {
            return Integer.parseInt(s.substring(1), 16);
        } else {
            return Integer.parseInt(s, 16);
        }
    }
}