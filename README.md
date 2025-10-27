# Система бронирования отелей

Распределённое приложения  на Spring Boot/Cloud:
- API Gateway (Spring Cloud Gateway)
- Booking Service (JWT-аутентификация, бронирования, согласованность)
- Hotel Management Service (CRUD отелей и номеров, агрегаты по загруженности)
- Eureka Server (Service Registry, динамическое обнаружение сервисов)

## Возможности
- Регистрация и вход пользователей (JWT) через Booking Service
- Создание бронирований с двухшаговой согласованностью (PENDING → CONFIRMED/CANCELLED с компенсацией)
- Идемпотентность запросов с `requestId`
- Повторы с экспоненциальной паузой и таймауты при удалённых вызовах
- Подсказки по выбору номера (сортировка по `timesBooked`, затем по `id`)
- Администрирование пользователей (CRUD) и отелей/номеров (CRUD) для админов
- Агрегации: популярность номеров по `timesBooked`
- Сквозная корреляция с заголовком `X-Correlation-Id`

## Архитектура и порты
- `eureka-server`: порт 8761
- `api-gateway`: порт 8080
- `hotel-service`: порт случайный (0), регистрируется в Eureka под именем `hotel-service`
- `booking-service`: порт случайный (0), регистрируется в Eureka под именем `booking-service`

## Основные эндпойнты
Через Gateway (8080):
- Аутентификация (Booking):
  - POST `/auth/register` — регистрация (для админа добавить `"admin": true`)
  - POST `/auth/login` — получение JWT
- Бронирования (Booking):
  - GET `/bookings` — мои бронирования
  - POST `/bookings` — создать бронирование (PENDING → CONFIRMED/компенсация). Поле `createdAt` выставляется автоматически.
  - GET `/bookings/suggestions` — подсказки по комнатам (сортировка по загрузке)
  - GET `/bookings/all` — все бронирования (только админ)
- Пользователи (Booking, admin):
  - GET `/admin/users`, GET `/admin/users/{id}`, PUT `/admin/users/{id}`, DELETE `/admin/users/{id}`
- Отели и номера (Hotel):
  - GET `/hotels`, GET `/hotels/{id}`, POST `/hotels`, PUT `/hotels/{id}`, DELETE `/hotels/{id}` (админ)
  - GET `/rooms/{id}`, POST `/rooms`, PUT `/rooms/{id}`, DELETE `/rooms/{id}` (админ)
  - POST `/rooms/{id}/hold` — удержание слота (идемпотентно по `requestId`)
  - POST `/rooms/{id}/confirm` — подтверждение удержания
  - POST `/rooms/{id}/release` — освобождение удержания (компенсация)
- Статистика (Hotel):
  - GET `/stats/rooms/popular` — номера по популярности (`timesBooked`)

## Консоль H2
- Включена для Hotel Service: `/h2-console` (через прямой порт сервиса)
- Схема JDBC: см. `application.yml` соответствующего сервиса

## Тестирование
Пример минимального теста в `hotel-service`: `HotelAvailabilityTests` (идемпотентность hold/confirm/release).

### Интеграционные тесты
- Booking Service (WebTestClient + WireMock):
  - `BookingHttpIT#createBooking_Http_Success` — успешное бронирование (CONFIRMED)
  - Покрыты unit/сервис-тестами: ошибка удалённого сервиса (компенсация), таймаут, идемпотентность, подсказки
- Hotel Service (MockMvc):
  - `HotelHttpIT#adminCanCreateHotel` — админ может создавать отели
  - `HotelAvailabilityTests` и `HotelMoreTests` — занятость по датам, available-флаг, статистика

