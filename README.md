# FixBreakRude - Legendary Monsters & GregTech Compatibility Patch

## 🚨 Проблема

При установке **Legendary Monsters** вместе с **GregTech Community Edition Unofficial (GTCEu)** возникает критическая проблема совместимости:

### ❌ Что происходит:
1. **Кирки GregTech не могут добывать блоки GregTech** - система определяет `Can Harvest: false`
2. **Скорость добычи неправильная** - кирки работают медленнее, чем должны
3. **Проблема затрагивает все рудные блоки GregTech** - от железной руды до редких минералов

### 🔍 Причина проблемы:
**Legendary Monsters** регистрирует свои собственные теги инструментов:
```java
public static final TagKey<Block> NEEDS_DINOSAUR_BONE_TOOL = tag("needs_dinosaur_bone_tool");
public static final TagKey<Block> NEEDS_VOID_TOOL = tag("needs_dinosaur_bone_tool");
```

Эти теги конфликтуют с системой `TierSortingRegistry`, которую использует GregTech для определения правильных инструментов. В результате:
- Система не распознает кирки GregTech как подходящие инструменты
- Скорость добычи изменяется из-за конфликтующих настроек
- Блоки GregTech требуют "правильный инструмент", но кирки GregTech не считаются таковыми

## ✅ Решение

**FixBreakRude** - это мод-патч, который решает проблему совместимости:

### 🛠️ Что делает мод:
1. **Принудительно разрешает добычу** - кирки GregTech могут добывать блоки GregTech
2. **Восстанавливает правильную скорость** - исправляет скорость добычи для GregTech инструментов
3. **Игнорирует конфликтующие теги** - обходит систему тегов Legendary Monsters
4. **Сохраняет функциональность** - не влияет на другие моды

### 🔧 Как работает:
```java
// Отслеживает события добычи
@SubscribeEvent(priority = EventPriority.HIGHEST)
public void onHarvestCheck(PlayerEvent.HarvestCheck event) {
    if (isGregTechBlock(state) && isGregTechTool(heldItem)) {
        event.setCanHarvest(true); // Принудительно разрешает добычу
    }
}

// Восстанавливает скорость добычи
@SubscribeEvent(priority = EventPriority.LOWEST)
public void onBreakSpeedOverride(PlayerEvent.BreakSpeed event) {
    if (isGregTechBlock(state) && isGregTechTool(heldItem)) {
        float correctSpeed = getGregTechToolSpeed(heldItem, state);
        event.setNewSpeed(correctSpeed); // Устанавливает правильную скорость
    }
}
```

## 📦 Установка

### Требования:
- **Minecraft**: 1.20.1
- **Forge**: 47+
- **GTCEu**: 7.0.2+
- **Legendary Monsters**: 1.9.3+

### Шаги установки:
1. Скачайте `fixbreakrude-1.0.0.jar`
2. Поместите файл в папку `mods`
3. Перезапустите игру
4. Проверьте логи: должно быть `"FixBreakRude mod initialized!"`

## ⚙️ Конфигурация

Мод создает файл `config/fixbreakrude-common.toml`:

```toml
[FixBreakRude Configuration]
# Включить патч для совместимости Legendary Monsters и GregTech
enablePatch = true
# Логировать действия патча
logActions = false
# Режим отладки (дополнительная информация)
debugMode = false
# Исправлять скорость добычи для GregTech инструментов
fixMiningSpeed = true
```

### Параметры:
- **enablePatch** - Включить/отключить патч (по умолчанию: true)
- **logActions** - Логировать действия патча в консоль (по умолчанию: false)
- **debugMode** - Режим отладки с дополнительной информацией (по умолчанию: false)
- **fixMiningSpeed** - Исправлять скорость добычи для GregTech инструментов (по умолчанию: true)

### ⚠️ Важно о логировании:
- **logActions** - логирует только реальные изменения (разрешение добычи, восстановление скорости)
- **debugMode** - логирует только значительные изменения скорости (>0.01)
- Мод оптимизирован для предотвращения спама в логах при наведении на руду

## 🧪 Тестирование

### Проверка работы патча:
1. **Попробуйте добыть блок GregTech киркой GregTech**
   - Блок должен добываться нормально
   - Скорость должна соответствовать кирке

2. **Включите логирование для отладки:**
   ```toml
   logActions = true
   debugMode = true
   ```

3. **Проверьте логи игры:**
   ```
   [INFO]: FixBreakRude: Allowed GregTech tool to harvest GregTech block
   [INFO]: FixBreakRude: Restored GregTech tool speed
   ```

### Что должно работать:
- ✅ Кирки GregTech добывают блоки GregTech
- ✅ Правильная скорость добычи
- ✅ Все типы руд GregTech
- ✅ Все материалы кирок GregTech

## 🔍 Техническая информация

### Приоритеты обработки:
- **HIGHEST** - Обрабатывается первым, чтобы перехватить события
- **LOWEST** - Обрабатывается последним, чтобы переопределить изменения

### Определение GregTech элементов:
```java
// Блоки GregTech
blockId.contains("gtceu:") || blockId.contains("gregtech:") ||
blockClass.contains("gtceu") || blockClass.contains("gregtech")

// Инструменты GregTech
itemId.contains("gtceu:") || itemId.contains("gregtech:") ||
itemClass.contains("gtceu") || itemClass.contains("gregtech") ||
itemStack.getTag().contains("GT.Tool")
```

### Извлечение скорости GregTech:
```java
if (itemStack.hasTag() && itemStack.getTag().contains("GT.Tool")) {
    var toolTag = itemStack.getTag().getCompound("GT.Tool");
    if (toolTag.contains("ToolSpeed")) {
        return toolTag.getFloat("ToolSpeed");
    }
}
```

## 🐛 Отчеты об ошибках

Если у вас возникли проблемы:

### Необходимая информация:
- Версия Minecraft
- Версия Forge
- Версия GTCEu
- Версия Legendary Monsters
- Полные логи игры (если включено логирование)

### Что проверить:
1. Загружается ли мод (см. логи)
2. Включены ли настройки патча
3. Правильно ли определены GregTech элементы
4. Нет ли конфликтов с другими модами

## 📄 Лицензия

All Rights Reserved

## 👥 Авторы

Leviathan Team

---

## 🔗 Связанные ссылки

- [GregTech Community Edition Unofficial]([https://github.com/GregTechCEu/GregTechCEu](https://github.com/GregTechCEu/GregTech-Modern))
- [Legendary Monsters](https://www.curseforge.com/minecraft/mc-mods/legendary-monsters)

## 📝 История версий

### v1.0.0
- ✅ Исправление проблемы добычи блоков GregTech
- ✅ Восстановление правильной скорости добычи
- ✅ Конфигурируемые настройки

- ✅ Подробное логирование для отладки 
