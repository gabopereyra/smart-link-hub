# Decisión: Uso de `Uni<T>` como tipo de retorno en endpoints reactivos con Quarkus
Estado: Aceptada
Fecha: 2026-04-05

## Contexto y problema
En el desarrollo de una API REST con Quarkus y MongoDB (via Reactive Panache), surge la decisión de qué tipo de retorno utilizar en los endpoints: el clásico `Response` de JAX-RS —equivalente al `ResponseEntity` de Spring— o `Uni<T>` del stack reactivo de Mutiny.

La elección impacta directamente en el modelo de concurrencia, el rendimiento bajo carga y la coherencia con el driver de base de datos utilizado.

## Drivers de la decisión
- El proyecto utiliza `ReactivePanacheMongoEntity`, que expone una API completamente reactiva basada en `Uni` y `Multi`.
- Se requiere alta concurrencia con el menor consumo de hilos posible.
- El framework elegido es RESTEasy Reactive, que soporta nativamente tipos de retorno reactivos.
- Bloquear hilos en un stack reactivo anula los beneficios de adoptar dicho stack.

## Opciones consideradas
### Opción A — `Uni<T>` (reactivo)
- El hilo se libera inmediatamente al recibir la request.
- Quarkus suscribe al `Uni` y responde cuando el resultado está disponible.
- Coherente con el driver reactivo de MongoDB.

### Opción B — `Response` (bloqueante)
- Familiar para desarrolladores con experiencia en JAX-RS o Spring.
- Requiere `.await().indefinitely()` para obtener el resultado del `Uni`, bloqueando el hilo.
- Rompe la cadena reactiva y elimina el beneficio de concurrencia.

## Decisión
Se adopta **`Uni<T>`** como tipo de retorno estándar en todos los endpoints que interactúan con drivers reactivos (MongoDB Reactive, Hibernate Reactive, Reactive PG Client).

Para control de códigos HTTP y headers se utiliza **`Uni<RestResponse<T>>`**, que es el equivalente reactivo de `ResponseEntity` en Spring.

## Justificación
La justificación central es la **coherencia de modelo**. Al usar `ReactivePanacheMongoEntity`, cada operación de base de datos devuelve un `Uni`. Forzar una respuesta síncrona implica bloquear ese `Uni`:

```java
// Hilo bloqueado: Rompe el modelo reactivo
public Response getSummary() {
    List<AliasSummary> result = ClickEvent.mongoCollection()
        .aggregate(...).collect().asList()
        .await().indefinitely(); // hilo bloqueado aquí
    return Response.ok(result).build();
}

// Coherente con el stack reactivo
public Uni<List<AliasSummary>> getSummary() {
    return ClickEvent.mongoCollection()
        .aggregate(...).collect().asList();
}
```

Cuando se necesita control del código HTTP, la solución es `Uni<RestResponse<T>>`:

```java
public Uni<RestResponse<AliasSummary>> getByAlias(String alias) {
    return AliasSummary.find("alias", alias).firstResult()
        .map(result -> result != null
            ? RestResponse.ok(result)
            : RestResponse.status(Response.Status.NOT_FOUND));
}
```

## Alternativas descartadas
**`Response` bloqueante:** requiere `.await().indefinitely()`, lo cual bloquea el event loop de Vert.x subyacente y convierte un endpoint reactivo en uno bloqueante, degradando la concurrencia bajo carga.

## Consecuencias
### Positivas
- El hilo queda libre mientras MongoDB procesa la query, disponible para otras requests.
- Mayor throughput bajo carga sin necesidad de escalar el pool de hilos.
- Código coherente de extremo a extremo: driver reactivo → servicio → endpoint.
- `RestResponse<T>` permite controlar código HTTP, headers y body manteniendo el modelo reactivo.

### Negativas / Trade-offs
- Curva de aprendizaje para equipos habituados a modelos sincrónicos (Spring MVC, Hibernate ORM clásico).
- El manejo de errores requiere familiaridad con operadores de Mutiny (`onFailure`, `recoverWithItem`, etc.).
- Los tests deben usar `.await().atMost(...)` o `UniAsserter` para no bloquear el hilo de test.

## Implicaciones
Esta decisión aplica únicamente cuando el **driver de base de datos es reactivo**. La elección del tipo de retorno no depende de la base de datos en sí, sino del driver utilizado:

| Driver | Tipo | Retorno natural |
|---|---|---|
| Reactive MongoDB | Reactivo | `Uni` / `Multi` |
| Reactive PostgreSQL (`reactive-pg-client`) | Reactivo | `Uni` / `Multi` |
| Hibernate ORM clásico | Bloqueante | `Response` / objeto directo |
| Hibernate Reactive | Reactivo | `Uni` / `Multi` |

Si en el futuro se incorpora Hibernate ORM clásico para otro módulo, sus endpoints pueden usar tipos síncronos sin conflicto mediante `@Blocking`.

## Referencias
- [Quarkus — Mutiny primer](https://quarkus.io/guides/mutiny-primer)
- [Quarkus — MongoDB with Panache](https://quarkus.io/guides/mongodb-panache)
- [Mutiny — Uni API](https://smallrye.io/smallrye-mutiny/latest/reference/uni/)
- [RESTEasy Reactive — RestResponse](https://quarkus.io/guides/resteasy-reactive)
