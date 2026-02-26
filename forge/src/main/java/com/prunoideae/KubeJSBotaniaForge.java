package com.prunoideae;




import com.prunoideae.modules.botania.config.BotaniaConfig;
import com.prunoideae.modules.botania.net.PacketCustomTerraPlate;
import com.prunoideae.recipe.AgglomerationRecipes;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(KubeJSBotania.MOD_ID)
public class KubeJSBotaniaForge {
    public KubeJSBotaniaForge() {
        // 注册 Architectury 事件总线
        EventBuses.registerModEventBus(KubeJSBotania.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        KubeJSBotania.init();

        // 注册配置
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BotaniaConfig.COMMON_SPEC);

        // 获取 MOD 事件总线，用于注册 FML 生命周期事件
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);

        // 注册 Forge 事件总线上的处理器（如右击事件）
    }
    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketCustomTerraPlate.register();
            AgglomerationRecipes.init(); // 加载默认配方
        });
    }
}