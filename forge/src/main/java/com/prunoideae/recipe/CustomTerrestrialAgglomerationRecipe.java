package com.prunoideae.recipe;

import com.prunoideae.KubeJSBotania;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;

import java.util.ArrayList;
import java.util.List;

public class CustomTerrestrialAgglomerationRecipe implements TerrestrialAgglomerationRecipe {
    private final AgglomerationRecipe delegate;
    private final ResourceLocation id;

    public CustomTerrestrialAgglomerationRecipe(AgglomerationRecipe delegate, ResourceLocation id) {
        this.delegate = delegate;
        this.id = id;
    }

    public AgglomerationRecipe getDelegate() {
        return delegate;
    }
    @Override
    public int getMana() {
        return delegate.getManaCost();
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) inputs.add(stack);
        }
        return delegate.itemsMatch(inputs);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess reg) {
        return delegate.getPrimaryOutputCopy();  // 使用第一个输出
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess reg) {
        return delegate.getPrimaryOutputCopy();  // 使用第一个输出
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack stack : delegate.recipeStacks) {
            ingredients.add(Ingredient.of(stack));
        }
        for (var tag : delegate.recipeItemTags) {
            ingredients.add(Ingredient.of(tag));
        }
        return ingredients;
    }

    @Override
    public @NotNull String getGroup() {
        return "";
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(BotaniaBlocks.terraPlate);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return null; // 如果不需要序列化可以返回 null
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return KubeJSBotania.CUSTOM_TERRA_PLATE_TYPE;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public boolean isIncomplete() {
        return false;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull Container container) {
        return NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
    }
}