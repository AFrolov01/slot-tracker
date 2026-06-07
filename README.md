# 🎰 Blackjackist Slot Tracker (Android)

Автоматический трекер слотов для мобильной игры **Blackjackist** (KamaGames).
Использует Android Accessibility Service для чтения баланса с экрана и автоматического подсчета статистики.

## ⚠️ Важно

- Это приложение **только считывает текст с экрана** и ведет статистику.
- Оно **НЕ нажимает кнопки**, **НЕ изменяет игру**, **НЕ использует эксплойты**.
- Работает только с игровой валютой (фишками), не с реальными деньгами.

## 📋 Требования

- Android 7.0+ (API 24+)
- Blackjackist установлен на устройстве
- Android Studio Hedgehog (2023.1.1) или новее

## 🚀 Установка

### 1. Склонируй или распакуй проект

```bash
cd blackjackist-tracker
```

### 2. Открой в Android Studio

`File → Open →` выбери папку `blackjackist-tracker`

### 3. Синхронизируй Gradle

Нажми "Sync Now" в уведомлении Android Studio.

### 4. Собери APK

```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

Или через терминал:
```bash
./gradlew assembleDebug
```

APK появится в:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 5. Установи на телефон

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Или скопируй APK на телефон и установи вручную (разреши установку из неизвестных источников).

## 🔧 Настройка

### 1. Запусти приложение Slot Tracker

### 2. Введи начальный баланс и ставку

### 3. Нажми "Открыть настройки Accessibility"

### 4. Найди "Slot Tracker" в списке и включи

### 5. Верись в приложение и запусти Blackjackist

Сервис автоматически начнет отслеживать изменения баланса.

## 🎯 Как это работает

1. **Accessibility Service** мониторит окно Blackjackist
2. Ищет текстовые элементы, похожие на числа баланса
3. Когда баланс меняется — определяет тип спина:
   - 💀 **Dead Spin** — баланс упал примерно на ставку
   - 💎 **Win** — баланс вырос (возврат ставки или выигрыш)
   - 🎁 **Bonus** — баланс вырос сильно (бонуска/фриспины)
4. Сохраняет в память телефона и показывает статистику в реальном времени

## 🐛 Если не работает

### Проблема: "Сервис не активен"
- Убедись, что включил Accessibility Service в настройках Android
- На некоторых телефонах (Xiaomi, Samsung) нужно дополнительно разрешить "Отображение поверх других приложений"

### Проблема: Баланс не считывается
- Blackjackist может использовать Unity/WebView — тогда Accessibility не видит текст
- Попробуй запустить игру через **BlueStacks/LDPlayer на PC** и использовать Python-скрипт `slot_auto_tracker.py`
- Или используй HTML-трекер в браузере телефона рядом с игрой

### Проблема: Дублируются спины
- Увеличь задержку между спинами в коде (`lastSpinTime` в `BlackjackistTrackerService.kt`)

## 📁 Структура проекта

```
blackjackist-tracker/
├── app/
│   ├── src/main/
│   │   ├── java/com/slottracker/
│   │   │   ├── MainActivity.kt          # Главный экран
│   │   │   ├── BlackjackistTrackerService.kt  # Accessibility Service
│   │   │   ├── StatsManager.kt          # Хранение данных
│   │   │   └── SpinData.kt              # Модель спина
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml # UI
│   │   │   ├── xml/accessibility_service_config.xml
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── README.md
```

## 🔒 Безопасность

- Приложение не отправляет данные в интернет
- Все данные хранятся локально в SharedPreferences
- Не требует root-прав
- Не использует VPN или прокси

## 📱 Альтернативы

Если Android-приложение не работает с твоей версией Blackjackist:

1. **HTML-трекер** — открой в браузере телефона, жми кнопки вручную
2. **Python-скрипт** — для PC + эмулятор (BlueStacks)
3. **Запись экрана + анализ** — запиши игру, скинь видео, я разберу статистику

---

**Автор:** Сгенерировано AI для сбора статистики слотов.
