# RabbitMQ: Mensajes del Emitter no llegaban a la cola

## Descripción
Al publicar mensajes desde un `Emitter` de Quarkus/SmallRye hacia RabbitMQ, los mensajes se enviaban sin errores pero nunca llegaban al exchange ni a la cola destino.

## Causa raíz
Uso incorrecto de la propiedad `routing-key` en el canal outgoing. En SmallRye RabbitMQ estas dos propiedades tienen roles distintos:

| Propiedad | Uso |
|---|---|
| `routing-key` | Define el binding key al **consumir** (incoming) |
| `default-routing-key` | Define la clave usada al **publicar** (outgoing) |

Usar `routing-key` en el outgoing hace que el conector no sepa con qué clave enrutar el mensaje hacia el exchange, descartándolo silenciosamente.

**Configuración incorrecta:**
```properties
mp.messaging.outgoing.url_clicked_out.routing-key=url_clicked
```

**Configuración correcta:**
```properties
mp.messaging.outgoing.url_clicked_out.default-routing-key=url_clicked
```

## Lecciones aprendidas
- RabbitMQ acepta la conexión y no loga errores cuando un mensaje se descarta por falta de routing key, lo que hace el problema muy difícil de detectar.
- SmallRye diferencia las propiedades de incoming y outgoing aunque parezcan equivalentes.
- El `Emitter.send()` completando sin excepción no garantiza que el mensaje llegó al broker.

## Prevención
- Revisar la documentación de SmallRye RabbitMQ distinguiendo explícitamente las propiedades de `incoming` vs `outgoing`.
- Para outgoing, usar siempre `default-routing-key`.
- Monitorear el contador de mensajes publicados en RabbitMQ UI (Message rates en el exchange) como primer paso de diagnóstico.
- Habilitar logs detallados del conector en desarrollo: `quarkus.log.category."io.smallrye.reactive.messaging".level=DEBUG`
