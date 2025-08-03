package com.fixbreakrude.fixbreakrude;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fixbreakrude.fixbreakrude.Config;

/**
 * Мод-патч для решения конфликта между Legendary Monsters и GregTech
 * Отключает проверку тегов Legendary Monsters для блоков GregTech
 */
@Mod(FixBreakRude.MOD_ID)
public class FixBreakRude {
    public static final String MOD_ID = "fixbreakrude";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public FixBreakRude() {
        LOGGER.info("FixBreakRude mod initialized!");
        
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Config.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Патч для отключения проверки тегов Legendary Monsters для блоков GregTech
     * Приоритет: HIGHEST - обрабатывается первым
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        // Проверяем, включен ли патч
        if (!Config.ENABLE_PATCH.get()) {
            return;
        }

        BlockState state = event.getTargetBlock();
        ItemStack heldItem = event.getEntity().getMainHandItem();
        
        // Проверяем, является ли блок блоком GregTech
        if (isGregTechBlock(state)) {
            // Проверяем, является ли инструмент инструментом GregTech
            if (isGregTechTool(heldItem)) {
                // Проверяем, нужно ли разрешить добычу (если она была запрещена)
                if (!event.canHarvest()) {
                    // Принудительно разрешаем добычу для GregTech инструментов
                    event.setCanHarvest(true);
                    
                    if (Config.LOG_ACTIONS.get()) {
                        LOGGER.info("FixBreakRude: Allowed GregTech tool to harvest GregTech block - {} -> {}", 
                                   heldItem.getDescriptionId(), state.getBlock().getDescriptionId());
                    }
                }
            }
        }
    }

    /**
     * Патч для восстановления правильной скорости добычи GregTech инструментов
     * Приоритет: HIGHEST - обрабатывается первым
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        // Проверяем, включен ли патч
        if (!Config.ENABLE_PATCH.get() || !Config.FIX_MINING_SPEED.get()) {
            return;
        }

        BlockState state = event.getState();
        ItemStack heldItem = event.getEntity().getMainHandItem();
        
        // Проверяем, является ли блок блоком GregTech
        if (isGregTechBlock(state)) {
            // Проверяем, является ли инструмент инструментом GregTech
            if (isGregTechTool(heldItem)) {
                // Восстанавливаем правильную скорость добычи для GregTech инструментов
                float originalSpeed = event.getOriginalSpeed();
                float newSpeed = event.getNewSpeed();
                
                // Если скорость была изменена (возможно, Legendary Monsters её изменил)
                if (Math.abs(newSpeed - originalSpeed) > 0.01f) {
                    // Восстанавливаем оригинальную скорость GregTech инструмента
                    event.setNewSpeed(originalSpeed);
                    
                    if (Config.LOG_ACTIONS.get()) {
                        LOGGER.info("FixBreakRude: Restored GregTech tool speed - {} -> {} (was: {})", 
                                   heldItem.getDescriptionId(), originalSpeed, newSpeed);
                    }
                }
            }
        }
    }

    /**
     * Дополнительный патч для полного отключения влияния Legendary Monsters на GregTech
     * Приоритет: LOWEST - обрабатывается последним, чтобы переопределить все изменения
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public void onBreakSpeedOverride(PlayerEvent.BreakSpeed event) {
        // Проверяем, включен ли патч
        if (!Config.ENABLE_PATCH.get() || !Config.FIX_MINING_SPEED.get()) {
            return;
        }

        BlockState state = event.getState();
        ItemStack heldItem = event.getEntity().getMainHandItem();
        
        // Проверяем, является ли блок блоком GregTech
        if (isGregTechBlock(state)) {
            // Проверяем, является ли инструмент инструментом GregTech
            if (isGregTechTool(heldItem)) {
                // Принудительно устанавливаем правильную скорость для GregTech инструментов
                float correctSpeed = getGregTechToolSpeed(heldItem, state);
                float currentSpeed = event.getNewSpeed();
                
                // Устанавливаем правильную скорость
                event.setNewSpeed(correctSpeed);
                
                // Логируем только если скорость действительно изменилась
                if (Config.DEBUG_MODE.get() && Math.abs(correctSpeed - currentSpeed) > 0.01f) {
                    LOGGER.debug("FixBreakRude: Forced GregTech tool speed - {} -> {} (was: {}) for block {}", 
                               heldItem.getDescriptionId(), correctSpeed, currentSpeed, state.getBlock().getDescriptionId());
                }
            }
        }
    }

    /**
     * Проверяет, является ли блок блоком GregTech
     */
    private boolean isGregTechBlock(BlockState state) {
        String blockId = state.getBlock().getDescriptionId();
        String blockClass = state.getBlock().getClass().getName();
        
        return blockId.contains("gtceu:") || 
               blockId.contains("gregtech:") ||
               blockClass.contains("gtceu") ||
               blockClass.contains("gregtech");
    }

    /**
     * Проверяет, является ли инструмент инструментом GregTech
     */
    private boolean isGregTechTool(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        
        String itemId = itemStack.getDescriptionId();
        String itemClass = itemStack.getItem().getClass().getName();
        
        return itemId.contains("gtceu:") ||
               itemId.contains("gregtech:") ||
               itemClass.contains("gtceu") ||
               itemClass.contains("gregtech") ||
               // Проверяем NBT теги GregTech
               (itemStack.hasTag() && itemStack.getTag().contains("GT.Tool"));
    }

    /**
     * Получает правильную скорость добычи для GregTech инструмента
     */
    private float getGregTechToolSpeed(ItemStack itemStack, BlockState state) {
        // Получаем базовую скорость инструмента
        float baseSpeed = itemStack.getDestroySpeed(state);
        
        // Если это GregTech инструмент, используем его собственную скорость
        if (isGregTechTool(itemStack)) {
            // Проверяем NBT теги GregTech для получения правильной скорости
            if (itemStack.hasTag() && itemStack.getTag().contains("GT.Tool")) {
                var toolTag = itemStack.getTag().getCompound("GT.Tool");
                if (toolTag.contains("ToolSpeed")) {
                    return toolTag.getFloat("ToolSpeed");
                }
            }
            
            // Если NBT нет, используем базовую скорость инструмента
            return Math.max(baseSpeed, 1.0f);
        }
        
        return baseSpeed;
    }
} 