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

---

## com.LT.restDummy.domain.model
- **StubService**
  - Модель заглушаемого сервиса: имя, тип (`json|xml`), endpoint, системное имя.
  - Доступность + планировщик доступности.
  - `DelayConfig` — конфиг задержек.
  - `responses` — список вариантов ответа (`List<StubResponse>`).
  - `responseType` — стратегия выбора ответа (`ResponseType`).

---

## com.LT.restDummy.domain.response
- **ResponseType** — стратегии выбора ответа:
  - `DEFAULT` — всегда первый ответ.
  - `THRESHOLD` — по весам (вероятностно, на основании `key` у ответа).
  - `PARAM_BASED` — по значению параметра (name/value) из тела запроса.

- **StubResponse**
  - Описывает один ответ: контент (JSON/XML строка), тип, ключ/вес (`key`), имя/значение параметра (для `PARAM_BASED`).

- **ResponseResolver**
  - Выбор подходящего `StubResponse` из `StubService` по стратегии (`ResponseType`).
  - Обрабатывает пустые списки/несоответствия, возвращает fallback.

- **ResponseBuilder**
  - Собирает `List<StubResponse>` для `StubService` из текста заглушки и параметров (`servicesParams.properties`).
  - Определяет `responseType` сервиса и расставляет служебные поля ответов.

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
