# RestDummy — описание классов проекта

Этот документ кратко описывает назначение каждого ключевого класса в текущей структуре проекта.

---

## com.LT.restDummy (приложение)
- **RestDummyApplication** — точка входа Spring Boot (запуск контекста приложения).

---

## com.LT.restDummy.config
- **PropertyBeen** — конфигурационный бин, читает свойства из `application.properties` и подготавливает зависимости/значения, используемые по всему приложению.

---

## com.LT.restDummy.controller
- **RestDummyController**
  - Эндпоинты:
    - `POST /services?service=<name>&delay=<ms>&isAvailable=<bool>` — вызов сервиса по имени.
    - `GET|POST /customEndpoint/**` — вызов по произвольному пути (часть после `/customEndpoint` — имя/эндпоинт сервиса).
    - `GET /getServices` — список сервисов + версия.
    - `POST /editServices` — массовое изменение параметров сервисов.
    - `GET /version` — вернуть версию приложения.
  - Делегирует бизнес-логику в `ResponseHandlerService` и `ServiceManagementService`.

- **DelayController** — вспомогательные эндпоинты управления задержками (сброс на дефолт, массовые операции и т.п.).

---

## com.LT.restDummy.date
- **DateModule** — модульные настройки/адаптеры работы с датами (форматы сериализации и т.п.).
- **DateUtils** — утилиты для операций с датами (парсинг, форматирование, арифметика).

---

## com.LT.restDummy.domain.delay
- **DelayConfig**
  - Конфигурация задержек и таймаутов сервиса:
    - `defaultDelay`, `currentDelay`, `timeout`.
    - Поля планировщика: дата применения и отложенная задержка.

---

## com.LT.restDummy.domain.dto
- **ServiceRequestDto** — DTO для редактирования одного сервиса (имя, задержка, таймаут, доступность, системное имя).
- **ServicesDto** — контейнер-список `ServiceRequestDto` (используется в `/editServices`).

---

## com.LT.restDummy.domain.manager
- **ServiceRegistry**
  - Потокобезопасный реестр сервисов (`ConcurrentHashMap`).
  - `register`, `registerAll`, `get(name)` (если нет — бросает `ServiceNotFoundException`), `getAll`, `getAllNames`.

- **ServiceDelayManager**
  - Управление задержками: `getDelay/setDelay`, `setDefaultDelays()`.
  - Таймауты/планировщик: `getTimeout`, `getDelayForScheduler/setDelayForScheduler`, `getSchedulerToDelay/setSchedulerToDelay`.
  - Утилиты: `getDefaultDelay`, `calculateMinus10PercentDelay`, `applyMinus10PercentToAll`.

- **ServiceAvailabilityManager**
  - Доступность сервисов: `isAvailable/setAvailable`, `setAvailableToAll`.
  - Планирование доступности по времени: `scheduleAvailability`, `getAvailabilityScheduler`.

- **OccurrenceTracker**
  - Отслеживание количества вхождений по ключу для `OCCURRENCE_BASED` стратегии.
  - `incrementAndGet(serviceName, key)` — инкрементирует счетчик и возвращает текущее значение.
  - `get(serviceName, key)` — получает текущее количество без инкремента.
  - `reset(serviceName, key)` — сбрасывает счетчик для конкретного ключа.
  - `scheduleKeyCleanup(serviceName, key, cleanupTimeMs)` — планирует автоматическую очистку ключа через указанное время.
  - `clearService(serviceName)` — очищает все ключи для сервиса.
  - `clearAll()` — очищает все ключи для всех сервисов.
  - Автоматически отслеживает время последнего использования ключей и отменяет предыдущие задачи очистки при повторном использовании.

---

## com.LT.restDummy.domain.model
- **StubService**
  - Модель заглушаемого сервиса: имя, тип (`json|xml`), endpoint, системное имя.
  - Доступность + планировщик доступности.
  - `DelayConfig` — конфиг задержек.
  - `responses` — список вариантов ответа (`List<StubResponse>`).
  - `responseType` — стратегия выбора ответа (`ResponseType`).
  - `occurrenceCleanupTimeMs` — время автоматической очистки ключей для `OCCURRENCE_BASED` (в миллисекундах).

---

## com.LT.restDummy.domain.response
- **ResponseType** — стратегии выбора ответа:
  - `DEFAULT` — всегда первый ответ.
  - `THRESHOLD` — по весам (вероятностно, на основании `key` у ответа).
  - `PARAM_BASED` — по значению параметра (name/value) из тела запроса.
  - `OCCURRENCE_BASED` — переключение ответов на основе количества вхождений по ключу (см. ниже).

- **StubResponse**
  - Описывает один ответ: контент (JSON/XML строка), тип, ключ/вес (`key`), имя/значение параметра (для `PARAM_BASED` и `OCCURRENCE_BASED`).

- **ResponseResolver**
  - Выбор подходящего `StubResponse` из `StubService` по стратегии (`ResponseType`).
  - Обрабатывает пустые списки/несоответствия, возвращает fallback.
  - Для `OCCURRENCE_BASED`: отслеживает количество обращений по ключу и переключает ответы при достижении порога.

- **ResponseBuilder**
  - Собирает `List<StubResponse>` для `StubService` из текста заглушки и параметров (`servicesParams.properties`).
  - Определяет `responseType` сервиса и расставляет служебные поля ответов.
  - Поддерживает загрузку ответов из множественных файлов (например, `serviceName-1`, `serviceName-2`) или из одного файла с разделителями `-###-`.

---

## com.LT.restDummy.exception
- **ServiceException** — бизнес-исключения (например, сервис временно недоступен).
- **ServiceNotFoundException** — попытка обратиться к несуществующему сервису в реестре.
- **IncorrectParameterException** — ошибки валидации входных параметров.
- **ErrorHandler** — глобальный обработчик исключений (формирует HTTP-ответы об ошибках).
- **ErrorResponse** — DTO ответа об ошибке (код/сообщение/детали).

---

## com.LT.restDummy.file
- **ServiceFileHandler**
  - Работа с файлами заглушек и параметрами:
    - `fullFile(name, content)` — сохранение контента в каталог `services/`.
    - `getListFilesForFolder(dir)` — рекурсивный сбор имён файлов.
    - `readPropertiesFile(path)` — загрузка `.properties`.
    - `getService(name, content)` / `getService(name, content, params)` — построение `StubService`.
    - `updateFilesServices(name, content, rawParams)` — обновление `.properties` и сохранение контента.

---

## com.LT.restDummy.helper
- **ResponseHeaderBuilder**
  - Сборка `HttpHeaders`:
    - Базовый: `build(type)` → `Content-Type: application/json|xml`.
    - Расширенный: добавление произвольных заголовков с фильтром hop-by-hop и контролем переопределения `Content-Type`.

- **ResponseDelay**
  - Планирует отдачу `ResponseEntity<String>` с задержкой (мс).
  - Безопасно отправляет метрику времени ответа (через `VictoriaWriter`), логирует исключения.

- **ResponseCorrelatorService**
  - Корреляции/подстановки: встраивает значения из входного запроса в шаблон ответа (регексы, плейсхолдеры).

- **ResponseHelper** — вспомогательные утилиты, используемые при формировании ответа.

---

## com.LT.restDummy.influx *(историческая интеграция)*
- **InfluxBean**, **InfluxConnect**, **InfluxWriter** — инициализация клиента и отправка метрик в InfluxDB (может быть отключено/неиспользуется).

---

## com.LT.restDummy.scheduler
- **DelaySchedulerService** — применение отложенной задержки (по расписанию).
- **AvailabilitySchedulerService** — плановое включение/выключение сервиса.
- **Scheduler** — инициализация периодических задач (если требуется).

---

## com.LT.restDummy.service
- **ResponseHandlerService**
  - Основной обработчик запросов:
    1. Получить `StubService` из `ServiceValue.registry()`.
    2. Применить входные флаги (`delay`, `isAvailable`).
    3. Выбрать `StubResponse` через `ResponseResolver`.
    4. Применить корреляции `ResponseCorrelatorService`.
    5. Отдать через `ResponseDelay` с заголовками `ResponseHeaderBuilder`.

- **ServiceManagementService**
  - Операции для UI/админки: вернуть список сервисов (с версией), массово применить изменения (delay/available и т.п.).

- **ServiceMapper**
  - Маппинг `StubService` ⇄ `ServiceRequestDto` (для обмена с фронтом).

- **ServiceValue**
  - Фасад над реестром и менеджерами:
    - `registry()` → `ServiceRegistry`
    - `availability()` → `ServiceAvailabilityManager`
    - `delay()` → `ServiceDelayManager`
    - `updateService(StubService)` — применить текущую задержку/доступность.
    - `calculateMinus10PercentDelay(timeout)` — утилита для планировщика.

---

## com.LT.restDummy.util
- **JsonXmlParamExtractor**
  - Извлечение значения параметра из тела запроса:
    - JSON — JsonPath `$..param`.
    - XML — XPath `//*[local-name()='param']`.
  - Null-safe, аккуратные логи предупреждений.

- **RandomUtils** — генерация случайных значений для шаблонов/корреляций.

---

## com.LT.restDummy.victoria
- **VictoriaWriter**
  - Отправка метрик в VictoriaMetrics (Prometheus-формат):
    - `mock_response_time_ms{application, channel, operation} <value>`
    - `mock_requests_total{...} <value>` — накапливается и шлётся периодически.
  - Мягко отключается при отсутствии `victoria.url` (ошибки логируются, не валят обработку).

---

## com.LT.restDummy.viewData
- **ViewServiceData**, **ViewServiceDataDTO**, **ViewServiceNewData** — модели/DTO для фронта (табличные представления сервисов, формы редактирования).

---

## OCCURRENCE_BASED — переключение ответов на основе вхождений

### Описание функционала

`OCCURRENCE_BASED` — стратегия выбора ответа, которая позволяет переключаться между различными ответами на основе количества обращений с одним и тем же ключом. Это полезно для тестирования сценариев, когда нужно вернуть один ответ при первых обращениях, а затем переключиться на другой ответ.

### Как это работает

1. **Извлечение ключа**: Из тела запроса извлекается значение параметра, указанного в конфигурации (например, `orderId`, `epkId`).
2. **Отслеживание вхождений**: Для каждого уникального значения ключа ведется счетчик обращений.
3. **Переключение ответов**: 
   - До достижения порога (`switchAt`) возвращается первый ответ.
   - Начиная с порога и далее возвращается второй ответ.
4. **Автоматическая очистка**: Через указанное время (`cleanupTimeMs`) после последнего использования ключа счетчик сбрасывается, и цикл начинается заново.

### Конфигурация в `servicesParams.properties`

Для использования `OCCURRENCE_BASED` необходимо указать следующие параметры:

```properties
# Базовые параметры сервиса
example.type=json
example.timeout=3000
example.delay=1000

# Параметры для OCCURRENCE_BASED
example.occurrence.key.param=epkId          # Имя параметра для ключа (обязательно)
example.occurrence.switchAt=3               # При каком вхождении переключать ответ (обязательно)
example.occurrence.cleanupTimeMs=3000       # Время очистки ключа в миллисекундах (опционально, по умолчанию 3600000 = 1 час)
```

### Параметры

- **`occurrence.key.param`** (обязательно) — имя параметра в теле запроса, значение которого будет использоваться как ключ для отслеживания вхождений. Поддерживается извлечение из JSON и XML.
- **`occurrence.switchAt`** (обязательно) — номер вхождения, начиная с которого будет возвращаться второй ответ. Например, при `switchAt=3`:
  - 1-е и 2-е обращения → первый ответ
  - 3-е и последующие обращения → второй ответ
- **`occurrence.cleanupTimeMs`** (опционально) — время в миллисекундах, через которое ключ будет автоматически очищен после последнего использования. По умолчанию: `3600000` (1 час). Если ключ используется повторно до истечения времени, предыдущая задача очистки отменяется.

### Формат ответов

Ответы можно хранить двумя способами:

#### 1. Множественные файлы (приоритет)

Создайте файлы с именами `serviceName-1`, `serviceName-2`, и т.д. в папке `services/`:

```
services/
  example-1    # Первый ответ
  example-2    # Второй ответ
```

#### 2. Один файл с разделителями

Используйте разделители `-###-` в одном файле:

```
-###-
{"response": "first"}
-###-
-###-
{"response": "second"}
-###-
```

### Пример использования

**Конфигурация (`servicesParams.properties`):**
```properties
example.type=json
example.occurrence.key.param=orderId
example.occurrence.switchAt=3
example.occurrence.cleanupTimeMs=5000
```

**Файлы ответов:**
- `services/example-1`: `{"status": "pending"}`
- `services/example-2`: `{"status": "completed"}`

**Сценарий:**
1. Запрос с `{"orderId": "123"}` → ответ: `{"status": "pending"}` (1-е вхождение)
2. Запрос с `{"orderId": "123"}` → ответ: `{"status": "pending"}` (2-е вхождение)
3. Запрос с `{"orderId": "123"}` → ответ: `{"status": "completed"}` (3-е вхождение, переключение)
4. Запрос с `{"orderId": "123"}` → ответ: `{"status": "completed"}` (4-е вхождение)
5. Через 5 секунд после последнего запроса ключ `123` очищается
6. Запрос с `{"orderId": "123"}` → ответ: `{"status": "pending"}` (снова 1-е вхождение)

**Разные ключи отслеживаются отдельно:**
- Запрос с `{"orderId": "123"}` → счетчик для `123` = 1
- Запрос с `{"orderId": "456"}` → счетчик для `456` = 1 (независимо от `123`)

### Особенности

- **Потокобезопасность**: `OccurrenceTracker` использует `ConcurrentHashMap` и безопасен для использования в многопоточной среде.
- **Автоматическая очистка**: Предотвращает утечки памяти, автоматически очищая неиспользуемые ключи.
- **Отмена задач очистки**: При повторном использовании ключа до истечения времени очистки предыдущая задача отменяется и планируется новая.
- **Fallback**: Если ключ не найден в запросе или `OccurrenceTracker` недоступен, возвращается первый ответ.

---
