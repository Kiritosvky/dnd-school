<!--
MIT License

Copyright (c) 2026 Kiritosvky

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
-->

# DND School

Тихий режим по таймеру и таймер до перемены. Для Samsung S23 Ultra, Android 16 (One UI), minSdk 26.

## Что делает

**Вкладка DND** — «Не беспокоить» на заданное время (часы:минуты:секунды): полностью без звука и без вибрации. Таймер идёт в фоне, после перезагрузки восстанавливается, по окончании режим и громкость возвращаются как было.

**Вкладка «Школа»** — сколько осталось до перемены: `15:20, 16:10, 17:00, 17:45, 18:50`. После последней — «Школа закончилась». Кнопка «Изменить расписание» правит список.

**Настройки (⚙)** — цвет фона, цвет текста, акцент, размер шрифта, жирность. Шрифт Inter.

## Сборка

Нужны JDK 17+, Android SDK (build-tools, platform). Без Gradle:

```powershell
powershell -ExecutionPolicy Bypass -File build.ps1
```

Результат: `DndSchool.apk` в корне.

## Тест логики

```powershell
javac -d build\utest app\src\com\kiritosvky\dndschool\School.java app\src\com\kiritosvky\dndschool\Fmt.java test\com\kiritosvky\dndschool\SchoolTest.java
java -cp build\utest com.kiritosvky.dndschool.SchoolTest
```

## Разрешения

- `ACCESS_NOTIFICATION_POLICY` — сам режим «Не беспокоить». Без него приложение глушит только звук и вибрацию звонков.
- `SCHEDULE_EXACT_ALARM` — точное выключение таймера.
- `RECEIVE_BOOT_COMPLETED` — восстановление таймера после перезагрузки.
- `INTERNET` — только отправка лога на ПК, если в настройках указан адрес. Пустое поле = приложение вообще не ходит в сеть.

## Отладка: лог на ПК по Wi-Fi

1. На ПК: `powershell -ExecutionPolicy Bypass -File tools\logserver.ps1` (слушает порт 8765, пишет в `tools/logs/`).
2. В приложении: ⚙ → «Лог-сервер» → `192.168.1.10:8765` (IP ПК) → Готово.
3. Дальше каждое действие, ошибки и краши летят в консоль ПК одной строкой.

Приложение не содержит аналитики и фоновых сервисов: секундный таймер работает только при открытом экране, всё остальное делает системный будильник.

## Лицензия

MIT, 2026 Kiritosvky. Шрифт Inter — SIL Open Font License (`LICENSE-INTER.txt`).
