package com.fixbreakrude.fixbreakrude;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraft.world.item.Tier;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.List;

/**
 * РАДИКАЛЬНОЕ РЕШЕНИЕ: Полностью отключает систему тегов Legendary Monsters
 * Вместо перехвата каждого события - удаляет теги из TierSortingRegistry
 */
@Mod(FixBreakRude.MOD_ID)
public class FixBreakRude {
    public static final String MOD_ID = "fixbreakrude";
    private static final Logger LOGGER = LogManager.getLogger();

    public FixBreakRude() {
        // Регистрируем конфигурацию
        Config.register();
        
        // Регистрируем обработчики событий
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("FixBreakRude RADICAL - Полное отключение Legendary Monsters загружено!");
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (Config.ENABLE_PATCH.get()) {
                disableLegendaryMonstersSystem();
                if (Config.LOG_ACTIONS.get()) {
                    LOGGER.info("FixBreakRude RADICAL: Система Legendary Monsters полностью отключена");
                }
            }
        });
    }

    /**
     * ПОЛНОСТЬЮ ОТКЛЮЧАЕТ систему тегов Legendary Monsters
     * Удаляет теги из TierSortingRegistry, чтобы они вообще не проверялись
     */
    private void disableLegendaryMonstersSystem() {
        try {
            // Получаем все зарегистрированные теги
            List<Tier> sortedTiers = TierSortingRegistry.getSortedTiers();
            
            LOGGER.info("FixBreakRude RADICAL: Найдено {} тегов в TierSortingRegistry", sortedTiers.size());
            
            // Удаляем теги Legendary Monsters из TierSortingRegistry
            int removedCount = 0;
            for (Tier tier : sortedTiers) {
                if (isLegendaryMonstersTier(tier)) {
                    // Пытаемся удалить тег из реестра
                    if (removeTierFromRegistry(tier)) {
                        removedCount++;
                        LOGGER.info("FixBreakRude RADICAL: Удален тег Legendary Monsters: {}", tier);
                    }
                }
            }
            
            LOGGER.info("FixBreakRude RADICAL: Удалено {} тегов Legendary Monsters", removedCount);
            
        } catch (Exception e) {
            LOGGER.error("FixBreakRude RADICAL: Ошибка при отключении системы Legendary Monsters: {}", e.getMessage());
        }
    }

    /**
     * Проверяет, является ли тег тегом Legendary Monsters
     */
    private boolean isLegendaryMonstersTier(Tier tier) {
        try {
            String tierName = tier.toString().toLowerCase();
            return tierName.contains("dinosaur_bone") || 
                   tierName.contains("void") || 
                   tierName.contains("bronzite") || 
                   tierName.contains("lightning") ||
                   tierName.contains("legendary_monsters");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Удаляет тег из TierSortingRegistry через рефлексию
     */
    private boolean removeTierFromRegistry(Tier tier) {
        try {
            // Используем рефлексию для доступа к внутренним полям TierSortingRegistry
            Field tiersField = TierSortingRegistry.class.getDeclaredField("sortedTiers");
            tiersField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            List<Tier> tiers = (List<Tier>) tiersField.get(null);
            
            if (tiers != null && tiers.contains(tier)) {
                boolean removed = tiers.remove(tier);
                if (removed) {
                    LOGGER.debug("FixBreakRude RADICAL: Тег успешно удален из реестра: {}", tier);
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("FixBreakRude RADICAL: Не удалось удалить тег {}: {}", tier, e.getMessage());
        }
        return false;
    }

    /**
     * РЕЗЕРВНЫЙ ПАТЧ - используется только если основное отключение не сработало
     * Принудительно разрешает добычу GregTech блоков GregTech инструментами
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (!Config.ENABLE_PATCH.get()) return;
        
        BlockState state = event.getTargetBlock();
        ItemStack heldItem = event.getEntity().getMainHandItem();
        
        if (isGregTechBlock(state) && isGregTechTool(heldItem)) {
            // Принудительно разрешаем добычу
            event.setCanHarvest(true);
            
            if (Config.LOG_ACTIONS.get()) {
                LOGGER.info("FixBreakRude RADICAL: Разрешена добыча блока {} киркой GregTech", 
                    ForgeRegistries.BLOCKS.getKey(state.getBlock()));
            }
        }
    }

    /**
     * РЕЗЕРВНЫЙ ПАТЧ - используется только если основное отключение не сработало
     * Восстанавливает правильную скорость добычи для GregTech инструментов
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!Config.ENABLE_PATCH.get() || !Config.FIX_MINING_SPEED.get()) return;
        
        BlockState state = event.getState();
        ItemStack heldItem = event.getEntity().getMainHandItem();
        
        if (isGregTechBlock(state) && isGregTechTool(heldItem)) {
            // Принудительно устанавливаем правильную скорость GregTech
            float correctSpeed = getGregTechToolSpeed(heldItem, state);
            event.setNewSpeed(correctSpeed);
            
            if (Config.LOG_ACTIONS.get()) {
                LOGGER.info("FixBreakRude RADICAL: Установлена скорость добычи для блока {}: {}", 
                    ForgeRegistries.BLOCKS.getKey(state.getBlock()), correctSpeed);
            }
        }
    }

    /**
     * Проверяет, является ли блок блоком GregTech
     */
    private boolean isGregTechBlock(BlockState state) {
        if (state == null) return false;
        
        String blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString().toLowerCase();
        String blockClass = state.getBlock().getClass().getName().toLowerCase();
        
        return blockId.contains("gtceu") || blockId.contains("gregtech") || 
               blockClass.contains("gtceu") || blockClass.contains("gregtech");
    }

    /**
     * Проверяет, является ли инструмент инструментом GregTech
     */
    private boolean isGregTechTool(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        
        String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString().toLowerCase();
        String itemClass = itemStack.getItem().getClass().getName().toLowerCase();
        
        // Проверяем NBT тег GregTech
        if (itemStack.hasTag() && itemStack.getTag().contains("GT.Tool")) {
            return true;
        }
        
        return itemId.contains("gtceu") || itemId.contains("gregtech") || 
               itemClass.contains("gtceu") || itemClass.contains("gregtech");
    }

    /**
     * Получает правильную скорость инструмента GregTech из NBT
     */
    private float getGregTechToolSpeed(ItemStack itemStack, BlockState state) {
        if (itemStack.hasTag() && itemStack.getTag().contains("GT.Tool")) {
            var gtTool = itemStack.getTag().getCompound("GT.Tool");
            if (gtTool.contains("ToolSpeed")) {
                return gtTool.getFloat("ToolSpeed");
            }
        }
        
        // Возвращаем стандартную скорость разрушения
        return itemStack.getDestroySpeed(state);
    }
} 