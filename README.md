# Проектная работа 11 спринта

## Spring Boot приложение BankApp

### Технологии

```
Java 21
Spring Boot 3.5.4
PostgreSQL 16.9
Kafka
Maven
Testcontainers
Docker
Keycloak
Minikube
kubectl
Helm
Zipkin
```

### Требования к окружению

```
JDK 21
Клонировать репозиторий: https://github.com/ksa-git-01/bankapp.git
Установлен Docker Desktop
Установлен kubectl 
Установлен Minikube
Установлен Helm 
```

### Запуск тестов

Из корня проекта:

```
./mvnw clean test
```

Первый запуск может быть долгим, т.к. будут скачаны необходимые образы и зависимости

### Сборка и запуск
- Запустить Docker Desktop
- Запустить Minukube:
```
minikube delete
minikube start --cpus=6 --memory=4096 --driver=docker --insecure-registry="localhost:5000" --static-ip=192.168.49.2
```
- Включить аддон Ingress
```
minikube addons enable ingress
```
- Создать неймспейсы
```
kubectl create namespace test
kubectl create namespace prod
```
- Переключиться на docker daemon внутри миникуб
```
minikube docker-env | Invoke-Expression
```
- Из корня проекта собрать образы микросервисов
```
docker build -t accounts:latest -f accounts/Dockerfile .
docker build -t blocker:latest -f blocker/Dockerfile .
docker build -t cash:latest -f cash/Dockerfile .
docker build -t exchange:latest -f exchange/Dockerfile .
docker build -t frontui:latest -f frontui/Dockerfile .
docker build -t generator:latest -f generator/Dockerfile .
docker build -t notifications:latest -f notifications/Dockerfile .
docker build -t transfer:latest -f transfer/Dockerfile .
```
- Если в Helm еще не добавлены репозитории их нужно добавить командами:
```
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add openzipkin https://openzipkin.github.io/zipkin
```

- Перейти в каталог с зонтичным хелмчартом и установить его в кластер
```
cd helm/umbrella-chart

helm install bankapp . -n test --set accounts.keycloak.client.secretValue=ACCOUNTS-client-secret-123456 --set cash.keycloak.client.secretValue=CASH-client-secret-123456 --set frontui.keycloak.client.secretValue=FRONTUI-client-secret-123456 --set generator.keycloak.client.secretValue=GENERATOR-client-secret-123456 --set transfer.keycloak.client.secretValue=TRANSFER-client-secret-123456
# или
helm install bankapp . -n prod --set accounts.keycloak.client.secretValue=ACCOUNTS-client-secret-123456 --set cash.keycloak.client.secretValue=CASH-client-secret-123456 --set frontui.keycloak.client.secretValue=FRONTUI-client-secret-123456 --set generator.keycloak.client.secretValue=GENERATOR-client-secret-123456 --set transfer.keycloak.client.secretValue=TRANSFER-client-secret-123456

# или обновить, если чарт уже устанавливался ранее 
helm upgrade bankapp . -n test
helm upgrade bankapp . -n prod
```
Для работы с приложением нужно дождаться полного запуска всех подов, это может занять пару минут

- После первого запуска, дождаться пока запустится pod с кафкой и создать топики
``` 
kubectl exec -it kafka-0 -n test -- /opt/kafka/bin/kafka-topics.sh --create --topic notifications-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
kubectl exec -it kafka-0 -n test -- /opt/kafka/bin/kafka-topics.sh --create --topic exchange-rates --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```
- Если нужен доступ к приложению через браузер хоста, то создать тоннель в отдельном терминале.
Окно не закрывать
```
minikube tunnel
# после создания тоннеля приложение будет доступно по адресу http://127.0.0.1/frontui/
```
- Также в отдельном терминале можно запустить веб-интерфейс миникуба.
После запуска окно не закрывать:
```
minikube dashboard --url
# в терминале будет выведен временный url веб-интерфейса миникуба 
```
- Если хотим перейти в веб-интерфейс кейклока, необходимо в отдельном окне терминала пробросить порт.
Окно не закрывать
```
kubectl port-forward -n test service/bankapp-keycloak 18080:8080
```
- Если хотим перейти в веб-интерфейс Zipkin, необходимо в отдельном окне терминала выполнить команду (Окно не закрывать)
```
minikube service bankapp=zipkin --url -n test
# в терминале будет выведен временный url веб-интерфейса Zipkin 
```


Используются порты ОС хоста:
- 8080..8088 - Сервисы
- 15433 - PostgreSQL keycloak
- 15432 - PostgreSQL Accounts
- 18080 - Веб-интерфейс keycloak
- 8500 - Consul 

### Ресурсы проекта

```
Веб-страницы:
Главная страница: http://127.0.0.1:8080
Страница регистрации пользователя: http://127.0.0.1:8080/signup

Публичная информация о реалме keycloak:
http://localhost:18080/realms/bankapp
```

### Схема взаимодействия
```
Все сервисы общаются между собой по HTTP по имени сервиса через внутренний DNS k8s кластера
Браузер общается с FrontUI через Ingress Controller 
```

### FLOW аутентификации и авторизации пользователя
```
Согласно заданию логины и пароли пользователей лежат в БД сервиса Accounts. 
Accounts также является сервисом аутентификации пользователя.
Для авторизации пользователей выбрана STATELESS политика сессий. 
Вместо использования веб-сессий, сервис Accounts генерирует JWT токен пользователя и подписывает его 
по алгоритму RS256.
RS256 это асимметричный алгоритм подписи который использует приватный ключ. 
Приватный ключ лежит в файле:
accounts/src/main/resources/certs/private.pem
Файл помещен в репозиторий только для упрощения локального запуска. В реальном проекте он бы 
находился в Vault хранилище.
Для валидации токена используется публичный ключ.
Публичный ключ скопирован в каждый сервис в /src/main/resources/certs/public.pem

FrontUI при попытке логина обращается к Accounts через RemoteAuthenticationProvider на эндпоинт 
api/auth, передавая логин и пароль из браузера.
Account проверяет пару логин/пароль и генерирует JWT токен с инф об id пользователя, 
логином и ролью.
FrontUI помещает токен в http-only cookie "JWT-TOKEN" с помощью CustomAuthenticationSuccessHandler 
и отдает браузеру.
Далее если из браузера приходит cookie "JWT-TOKEN", токен валидируется публичным ключем. 
Если токен подделан, то выполняется редирект на страницу логина.
Далее при обращении FrontUI в любой другой сервис происходит добавление пользовательского токена 
в header "X-User-JWT".
Каждый сервис получая запрос валидирует токен из header'а и если все ок, помещает информацию 
из токена в атрибут userId объекта HttpServletRequest и разрешает доступ к эндпоинтам.
Далее в каждом сервисе где это необходимо, из HttpServletRequest извлекается userId из токена 
и сравнивается с userId, который пришел в параметрах http запроса. 
Таким образом получить доступ к данным пользователь может только при наличии валидного токена 
и только в случае если он делает запрос за "своими" данными.
Пример такой проверки в контроллере:
        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");

        if (authenticatedUserId == null || !request.fromUserId().equals(authenticatedUserId)) {
            return ResponseEntity.status(403).body(
                    new TransferResponse(false, "Access denied", null, null)
            );
        }
```

### FLOW межсервисной авторизации по OAuth2
```
Параллельно с пользовательской авторизацией в приложении реализована межсервисная авторизация 
по OAuth2 через Keycloak.
Тут используется стандартный механизм с передачей токена в заголовке Authorization: Bearer ....
Для межсервисной авторизации переиспользуется механизм, который создавался в заданиях прошлого 
модуля, но с доработкой:
теперь каждый клиент имеет роли доступа к тем или иным сервисам и в каждом сервисе добавлена 
проверка роли в веб фильтр,например: .requestMatchers("/api/**").hasRole("cash-access")
Таким образом к сервису cash могут обращаться только сервисы с ролью cash-access 
и т.д для остальных сервисов.

Для того чтобы поженить между собой два независимых flow авторизации используется метод 
dualTokenInterceptor в конфигурации RestTemplate.
Этот метод выполняет проверку на наличие валидного токена из кейклока. 
Если него нет, идет в кейклок за новым, добавляет его в заголовок Authorization 
и также добавляет заголовок X-User-JWT куда прописывает токен пользователя.
```