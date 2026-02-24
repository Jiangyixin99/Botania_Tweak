package com.prunoideae.schema;

import com.prunoideae.kubejs.AgglomerationRecipeJS;import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface TerraPlateSchema {
    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT.key("result");
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<Integer> MANA = NumberComponent.INT.key("mana").optional(500000);
    RecipeKey<String> CENTER = StringComponent.ANY.key("center").optional("botania:livingrock");
    RecipeKey<String> EDGE = StringComponent.ANY.key("edge").optional("minecraft:lapis_block");
    RecipeKey<String> CORNER = StringComponent.ANY.key("corner").optional("botania:livingrock");
    RecipeKey<String> CENTER_REPLACE = StringComponent.ANY.key("centerReplace").optional("");
    RecipeKey<String> EDGE_REPLACE = StringComponent.ANY.key("edgeReplace").optional("");
    RecipeKey<String> CORNER_REPLACE = StringComponent.ANY.key("cornerReplace").optional("");

    RecipeSchema SCHEMA = new RecipeSchema(AgglomerationRecipeJS.class, AgglomerationRecipeJS::new,
            RESULT, INGREDIENTS, MANA, CENTER, EDGE, CORNER, CENTER_REPLACE, EDGE_REPLACE, CORNER_REPLACE);
}