# Voice Reminder

Android-приложение на Kotlin с голосовыми напоминаниями, заметками, Room и локальными уведомлениями.

## CI/CD

В проект добавлен GitHub Actions workflow:

- Файл: `.github/workflows/android-apk.yml`
- Назначение: удалённая сборка installable debug APK
- Триггеры:
  - ручной запуск через **Actions > Android APK > Run workflow**
  - автоматический запуск при push в `main` или `master`

## Как получить APK через GitHub Actions

1. Загрузите проект в GitHub-репозиторий.
2. Откройте вкладку **Actions**.
3. Запустите workflow **Android APK** вручную или дождитесь запуска после push.
4. После завершения откройте run и скачайте артефакт `voice-reminder-debug-apk`.

## Где будет APK

Внутри workflow APK собирается по пути:

`app/build/outputs/apk/debug/app-debug.apk`

В GitHub Actions он публикуется как артефакт:

`voice-reminder-debug-apk`

## Локальная сборка

```powershell
gradle :app:assembleDebug --no-daemon
```
