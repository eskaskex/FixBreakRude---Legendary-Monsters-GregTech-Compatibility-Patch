package com.fixbreakrude.fixbreakrude;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Конфигурация для мода-патча FixBreakRude
 */
public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Основные настройки
    public static final ForgeConfigSpec.BooleanValue ENABLE_PATCH;
    public static final ForgeConfigSpec.BooleanValue LOG_ACTIONS;
    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE;
    public static final ForgeConfigSpec.BooleanValue FIX_MINING_SPEED;

    static {
        BUILDER.push("FixBreakRude Configuration");

        ENABLE_PATCH = BUILDER
                .comment("Включить патч для совместимости Legendary Monsters и GregTech")
                .define("enablePatch", true);

        LOG_ACTIONS = BUILDER
                .comment("Логировать действия патча")
                .define("logActions", false);

        DEBUG_MODE = BUILDER
                .comment("Режим отладки (дополнительная информация)")
                .define("debugMode", false);

        FIX_MINING_SPEED = BUILDER
                .comment("Исправлять скорость добычи для GregTech инструментов")
                .define("fixMiningSpeed", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
} 