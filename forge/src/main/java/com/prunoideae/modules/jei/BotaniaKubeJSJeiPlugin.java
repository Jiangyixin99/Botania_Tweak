package com.prunoideae.modules.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.prunoideae.recipe.AgglomerationRecipes;
import vazkii.botania.common.block.BotaniaBlocks;
@JeiPlugin
public class BotaniaKubeJSJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation("botaniatweaks", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new RecipeCategoryCustomAgglomeration(registration.getJeiHelpers().getGuiHelper())
        );
    }

    // 注意：参数是 IRecipeRegistration
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 注册配方列表
        registration.addRecipes(
                RecipeCategoryCustomAgglomeration.RECIPE_TYPE,
                AgglomerationRecipes.RECIPES
        );
    }

    // 单独注册催化剂
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(BotaniaBlocks.terraPlate),
                RecipeCategoryCustomAgglomeration.RECIPE_TYPE
        );
    }
}