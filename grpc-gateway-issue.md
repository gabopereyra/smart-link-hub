# Conflicto gRPC en Spring Cloud Gateway

## Problema

Al intentar integrar trazado distribuido con Jaeger usando OTLP/gRPC, la aplicación no propagaba la traza, asi mismo se intento sumar netty para adecuarlo pero ya fallaba el arranque indicando el siguiente error:

```
Caused by: java.lang.NoClassDefFoundError: io/grpc/netty/NettyChannelBuilder
Caused by: java.lang.ClassNotFoundException: io.grpc.netty.NettyChannelBuilder
```

## Causa raíz
Spring Cloud Gateway ya incluye internamente su propia implementación de gRPC usando el paquete `io.grpc.netty`. Al agregar `grpc-netty-shaded` para el transporte OTLP, se generaba un conflicto de classpath porque esa dependencia usa el paquete `io.grpc.netty.shaded` — un namespace diferente e incompatible.

Intentar reemplazarlo por `grpc-netty` (sin shaded) alinea los paquetes, pero sigue existiendo riesgo de colisión de versiones con la gRPC interna del gateway.

**En resumen: Spring Cloud Gateway y cualquier dependencia gRPC externa no conviven bien.**

## Solución
Eliminar toda dependencia de transporte gRPC y usar **OTLP HTTP/Protobuf** en su lugar. No requiere ninguna librería extra y no genera ningún conflicto de classpath.

```yaml
management:
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
      protocol: http/protobuf
```

## Jaeger y los puertos
Jaeger expone dos puertos de ingesta simultáneamente:

- `4317` → OTLP/gRPC
- `4318` → OTLP HTTP/Protobuf

Jaeger es lo suficientemente inteligente para correlacionar trazas sin importar por qué puerto llegaron los spans. Lo que une los spans de distintos servicios es el `traceId`, no el protocolo. Por lo tanto, si el gateway exporta por `4318` y otro servicio exporta por `4317`, Jaeger igualmente arma la traza completa en su UI.
