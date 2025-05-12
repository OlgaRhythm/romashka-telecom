## Romashka Telecom

### Оглавление
- Описание
- Модули проекта
- Установка и запуск
- Документация

### Описание
Romashka Telecom — это микросервисное приложение, моделирующее процессы мобильного оператора "Ромашка": фиксация звонков, тарификация, управление балансами абонентов и работа CRM.
#### Технологии разработки:
- Java 17
- Spring Boot, Spring Security, Spring Data, Spring Web, Spring Cloud, Spring AMQP
- Maven/Gradle
#### Тестирование:
- JUnit
- Mockito
- Spring Test
#### Инфраструктура:
- RabbitMQ
- PostgreSQL
- H2 Database
- Liquibase
- Docker

### Описание модулей

1. CDR Generator (Коммутатор)
Сервис генерирует случайные события звонков между абонентами, собирает их в CDR-файлы (формат txt/csv) и отправляет через брокер сообщений (RabbitMQ) в систему тарификации BRT.

2. BRT (Billing Real Time)
Сервис хранения информации о звонках и абонентах. Принимает CDR, сохраняет данные в базу и взаимодействует с HRS для расчёта стоимости звонков, а затем списывает средства с баланса абонентов.

3. HRS (High-performance Rating Server)
Сервис расчёта стоимости звонков на основе тарифов. Получает данные от BRT, производит расчёты в зависимости от типа тарифа ("Классика" или "Помесячный") и отправляет результаты обратно.

4. CRM
API-сервис для работы абонентов и менеджеров салонов связи: пополнение баланса, смена тарифа, регистрация новых абонентов, просмотр информации об абоненте.

### Документация

Подробная документация расположена в [doc/README.md](./doc/README.md)

### Установка 

1. Клонировать репозиторий:
`https://github.com/OlgaRhythm/romashka-telecom.git`
2. Перейти в директорию проекта:
`cd romashka-telecom`
3. Построить проект
`gradle clean build`
4. Создать образы контейнеров
`docker build -t romashka-telecom/cdr cdr`
`docker build -t romashka-telecom/brt brt`
`docker build -t romashka-telecom/hrs hrs`
`docker build -t romashka-telecom/crm crm`
5. Запустить контейнеры
`docker-compose up --build`
