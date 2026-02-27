package com.prunoideae.schema;

import com.prunoideae.kubejs.AgglomerationRecipeJS;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
public interface TerraPlateSchema {
    RecipeKey<OutputItem[]> RESULTS = ItemComponents.OUTPUT_ARRAY.key("results");
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<Integer> MANA = NumberComponent.INT.key("mana");
    RecipeKey<String> CENTER = StringComponent.ANY.key("center");
    RecipeKey<String> EDGE = StringComponent.ANY.key("edge");
    RecipeKey<String> CORNER = StringComponent.ANY.key("corner");
    RecipeKey<String> CENTER_REPLACE = StringComponent.ANY.key("centerReplace");
    RecipeKey<String> EDGE_REPLACE = StringComponent.ANY.key("edgeReplace");
    RecipeKey<String> CORNER_REPLACE = StringComponent.ANY.key("cornerReplace");

    RecipeSchema SCHEMA = new RecipeSchema(AgglomerationRecipeJS.class, AgglomerationRecipeJS::new,
            RESULTS, INGREDIENTS, MANA, CENTER, EDGE, CORNER, CENTER_REPLACE, EDGE_REPLACE, CORNER_REPLACE);
}