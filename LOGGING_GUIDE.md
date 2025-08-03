# Руководство по логированию FixBreakRude

## 📝 Настройки логирования

Мод имеет встроенную защиту от спама в логах. Все настройки находятся в `config/fixbreakrude-common.toml`:

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

## 🔧 Типы логирования

### 1. logActions (рекомендуется для отладки)
- **Что логирует**: Только реальные изменения
- **Когда срабатывает**: При разрешении добычи или восстановлении скорости
- **Пример логов**:
  ```
  [INFO]: FixBreakRude: Allowed GregTech tool to harvest GregTech block - gtceu:pickaxe_iron -> gtceu:coal_ore
  [INFO]: FixBreakRude: Restored GregTech tool speed - gtceu:pickaxe_iron -> 12.0 (was: 1.0)
  ```

### 2. debugMode (для детальной отладки)
- **Что логирует**: Значительные изменения скорости (>0.01)
- **Когда срабатывает**: При принудительной установке скорости
- **Пример логов**:
  ```
  [DEBUG]: FixBreakRude: Forced GregTech tool speed - gtceu:pickaxe_iron -> 12.0 (was: 1.0) for block gtceu:coal_ore
  ```

## ⚠️ Защита от спама

Мод оптимизирован для предотвращения спама в логах:

### ✅ Что НЕ логируется:
- Наведение на руду без изменений
- Повторные проверки одного и того же блока
- Незначительные изменения скорости (<0.01)

### ✅ Что логируется:
- Только реальные изменения состояния
- Только значительные изменения скорости
- Только при активном действии игрока

## 🚀 Рекомендуемые настройки

### Для обычной игры:
```toml
logActions = false
debugMode = false
```

### Для отладки проблем:
```toml
logActions = true
debugMode = false
```

### Для детальной диагностики:
```toml
logActions = true
debugMode = true
```

## 🔍 Как проверить работу мода

1. **Включите логирование**:
   ```toml
   logActions = true
   ```

2. **Попробуйте добыть блок GregTech киркой GregTech**

3. **Проверьте логи** - должны появиться сообщения:
   ```
   [INFO]: FixBreakRude: Allowed GregTech tool to harvest GregTech block
   [INFO]: FixBreakRude: Restored GregTech tool speed
   ```

4. **Если логов нет** - мод работает, но не нуждается в исправлениях

## 📊 Производительность

- **Без логирования**: Минимальное влияние на производительность
- **С logActions**: Незначительное влияние
- **С debugMode**: Умеренное влияние (только при реальных изменениях)

## 🐛 Устранение проблем

### Если логи спамят:
1. Отключите `debugMode = false`
2. Используйте только `logActions = true`
3. Проверьте, что проблема действительно решена

### Если логов нет:
1. Убедитесь, что мод загрузился: `"FixBreakRude mod initialized!"`
2. Проверьте, что `enablePatch = true`
3. Попробуйте добыть блок GregTech киркой GregTech 