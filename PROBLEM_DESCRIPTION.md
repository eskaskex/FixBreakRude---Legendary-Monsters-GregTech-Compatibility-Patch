# Проблема совместимости: Legendary Monsters & GregTech

## 🚨 Описание проблемы

При установке мода **Legendary Monsters** вместе с **GregTech Community Edition Unofficial (GTCEu)** возникает критическая проблема совместимости, которая делает невозможным использование кирок GregTech для добычи блоков GregTech.

## ❌ Симптомы проблемы

### 1. Невозможность добычи блоков
- **Кирки GregTech не могут добывать блоки GregTech**
- Система определяет `Can Harvest: false` для всех рудных блоков GregTech
- Проблема затрагивает все типы руд: железная, медная, золотая, редкие минералы и т.д.

### 2. Неправильная скорость добычи
- **Кирки работают медленнее, чем должны**
- Скорость добычи не соответствует материалу кирки
- Проблема влияет на все инструменты GregTech

### 3. Ошибки в логах
```
Can Harvest: false
Block Requires Correct Tool: true
Block Class: com.gregtechceu.gtceu.api.block.OreBlock
```

## 🔍 Технический анализ проблемы

### Корень проблемы

**Legendary Monsters** регистрирует свои собственные теги инструментов в системе Minecraft:

```java
// Legendary Monsters - ModToolTiers.java
public static final Tier DINOSAUR_BONE = TierSortingRegistry.registerTier(
    new ForgeTier(5, 700, 0.5F, 2.0F, 25, 
    ModTags.Blocks.NEEDS_DINOSAUR_BONE_TOOL, 
    () -> Ingredient.of(ModItems.DINOSAUR_BONE.get())), 
    new ResourceLocation("legendary_monsters", "dinosaur_bone"), 
    List.of(Tiers.NETHERITE), List.of());

public static final Tier VOID = TierSortingRegistry.registerTier(
    new ForgeTier(5, 1100, 0.5F, 2.0F, 25, 
    ModTags.Blocks.NEEDS_VOID_TOOL, 
    () -> Ingredient.of(ModItems.VOID_GEM.get())), 
    new ResourceLocation("legendary_monsters", "void"), 
    List.of(Tiers.NETHERITE), List.of());
```

### Конфликт систем

1. **GregTech использует `TierSortingRegistry`** для определения правильных инструментов
2. **Legendary Monsters регистрирует свои теги** в той же системе
3. **Конфликт возникает** когда система проверяет совместимость инструментов

### Механизм конфликта

```java
// GregTech - ToolHelper.java
public static boolean isToolEffective(BlockState state, Set<GTToolType> toolClasses, int harvestLevel) {
    if (toolClasses.stream().anyMatch(type -> type.harvestTags.stream().anyMatch(state::is))) {
        return isCorrectTierForDrops(state, harvestLevel); // ← Проблема здесь
    }
    return false;
}

public static boolean isCorrectTierForDrops(BlockState state, int tier) {
    return TierSortingRegistry.isCorrectTierForDrops(getTier(tier), state); // ← Конфликт
}
```

### Почему это происходит

1. **GregTech блоки** требуют правильный инструмент (`requiresCorrectToolForDrops()`)
2. **Legendary Monsters** изменяет систему `TierSortingRegistry`
3. **Система не распознает** кирки GregTech как подходящие инструменты
4. **Результат**: `Can Harvest: false`

## 📊 Влияние на игровой процесс

### Затронутые элементы:
- ✅ **Все рудные блоки GregTech** (железная, медная, золотая руда и т.д.)
- ✅ **Все инструменты GregTech** (кирки, лопаты, топоры)
- ✅ **Все материалы инструментов** (железо, сталь, титан, алмаз и т.д.)

### Не затронутые элементы:
- ❌ **Ванильные блоки** - работают нормально
- ❌ **Блоки других модов** - не влияют
- ❌ **Ванильные инструменты** - работают нормально

## 🔧 Технические детали

### Структура тегов Legendary Monsters:
```json
// legendary_monsters/data/minecraft/tags/blocks/mineable/pickaxe.json
{
  "values": [
    "legendary_monsters:ancient_dripstone_bricks",
    "legendary_monsters:ancient_dripstone_block",
    "legendary_monsters:ancient_dripstone_tiles",
    // ... другие блоки Legendary Monsters
  ]
}
```

### Регистрация в TierSortingRegistry:
```java
// Legendary Monsters регистрирует свои теги
TierSortingRegistry.registerTier(DINOSAUR_BONE, ...)
TierSortingRegistry.registerTier(VOID, ...)
TierSortingRegistry.registerTier(BRONZITE, ...)
TierSortingRegistry.registerTier(Lightning, ...)
```

### Влияние на GregTech:
```java
// GregTech проверяет совместимость
TierSortingRegistry.isCorrectTierForDrops(getTier(tier), state)
// ↑ Эта проверка не проходит из-за конфликтующих тегов
```

## 🎯 Решение

### Подход FixBreakRude:

1. **Принудительное разрешение добычи**
   ```java
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onHarvestCheck(PlayerEvent.HarvestCheck event) {
       if (isGregTechBlock(state) && isGregTechTool(heldItem)) {
           event.setCanHarvest(true); // Принудительно разрешаем
       }
   }
   ```

2. **Восстановление правильной скорости**
   ```java
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onBreakSpeedOverride(PlayerEvent.BreakSpeed event) {
       if (isGregTechBlock(state) && isGregTechTool(heldItem)) {
           float correctSpeed = getGregTechToolSpeed(heldItem, state);
           event.setNewSpeed(correctSpeed); // Устанавливаем правильную скорость
       }
   }
   ```

3. **Определение GregTech элементов**
   ```java
   private boolean isGregTechBlock(BlockState state) {
       return blockId.contains("gtceu:") || blockId.contains("gregtech:") ||
              blockClass.contains("gtceu") || blockClass.contains("gregtech");
   }
   ```

## 📈 Результат

После применения патча:
- ✅ **Кирки GregTech добывают блоки GregTech**
- ✅ **Правильная скорость добычи**
- ✅ **Все типы руд работают**
- ✅ **Все материалы инструментов работают**
- ✅ **Не влияет на другие моды**

## 🔗 Связанные ресурсы

- [GregTech Community Edition Unofficial](https://github.com/GregTechCEu/GregTechCEu)
- [Legendary Monsters](https://www.curseforge.com/minecraft/mc-mods/legendary-monsters)
- [FixBreakRude - Решение проблемы](https://github.com/your-repo/fixbreakrude) 