# EduHelper Bot 🤖

Образовательный Telegram-бот для взаимодействия между преподавателями и студентами.

## 🚀 Функции
- Аутентификация по ролям (учитель/студент)
- Управление расписанием
- Отправка заданий
- Отслеживание прогресса
- Генерация токенов для связи
- Система обратной связи

## 🛠 Технологии
- Java 17
- Spring(Boot, WEB, Data JPA, AOP)
- Hibernate
- Docker
- Telegram Bots API
- Lombok
- Maven

## 🚀 Запуск
1. Создайте бота через [@BotFather](https://t.me/BotFather) и получите токен
2. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/Archiebalt/EduHelper.git
3. Создайте файл .env в корне проекта и отредактируйте
   ```
   # Telegram
    TELEGRAM_BOT_USERNAME=@your_bot_username
    TELEGRAM_BOT_TOKEN=your_bot_token
    TELEGRAM_BOT_PATH=https://your-webhook-url

    # Database
    DB_USERNAME=username
    DB_PASSWORD=strongpassword
4. Запуск приложения
   ```bash
   docker-compose up -d --build
