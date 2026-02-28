package com.prunoideae.recipe;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class AgglomerationRecipeSerializer implements RecipeSerializer<AgglomerationRecipe> {
    public static final AgglomerationRecipeSerializer INSTANCE = new AgglomerationRecipeSerializer();

    private AgglomerationRecipeSerializer() {}

    @Override
    public AgglomerationRecipe fromJson(ResourceLocation id, JsonObject json) {
        // 解析 ingredients
        JsonArray ingredientsJson = GsonHelper.getAsJsonArray(json, "ingredients");
        List<Object> inputs = new ArrayList<>(); // 用于 AgglomerationRecipe 构造器的输入列表
        for (JsonElement e : ingredientsJson) {
            JsonObject obj = e.getAsJsonObject();
            if (obj.has("item")) {
                ItemStack stack = net.minecraftforge.common.crafting.CraftingHelper.getItemStack(obj, true);
                inputs.add(stack);
            } else if (obj.has("tag")) {
                ResourceLocation tagId = new ResourceLocation(GsonHelper.getAsString(obj, "tag"));
                TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                inputs.add(tag);
            }
        }

        // 解析 results
        JsonArray resultsJson = GsonHelper.getAsJsonArray(json, "results");
        List<ItemStack> outputs = new ArrayList<>();
        for (JsonElement e : resultsJson) {
            JsonObject obj = e.getAsJsonObject();
            ItemStack stack = net.minecraftforge.common.crafting.CraftingHelper.getItemStack(obj, true);
            outputs.add(stack);
        }

        int mana = GsonHelper.getAsInt(json, "mana", 500000);

        // 解析多方块结构
        BlockState center = parseBlockState(json, "center");
        BlockState edge = parseBlockState(json, "edge");
        BlockState corner = parseBlockState(json, "corner");
        BlockState centerReplace = parseBlockState(json, "centerReplace");
        BlockState edgeReplace = parseBlockState(json, "edgeReplace");
        BlockState cornerReplace = parseBlockState(json, "cornerReplace");

        return new AgglomerationRecipe(
                ImmutableList.copyOf(inputs),      // recipeInputs
                ImmutableList.copyOf(outputs),     // recipeOutputs
                mana,
                center,
                edge,
                corner,
                centerReplace,
                edgeReplace,
                cornerReplace,
                id
        );
    }

    private BlockState parseBlockState(JsonObject json, String key) {
        if (!json.has(key)) return null;
        String blockId = GsonHelper.getAsString(json, key);
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(blockId));
        return block != null ? block.defaultBlockState() : null;
    }

    @Override
    public AgglomerationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        int inputCount = buf.readVarInt();
        List<Object> inputs = new ArrayList<>();
        for (int i = 0; i < inputCount; i++) {
            boolean isItem = buf.readBoolean();
            if (isItem) {
                inputs.add(buf.readItem());
            } else {
                ResourceLocation tagId = buf.readResourceLocation();
                inputs.add(TagKey.create(Registries.ITEM, tagId));
            }
        }
        int outputCount = buf.readVarInt();
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < outputCount; i++) {
            outputs.add(buf.readItem());
        }
        int mana = buf.readInt();
        // 读取多方块状态（简化为读方块ID，用默认状态）
        BlockState center = readBlockState(buf);
        BlockState edge = readBlockState(buf);
        BlockState corner = readBlockState(buf);
        BlockState centerReplace = readBlockState(buf);
        BlockState edgeReplace = readBlockState(buf);
        BlockState cornerReplace = readBlockState(buf);

        return new AgglomerationRecipe(
                ImmutableList.copyOf(inputs),      // recipeInputs
                ImmutableList.copyOf(outputs),     // recipeOutputs
                mana,
                center,
                edge,
                corner,
                centerReplace,
                edgeReplace,
                cornerReplace,
                id
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, AgglomerationRecipe recipe) {
        buf.writeVarInt(recipe.recipeStacks.size() + recipe.recipeItemTags.size());
        for (ItemStack stack : recipe.recipeStacks) {
            buf.writeBoolean(true);
            buf.writeItem(stack);
        }
        for (TagKey<Item> tag : recipe.recipeItemTags) {
            buf.writeBoolean(false);
            buf.writeResourceLocation(tag.location());
        }
        buf.writeVarInt(recipe.recipeOutputs.size());
        for (ItemStack stack : recipe.recipeOutputs) {
            buf.writeItem(stack);
        }
        buf.writeInt(recipe.manaCost);
        writeBlockState(buf, recipe.multiblockCenter);
        writeBlockState(buf, recipe.multiblockEdge);
        writeBlockState(buf, recipe.multiblockCorner);
        writeBlockState(buf, recipe.multiblockCenterReplace);
        writeBlockState(buf, recipe.multiblockEdgeReplace);
        writeBlockState(buf, recipe.multiblockCornerReplace);
    }

    private void writeBlockState(FriendlyByteBuf buf, BlockState state) {
        if (state == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        }
    }

    private BlockState readBlockState(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) return null;
        ResourceLocation id = buf.readResourceLocation();
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block != null ? block.defaultBlockState() : null;
    }
}