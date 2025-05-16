# My OTP service

#### Простое и удобное backend-приложение, которое помогает защитить операции с помощью временных кодов.

## Функционал

- Аутентификация пользователя с авторизацией прав Admin и User;
- Генерация OTP кода и его отправка на: Email, SMS, Telegram и сохранение в файл;
- Валидация кода.

## Стек

- Java 21
- PostgreSQL 15
- Система сборки - Maven

## Запуск

```bash
docker-compose up
```
После этой команды развернется бэкенд (порт 8080) и БД (порт 5432) в контейнерах docker.
Автоматически произойдет миграция необходимых таблиц в БД, согласно подготовленным ранее скриптам
SQL в папке /migrations/init.sql

## Примеры API-запросов для тестирования

### Регистрация пользователя

```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"Mikhail","password":"321","role":"USER"}'
```

### Вход (получение токена)

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"Mikhail","password":"321"}'
```

### Генерация OTP

```bash
curl -X POST http://localhost:8080/otp/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"operationId":"11","channel":"TELEGRAM"}'
```

### Проверка OTP

```bash
curl -X POST http://localhost:8080/otp/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"code":"RECEIVED_CODE"}'
```

### Действия администратора

```bash
# Изменение параметров OTP
curl -X PUT http://localhost:8080/admin/config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d '{"length":3,"ttlSeconds":30}'

# Просмотр пользователей
curl -X GET http://localhost:8080/admin/users \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Удаление пользователя
curl -X DELETE http://localhost:8080/admin/users/2 \
  -H "Authorization: Bearer ADMIN_TOKEN"
```




