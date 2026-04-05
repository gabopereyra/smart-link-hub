## Incidente: 401 Unauthorized por issuer (`iss`) inválido en JWT

### Descripción
Al realizar una petición autenticada a través del gateway, el sistema respondía con:

```
401 Unauthorized
error="invalid_token", error_description="The iss claim is not valid"
```

### Causa raíz
El token JWT emitido por Keycloak contenía un `iss` (issuer) distinto al configurado en el gateway.

* Issuer en el token:

  ```
  http://localhost:8180/realms/smartlink-hub
  ```

* Issuer esperado por el gateway:

  ```
  http://keycloak:8080/realms/smartlink-hub
  ```

Esta discrepancia se debe a que:

* El token fue generado usando la URL accesible desde el host (`localhost`)
* Pero los servicios dentro de Docker utilizan el hostname interno (`keycloak`)

Spring Security valida estrictamente que el `iss` del token coincida exactamente con el configurado, por lo que la diferencia provocó el rechazo del token.

### Solución

Se configuró correctamente el `Frontend URL` en Keycloak para alinear el issuer, esto se realiza en "Realm Settings > General > Frontend URL" 

```
http://keycloak:8080
```

De esta manera, los nuevos tokens emitidos contienen un `iss` consistente con el entorno Docker y válido para todos los servicios.

### Lecciones aprendidas

* El `issuer` de un JWT debe ser consistente entre todos los componentes del sistema
* En entornos Docker, evitar usar `localhost` como referencia entre servicios
* Ante errores de autenticación, inspeccionar los headers de respuesta al intentar consumir el protegido
* Spring Security realiza validaciones estrictas del issuer por motivos de seguridad

### Prevención

* Definir una única URL canónica para el Identity Provider
* Configurar explícitamente el `Frontend URL` en Keycloak en entornos containerizados
* Validar tokens (payload) durante debugging de problemas de autenticación
