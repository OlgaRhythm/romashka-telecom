### Валидация

- номер телефона: состоит из цифр, максимальная длина - 15 символов (https://habr.com/ru/companies/tensor/articles/752122/)
- дата и время: ISO 8601, гггг-мм-ддTчч:мм:сс

### Алгоритм генерации

Генератор звонков
1. Читаем номера из базы данных
2. Генерация звонков и запись в базу данных

Генератор файлов
3. Получение отсортированного массива записей всех звонков
4. Формирование CDR файлов в формате CSV

Модуль отправки
5. По мере создания файлов - отправка в сервис BRT

Нужно гарантировать, что количество записей о звонках % 10 == 0
Записи, разделенные полночью, должны находиться в одном файле CDR

### Дополнительно

#### 1

Число 4 у пула потоков, потому что обычно берется количество ядер

#### 2

Абоненты в теории могут разговаривать сразу с несколькими людьми. 😁 Но обычно для этого нужно подключать специальную услугу. Оплата взымается с каждого звонка, как если бы они происходили в разное время.
Получается, можно не делать проверку в CDR генераторе, что абонент разговаривает только по одной линии?

https://www.bolshoyvopros.ru/questions/2777433-kak-razgovarivat-po-telefonu-s-dvumja-ljudmi-odnovremenno.html?ysclid=m9lj4pok4u824786197
https://mts-link.ru/blog/konferenc-svyaz-mts/

Тогда может сделать 2 режима работы, чтобы при запуске можно было контролировать, разрешаем ли абонентам разговаривать одновременно с несколькими людьми?

#### 3

Если абонент звонит на многоканальный телефонный номер, то в CDR записывается набранный номер или тот, на который произошла переадресация?

#### 4 

Потоки могут работать в случайной последовательности.
Можно генерировать очередь из времени начала звонков.

#### 5

Запуск контейнера:

docker compose down
gradle clean build
docker build -t romashka-telecom/cdr cdr
docker build -t romashka-telecom/brt brt
docker build -t romashka-telecom/hrs hrs
docker build -t romashka-telecom/crm crm
docker-compose up --build

// docker compose up -d
- для корректной работы RabbitMQ
docker-compose up --build
- для проверки базы данных:
docker run -d -p 8080:8080 --name romashka-telecom romashka-telecom

docker compose logs -f romashka-telecom

#### 6

Добавила ломбок в проект

#### 7

Если остаются звонки, не собирающиеся в пакет по 10, то они не отправляются


#### Модельное время CDR

В режиме Simulation нам важно не просто «отправить всё сразу», а эмулировать ход времени: записи CDR должны уходить в очередь так, как будто они формировались «в реальном времени», но с ускорением (или замедлением) по отношению к реальному ходу часов.

Для этого:
1. Задаем период моделирования и коэффициент

В году примерно 365 дней * 24 ч * 3600 с = 31 536 000 секунд.

В 3 минутах — 3 * 60 = 180 секунд.

Значит коэффициент = модельное_время_в_секундах / реальные_секунды = 31 536 000 / 180 ≈ 175 200.

Коэффициент задается в CDR в application.yml
и в docker-compose.yml

Если коэффициент равен 1.0, то система работает
в режиме реального времени и использует системное время

// TODO: это ещё нужно будет реализовать

2. Для каждого файла CDR берем время
   окончания последнего звонка
   и получаем точную дату-время в реальном мире,
   когда эта партия должна учти, чтобы
   время отправки соответствовало модельному

Отправка файлов происходит по счетчику,
с модельными секундами.

3. Планируем отправку через TaskScheduler
   используем Spring’s TaskScheduler.schedule(Runnable, Instant).

4. Динамическая переименование файлов
   При отправке через RabbitMQ в заголовок fileName автоматически вкладывается штамп модельного времени:

   