package com.prunoideae;

import com.prunoideae.schema.*;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.mana.ManaItemHandler;

public class KubeJSBotaniaPlugin extends KubeJSPlugin {
    public KubeJSBotaniaPlugin() {
        System.out.println("[KubeJSBotaniaPlugin] Constructor called");
        // 可选：捕获异常并打印堆栈
        try {
            // 可能出错的代码（如依赖其他类）
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace("botania")
                .register("terra_plate", TerraPlateSchema.SCHEMA);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("ManaHandler", ManaItemHandler.instance());
        event.add("CorporeaHelper", CorporeaHelper.instance());
    }
}
