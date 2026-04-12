# ADR-0004: Circuit Breaker para el Servicio de Análisis de IA
Estado: Aceptada
Fecha: 2026-04-12

## Contexto
El sistema delega el análisis de seguridad de URLs a un servicio externo de IA (`aiServiceApi`). Este servicio, como cualquier dependencia de red o tercero, es inherentemente no confiable en términos de disponibilidad: puede sufrir caídas, latencia elevada, timeouts o errores transitorios.

Si el sistema llamara al servicio de IA de forma directa y sin protección, cualquier indisponibilidad del mismo se propagaría en cascada hacia los usuarios finales, degradando o bloqueando por completo la capacidad de analizar URLs. Esto viola el principio de **resiliencia**: una dependencia que falla no debería tumbar el sistema que la consume.

Los puntos de riesgo identificados fueron:
- El servicio de IA puede no estar disponible durante despliegues, mantenimientos o incidentes.
- Bajo alta carga, el servicio puede responder lentamente o con errores intermitentes.
- Sin protección, los reintentos continuos a un servicio caído aumentan la presión sobre él y sobre el sistema consumidor.
- La ausencia de respuesta no debería impedir que el sistema funcione, aunque sea en modo degradado.

## Decisión
Se implementó un **Circuit Breaker** con **Fallback** sobre el método `analyzeUrl`, utilizando las anotaciones de MicroProfile Fault Tolerance (`@CircuitBreaker` y `@Fallback`).

```java
@CircuitBreaker(
        requestVolumeThreshold = 5,
        failureRatio = 0.5,
        delay = 10,
        delayUnit = ChronoUnit.SECONDS
)
@Fallback(fallbackMethod = "isSafeFallback")
public UrlAnalysisResult analyzeUrl(String url) {
    Log.info(">>> Calling AI service...");
    UrlAnalysisResult result = aiServiceApi.analyze(new UrlAnalysisRequest(url));
    Log.info(">>> AI service successful invocation");
    return result;
}

public UrlAnalysisResult isSafeFallback(String url) {
    Log.warn(">>> FALLBACK AI service for: " + url);
    return new UrlAnalysisResult(true, "AiService not available, assume url it is safe");
}
```

### Parámetros del Circuit Breaker

| Parámetro                | Valor | Significado                                                                 |
|--------------------------|-------|-----------------------------------------------------------------------------|
| `requestVolumeThreshold` | 5     | Se necesitan al menos 5 llamadas para evaluar si abrir el circuito.         |
| `failureRatio`           | 0.5   | Si el 50% o más de las últimas llamadas fallan, el circuito se abre.        |
| `delay`                  | 10 s  | El circuito permanece abierto 10 segundos antes de intentar recuperarse.   |

### Comportamiento del Fallback
Cuando el circuito está abierto —o cuando ocurre una falla individual antes de que se abra— la llamada es interceptada y se ejecuta `isSafeFallback`. Este método devuelve un resultado que asume la URL como **segura**, permitiendo que el flujo continúe sin interrumpir la experiencia del usuario.

Esta decisión de fallback (asumir seguro) es conservadora en términos de UX: se prefiere no bloquear una URL legítima por culpa de una indisponibilidad del servicio de IA, antes que bloquear al usuario sin razón válida donde el trade-off de seguridad fue evaluado y aceptado.

## Consecuencias
### Positivas
- **Resiliencia**: el sistema sigue operando aunque el servicio de IA esté caído.
- **Protección del servicio dependiente**: el circuito abierto evita bombardear con reintentos a un servicio que ya está fallando.
- **Experiencia de usuario preservada**: las URLs continúan siendo procesadas, aunque sin análisis de IA.
- **Observabilidad**: los logs de warn en el fallback permiten detectar y monitorear cuándo el circuito se activa.

### Negativas / Trade-offs
- **Degradación silenciosa**: durante el fallback, las URLs se clasifican como seguras sin análisis real. Si el servicio de IA estaba detectando amenazas, esa protección queda temporalmente desactivada.
- **Ventana de riesgo**: los 10 segundos de `delay` implican que el circuito permanece abierto ese tiempo mínimo, incluso si el servicio de IA se recupera antes.
- **Sin reintentos automáticos individuales**: la configuración actual no incluye `@Retry`. Si se requiere mayor tolerancia a fallas transitorias puntuales, podría complementarse con una política de reintentos acotada.

## Alternativas Consideradas
| Alternativa                          | Motivo de descarte                                                                 |
|--------------------------------------|------------------------------------------------------------------------------------|
| Llamada directa sin protección       | Propaga fallos al usuario; inaceptable para un sistema resiliente.                |
| Timeout simple                       | Protege contra latencia pero no contra fallos en ráfaga ni acumulación de carga.  |
| Cola de mensajes asíncrona           | Introduce complejidad operacional innecesaria para este caso de uso sincrónico.   |
| Cache de resultados previos          | Útil como complemento, pero no reemplaza la necesidad de gestionar el circuito.   |

## Referencias
- [MicroProfile Fault Tolerance — @CircuitBreaker](https://download.eclipse.org/microprofile/microprofile-fault-tolerance-4.0/microprofile-fault-tolerance-spec-4.0.html#_circuit_breaker)
- [MicroProfile Fault Tolerance — @Fallback](https://download.eclipse.org/microprofile/microprofile-fault-tolerance-4.0/microprofile-fault-tolerance-spec-4.0.html#_fallback)
- [Martin Fowler — Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)