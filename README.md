Play App for rapid development
==============================

#### Raw facts
* Java 8 required
* Scala 2.12.2
* Play 2.6.0 with compile time dependency injection
* Slick 3.2 (`without play-slick extension`) with help from [pregenerated common repository calls](https://github.com/gonmarques/slick-repo)
* Postgres 9.6.2 with [Slick Postgres extensions](https://github.com/tminglei/slick-pg)
* Cats 0.9.0
* Monix 2.3.0

#### Goals

- [x] dependency injection of some form
- [x] build setting
- [x] ready to deploy from the get-go
- [x] logging ready and configured
- [x] flyway migrations https://flywaydb.org/
- [x] data persistence
- [x] Ability to Dockerize application
- [x] CRUD REST API for a dummy resource
- [ ] Swagger specification for dummy resource
- [x] migration to Play 2.6
- [X] tests
- [x] static views
- [ ] split project into core and web modules
- [ ] example of versioned endpoints - path or header negotiation?
- [ ] CRSF
- [ ] SSL
- [ ] base authentication with lib like https://github.com/mohiva/play-silhouette
- [ ] 3rdParty service call with build in timeout and circuit breaker
- [ ] update README with information where are defined useful utils and implicit transformations
- [ ] clear all code TODOs
- [ ] make public and private health check endpoint (based on existing `/healthcheck`)
- [ ] provide [giter8](https://github.com/foundweekends/giter8) support

### Database for local development

##### Setting up

Project uses PostgreSQL 9.6. In the project's root directory run
```
docker-compose up -d
```

##### Migration using Flyway
```
sbt -Dflyway.url="jdbc:postgresql://127.0.0.1:5432/rapid-development" -Dflyway.user=postgres-dev -Dflyway.password=secretpass flywayMigrate
```

### Available endpoints

#### /healthcheck
Status of the application itself, additionally shows build data.
* `GET /healthcheck?diagnostic=true` - status of the application and all connected external sources like database etc.

### Example API: manipulating notes

* `GET /notes?limit=10&offset=0` - find all available notes with required pagination. Returns `200` or `500`.
* `GET /notes/:noteId` - find given note. Returns `200`, `404` or `500`.
* `PUT /notes/:noteId` - update given note. Returns `200`, `400`, `404` or `500`.
    Expected body

        {
          "creator": String, lenght: 3 -> 100,
          "note": String, lenght: 1 -> 5000,
        }

* `POST /notes` - create new note. Returns `201`, `400` or `500`.
    Expected body

        {
          "creator": String, lenght: 3 -> 100,
          "note": String, lenght: 1 -> 5000,
        }
        
### Modularization of rapid-development app

As `rapid-development` has been designed as a template for creating applications we divided it into submodules
to help separated concerns. As for now there are 2 submodules:

* `core` - this is where business log lives. There should be nothing Play-specific there. The main goal is to be able to
change HTTP layer to different framework/library (e.g. `akka-http`) without changing `core` at all. You can see some 
`play` in some imports there but that's just because `play-json` has been chosen as a JSON library.
* `web` - this is where HTTP-specific things are implemented: routing, destructuring HTTP requests, HTTP return codes 
and so on. This layer should communicate with `core` using business objects.

`core` contains of two main packages: `io.scalac.common` and `io.scalac.domain`. The first one is responsible for
handling cross-cutting concerns as e.g. logging while the second one is responsible for
pure business logic. In future `core` may be splitted into two modules.

### Known issues after migration to Play 2.6

        SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
        SLF4J: Defaulting to no-operation (NOP) logger implementation
        SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.

Issue: https://github.com/playframework/playframework/issues/7422
Fix: https://github.com/playframework/playframework/pull/7534 (looks like in Play 2.6.1)
