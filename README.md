# Reservas API

API Spring Boot para gerenciamento de reserva de ingressos com fluxo completo: criar evento → reservar ingresso → confirmar pagamento → obter tickets.


## Exercício 1

Excutar a aplicação no Docker conectand no Postgres.

Passo 0 - Opcional - Limpar o Docker

```
docker system prune -a
```

Passo 1 - Criar a rede Docker

```
docker network create -d bridge rede
```

Passo 2 - Executar o Postgres

```
docker run -d --name postgres-aula -e POSTGRES_DB=auladb -e POSTGRES_USER=usuario -e POSTGRES_PASSWORD=senha --network rede -p 5432:5432 postgres
```

Passo 3 - Mudar o application.properties


```
spring.datasource.url=jdbc:postgresql://postgres-aula:5432/auladb
spring.datasource.username=usuario
spring.datasource.password=senha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Passo 3.1 - Adicionar dependência do postgres e remover  a do H2

```
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
```

Passo 4 - Construir a imagem

```
docker build -t reservas .
```

Passo 5 - Executar a imagem

```
docker run -p 8081:8081 --name reservas --network rede reservas
```

Passo 6 - Mudar novamente o application.properties

```
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/auladb
spring.datasource.username=usuario
spring.datasource.password=senha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Passo 7 - Construir a imagem novamente

```
docker build -t reservas .
```

Passo 8 - Executar a imagem novamente

```
docker run -p 8081:8081 -e DB_HOST=postgres-aula --network rede --name reservas reservas
```

## Exercício 2

Executar a aplicação também na AWS

Passo 1 - Criar a network na máquina da aws


```
docker network create -d bridge rede
```

Passo 2 

```
docker run -d --name postgres-aula -e POSTGRES_DB=auladb -e POSTGRES_USER=usuario -e POSTGRES_PASSWORD=senha --network rede -p 5432:5432 postgres
```

Passo 3 - Escrever o pipeline que sobe a aplicação para a AWS, se basear no que a gente fez para o projeto anterior: https://github.com/ezambomsantana/projeto-software-2026-2-pagamento/blob/main/.github/workflows/deploy.yml

Lembrar de adicionar o --network (não fizemos isso no exercício anterior) e também o -e com o DB_HOST=postgres-aula

Adicionar também a senha como variável de ambiente, precisa colocar no application.properties e também colocar o -e como parâmetro do docker run.

Passo 4 - Colocar todas as secrets nno GitHub, DOCKERHUB_TOKEN, HOST_TEST, KEY_TEST, DB_PASSWORD,...
