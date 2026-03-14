package com.prunoideae;

import com.google.gson.Gson;
import com.prunoideae.recipe.AgglomerationRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public class KubeJSBotania {
    public static final String MOD_ID = "kubejs_botania";
    public static final Gson GSON = new Gson();
    public static void init() {

    }
    // 自定义聚合配方的 RecipeType
    public static final ResourceLocation CUSTOM_TERRA_PLATE_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_terra_plate");
    public static final RecipeType<AgglomerationRecipe> CUSTOM_TERRA_PLATE_TYPE =
            RecipeType.simple(CUSTOM_TERRA_PLATE_ID);
}