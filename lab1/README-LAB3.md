# Лабораторная работа №3: Асинхронное взаимодействие в enterprise-приложениях

## Реализованный функционал

Данная работа реализует асинхронную обработку задач с использованием JMS и ActiveMQ Artemis.

### Компоненты системы

#### 1. Очереди (Point-to-Point / Queue)

**email.queue** - очередь для отправки приветственных email
- `NotificationProducer` - отправляет сообщения при создании клиента
- `EmailNotificationConsumer` - обрабатывает сообщения и отправляет email

**report.queue** - очередь для генерации отчётов
- `ReportProducer` - отправляет запросы на генерацию отчётов
- `ReportGeneratorConsumer` - обрабатывает запросы и генерирует отчёты

#### 2. Топики (Publish-Subscribe / Topic)

**customer.events.topic** - топик событий клиентов
- `CustomerEventPublisher` - публикует события (CREATED, UPDATED, DELETED)
- `AuditLogConsumer` - подписчик для аудита (запись в лог)
- `AnalyticsConsumer` - подписчик для аналитики (обновление метрик)

### Конфигурация

#### application.yml
```yaml
spring:
  artemis:
    mode: native
    host: localhost
    port: 61616
    user: admin
    password: admin
  
  jms:
    listener:
      auto-startup: true
      concurrency: 1
      max-concurrency: 5

app:
  queue:
    email: email.queue
    report: report.queue
  topic:
    customer-events: customer.events.topic
```

## Инструкция по запуску

### 1. Установка и запуск брокера сообщений

**Вариант А: Apache ActiveMQ Artemis (рекомендуется)**

```bash
# Скачать ActiveMQ Artemis с официального сайта
# Распаковать архив
cd apache-artemis-2.31.2/bin
./artemis create --allow-anonymous --user admin --password admin mybroker
cd mybroker/bin
./artemis run
```

Консоль управления: http://localhost:8161 (admin/admin)

**Вариант Б: Встроенный брокер (для разработки)**

В application.yml заменить:
```yaml
spring:
  artemis:
    mode: embedded
    enabled: true
    embedded:
      persistent: false
      queues: email.queue,report.queue
      topics: customer.events.topic
```

### 2. Запуск приложения

```bash
gradle bootRun
```

## API Endpoints

### 1. Создание клиента (с асинхронной отправкой email)

```bash
POST /api/v1/customers
Content-Type: application/json
Authorization: Bearer <token>

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com"
}
```

**Что происходит:**
1. Клиент сохраняется в БД (синхронно)
2. Отправляется сообщение в `email.queue` (асинхронно)
3. Публикуется событие в `customer.events.topic` (асинхронно)
4. API возвращает ответ мгновенно

### 2. Генерация отчёта (асинхронно)

```bash
POST /api/v1/customers/reports/generate?type=customer-list&format=PDF
Authorization: Bearer <token>
```

**Ответ:**
```json
{
  "requestId": 1234567890,
  "status": "queued",
  "message": "Запрос на генерацию отчёта принят в обработку",
  "reportType": "customer-list",
  "format": "PDF"
}
```

### 3. Получение клиентов

```bash
GET /api/v1/customers?page=0&size=10
Authorization: Bearer <token>
```

## Проверка асинхронности

1. Создайте несколько клиентов через POST /api/v1/customers
2. Обратите внимание, что API возвращает ответ **мгновенно** (не ждёт 2 секунды)
3. В логах приложения через несколько секунд появятся сообщения:
   - `[EmailNotificationConsumer] Получено сообщение для отправки email`
   - `[AUDIT LOG] Получено событие клиента`
   - `[ANALYTICS] Получено событие клиента для аналитики`

## Архитектурные преимущества

| Преимущество | Описание |
|-------------|----------|
| **Отзывчивость** | API возвращает ответ мгновенно, тяжёлая работа выполняется в фоне |
| **Отказоустойчивость** | При временной недоступности потребителя сообщения сохраняются в очереди |
| **Масштабирование** | Можно добавлять потребителей для параллельной обработки |
| **Слабая связанность** | Отправитель не знает о получателях, только о формате сообщения |
| **Сглаживание пиков** | Очередь работает как буфер при всплесках нагрузки |

## Структура проекта

```
src/main/java/com/lab1/
├── config/
│   ├── JmsConfig.java              # Конфигурация JMS (фабрики, конвертеры)
│   └── ...
├── dto/
│   ├── WelcomeEmailMessage.java    # DTO для email уведомлений
│   ├── ReportRequestMessage.java   # DTO для запросов отчётов
│   ├── CustomerEvent.java          # DTO для событий клиентов
│   └── CustomerEventType.java      # Типы событий
├── jms/
│   ├── NotificationProducer.java   # Producer для email
│   ├── EmailNotificationConsumer.java  # Consumer для email
│   ├── ReportProducer.java         # Producer для отчётов
│   ├── ReportGeneratorConsumer.java    # Consumer для отчётов
│   ├── CustomerEventPublisher.java # Publisher событий
│   ├── AuditLogConsumer.java       # Consumer для аудита
│   └── AnalyticsConsumer.java      # Consumer для аналитики
└── ...
```

## Тестирование

### 1. Тестирование асинхронной отправки email

```bash
# Создать клиента
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{"firstName":"Test","lastName":"User","email":"test@example.com"}'

# Проверить логи - ответ должен прийти мгновенно,
# а сообщение об отправке email появиться через ~2 секунды
```

### 2. Тестирование генерации отчётов

```bash
# Запросить генерацию отчёта
curl -X POST "http://localhost:8080/api/v1/customers/reports/generate?type=customer-list&format=PDF" \
  -H "Authorization: Bearer <admin_token>"

# Проверить логи - запрос должен быть принят мгновенно,
# а сообщение о генерации появиться через ~3 секунды
```

### 3. Тестирование событий (Pub-Sub)

При создании/обновлении/удалении клиента в логах должны появиться сообщения:
- `[AUDIT LOG]` - от AuditLogConsumer
- `[ANALYTICS]` - от AnalyticsConsumer

Оба потребителя получают одно и то же сообщение (Pub-Sub паттерн).
