package com.prunoideae.modules.jei;

import com.prunoideae.KubeJSBotania;
import com.prunoideae.recipe.AgglomerationRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.prunoideae.recipe.AgglomerationRecipes;
import net.minecraft.world.item.crafting.RecipeManager;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class BotaniaKubeJSJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.parse("botaniatweaks:jei_plugin");
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            RecipeManager recipeManager = mc.level.getRecipeManager();
            List<AgglomerationRecipe> recipes = recipeManager.getAllRecipesFor(KubeJSBotania.CUSTOM_TERRA_PLATE_TYPE)
                    .stream()
                    .filter(r -> r instanceof AgglomerationRecipe)
                    .map(r -> (AgglomerationRecipe) r)
                    .collect(Collectors.toList());
            registration.addRecipes(RecipeCategoryCustomAgglomeration.RECIPE_TYPE, recipes);
        }
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