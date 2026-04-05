# RabbitMQ: Binding con routing key incorrecta en exchange direct
 
## Descripción
Al consumir mensajes desde un exchange de tipo `direct` en Quarkus 3.x con SmallRye Reactive Messaging, el binding entre el exchange y la cola se creaba con routing key `#` en lugar de `url_clicked`, impidiendo que los mensajes llegaran al consumidor.
 
## Causa raíz
SmallRye Reactive Messaging ignora la propiedad `exchange.routing-keys` en Quarkus 3.x al crear el binding, usando `#` como valor por defecto. Además, SmallRye se inicializa antes del `StartupEvent`, por lo que declara la topología (exchange, cola y binding) antes de que cualquier configuración programática pueda intervenir.
 
## Resolución
Se desactivó la declaración automática de topología por parte de SmallRye y se delegó a una clase de configuración que se ejecuta al inicio:
 
**application.properties:**
```properties
mp.messaging.incoming.url_clicked.exchange.declare=false
mp.messaging.incoming.url_clicked.queue.declare=false
mp.messaging.incoming.url_clicked.queue.bind=false
```
 
**RabbitMQConfig.java:**
```java
@ApplicationScoped
public class RabbitMQConfig {
 
    @Inject
    Vertx vertx;
 
    void onStart(@Observes StartupEvent ev) {
        RabbitMQOptions options = new RabbitMQOptions()
                .setHost("localhost")
                .setPort(5672)
                .setUser("admin")
                .setPassword("secret");
 
        RabbitMQClient client = RabbitMQClient.create(vertx, options);
 
        client.start()
              .flatMap(v -> client.exchangeDeclare("url_clicked_exchange", "direct", true, false))
              .flatMap(v -> client.queueDeclare("url_clicked_queue", true, false, false))
              .flatMap(v -> client.queueBind("url_clicked_queue", "url_clicked_exchange", "url_clicked"))
              .flatMap(v -> client.stop())
              .await().indefinitely();
    }
}
```
 
## Lecciones aprendidas
- En Quarkus 3.x, SmallRye no respeta `exchange.routing-keys` para exchanges `direct` y usa `#` por defecto.
- SmallRye declara la topología antes del `StartupEvent`, por lo que la configuración programática tardía no tiene efecto si no se deshabilita la automática.
- La propiedad `routing-key` (outgoing) y `exchange.routing-keys` (incoming) tienen propósitos distintos y no son intercambiables.
 
## Prevención
- En proyectos con Quarkus 3.x y exchanges `direct`, siempre manejar la topología de RabbitMQ de forma programática.
- Deshabilitar explícitamente `exchange.declare`, `queue.declare` y `queue.bind` en el properties para evitar que SmallRye interfiera.
- Verificar en RabbitMQ UI el tipo de exchange y la routing key del binding inmediatamente después del primer arranque. 