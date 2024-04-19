![Bot](https://github.com/Efler/telegram-scrapper-bot/actions/workflows/bot.yml/badge.svg)
![Scrapper](https://github.com/Efler/telegram-scrapper-bot/actions/workflows/scrapper.yml/badge.svg)

# Telegram Scrapper Bot (Link tracker)

Микросервисное приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий уведомление отправляются через Telegram-бота.

---

## Стек

* Проект написан на `Java 21` с использованием `Spring Boot 3`
* Приложение состоит из 2-х микросервисов: 
  - `scrapper` - отслеживание обновлений, работа с базой данных
  - `bot` - API приложения через Telegram-бота
* Для работы с API Telegram используется библиотека [pengrad/java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api)
* Микросервисы общаются между собой через `HTTP`/`Kafka` _(в зависимости от конфигурации)_
* Для хранения данных используется `PostgreSQL` в связке с системой управления миграциями базы данных `Liquibase`
* `REST API` модулей описан в `OpenAPI`-спецификациях, присутствует возможность тестирования через `Swagger`
* Работа с данными в `scrapper` производится с использованием `JDBC`/`JOOQ`/`JPA` _(в зависимости от конфигурации)_
* В каждом модуле используется механизм `Retry (Spring)` и `Rate Limiting (Bucket4j)` для HTTP-запросов _(настройки конфигурируются)_
* Присутствует мониторинг метрик приложения с использованием `Prometheus` и Web-UI `Grafana`
* Github Actions настроен на автоматическую сборку образов `Docker`

---

## Packages

Образы модулей доступны на Github Container Registry:
```
docker pull ghcr.io/efler/telegram-scrapper-bot/scrapper:latest
```
```
docker pull ghcr.io/efler/telegram-scrapper-bot/bot:latest
```

---

## API

Взаимодействие с приложением происходит через Telegram-бота (токен бота указывается через переменную окружения `TOKEN`)

**Список команд:**
- `/start` - Зарегистрировать пользователя
- `/help` - Вывести окно с командами
- `/track [link]` - Начать отслеживание ссылки
- `/untrack [link]` - Прекратить отслеживание ссылки
- `/list` - Вывести список отслеживаемых ссылок
- `/remove_me` - Удалить свой аккаунт из базы данных4

Бот поддерживает _'прозрачный'_ режим, конфигурация происходит через переменную окружения `IGNORE_INCOME_UPDATES`

---

## Конфигурация модулей

Вся конфигурация контейнеров происходит через `переменные окружения`, сами модули конфигурируются через `application.yml`
