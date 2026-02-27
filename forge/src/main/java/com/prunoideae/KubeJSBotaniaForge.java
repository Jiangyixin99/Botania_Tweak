package com.prunoideae;

import com.prunoideae.modules.botania.config.BotaniaConfig;
import com.prunoideae.modules.botania.net.PacketCustomTerraPlate;
import com.prunoideae.recipe.AgglomerationRecipe;
import com.prunoideae.recipe.AgglomerationRecipeSerializer;
import com.prunoideae.recipe.AgglomerationRecipes;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(KubeJSBotania.MOD_ID)
public class KubeJSBotaniaForge {
    public static final String MOD_ID = "kubejs_botania";

    // 注册 RecipeSerializer
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

    public static final RegistryObject<RecipeSerializer<AgglomerationRecipe>> TERRA_PLATE_SERIALIZER =
            SERIALIZERS.register("terra_plate", () -> AgglomerationRecipeSerializer.INSTANCE);

    public KubeJSBotaniaForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册 Architectury 事件总线
        EventBuses.registerModEventBus(KubeJSBotania.MOD_ID, modBus);

        // 注册 RecipeSerializer
        SERIALIZERS.register(modBus);

        KubeJSBotania.init();

        // 注册配置
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BotaniaConfig.COMMON_SPEC);

        // 注册 FML 生命周期事件
        modBus.addListener(this::commonSetup);

        // 注册 Forge 事件总线（如果需要）
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketCustomTerraPlate.register();
            // AgglomerationRecipes.init() 现在可以为空，因为配方由 KubeJS 添加
        });
    }
}