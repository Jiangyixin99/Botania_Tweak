package com.prunoideae.modules.botania.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class BotaniaConfig {
    // 凝聚板相关
    public static ForgeConfigSpec.BooleanValue PROCESS_CUSTOM_AGGLO_STACKS;
    public static ForgeConfigSpec.IntValue ADVANCED_CRATE_MANA_PER_ITEM;
    // 非GOG模式水碗功能开关（原为 IntValue，现更正为 BooleanValue）
    public static ForgeConfigSpec.BooleanValue NON_GOG_WATER_BOWL;
    // ... 可根据需要添加其他配置项

    // 公共配置（可同时作用于客户端和服务器，根据实际需求也可分离）
    public static class Common {
        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("balance");
            // 平衡性配置（如需要可在此添加）
            builder.pop();

            builder.push("etc");
            PROCESS_CUSTOM_AGGLO_STACKS = builder
                    .comment("If true, the custom terrestrial agglomeration plate will try to collect items on top of it, resolving issues related to stack sizes. This operation can be expensive; disable if not needed.")
                    .define("doCustomAgglomerationPreprocessing", true);

            NON_GOG_WATER_BOWL = builder
                    .comment("If true, allows the water bowl mechanic (right-click water with a bowl to obtain a water bowl) to work outside of Garden of Glass mode.")
                    .define("nonGogWaterBowl", false);

            // 高级板条箱每物品魔力消耗（保留以备将来使用）
            ADVANCED_CRATE_MANA_PER_ITEM = builder
                    .comment("How much mana does the crafty crate use per item in the recipe? (Default: 160, one burst from a redstone spreader)")
                    .defineInRange("crateManaPerItem", 160, 1, Integer.MAX_VALUE);

            builder.pop();

            builder.push("fixes");
            // 修复选项（如需要可添加）
            builder.pop();
        }
    }

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }
}