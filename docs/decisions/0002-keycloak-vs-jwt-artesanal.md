# Decisión: Usar Keycloak en lugar de implementación JWT artesanal
Estado: Aceptado
Fecha: 27/03/2026

## Contexto y problema

El sistema requiere implementar autenticación y autorización para proteger los distintos microservicios.

Existen dos enfoques principales:

- Implementar una solución propia basada en JWT (generación, validación, expiración, refresh, manejo de usuarios, roles, etc.)
- Utilizar un proveedor de identidad externo como Keycloak, que implementa estándares como OAuth 2.0 y OpenID Connect

Es necesario definir un enfoque que garantice seguridad, mantenibilidad y velocidad de desarrollo.

## Drivers de la decisión
- Evitar implementar lógica de seguridad crítica desde cero
- Reducir riesgos asociados a errores en autenticación/autorización
- Acelerar el desarrollo inicial del sistema
- Soportar estándares ampliamente adoptados (OAuth2 / OIDC)
- Facilitar la gestión de usuarios, roles y permisos
- Preparar el sistema para escenarios futuros (SSO, federación, etc.)

## Opciones consideradas
1. JWT artesanal

Implementación propia de autenticación basada en JWT:
- Generación de tokens
- Validación de firmas
- Manejo de expiración y refresh tokens
- Gestión de usuarios, credenciales y roles
- Implementación de endpoints de login

2. Keycloak
Uso de Keycloak como Identity Provider (IdP), delegando:
- Autenticación
- Emisión y validación de tokens
- Gestión de usuarios
- Manejo de roles y permisos
- Flujos estándar (client credentials, password, authorization code, etc.)

## Decisión

Se adopta el uso de Keycloak como solución de autenticación y autorización del sistema.

## Justificación
- Reducción de complejidad: Implementar JWT de forma correcta implica manejar múltiples aspectos sensibles (firmas, expiración, refresh, revocación, etc.). Keycloak ya resuelve estos problemas de forma robusta.
- Seguridad: La seguridad es un dominio crítico. Una implementación artesanal aumenta el riesgo de vulnerabilidades. Keycloak es una herramienta madura, ampliamente utilizada y probada.
- Soporte de estándares: Permite trabajar directamente con OAuth 2.0 y OpenID Connect, evitando implementaciones ad-hoc.
- Funcionalidades listas para usar:
    - Gestión de usuarios
    - Roles y permisos
    - Login flows configurables
    - Integración con proveedores externos (Google, LDAP, etc.)
    - Single Sign-On (SSO)
- Escalabilidad y evolución: Facilita la incorporación futura de:
    - Multi-tenancy (realms)
    - Federated identity
    - Políticas avanzadas de acceso

## Alternativas descartadas
JWT artesanal
- Alta complejidad de implementación
- Mayor probabilidad de errores de seguridad
- Necesidad de mantener lógica crítica internamente
- Requiere implementar features que ya existen (login, refresh tokens, revocación, etc.)
- Difícil alineación con estándares si no se implementan completamente

## Consecuencias
### Positivas
- Reducción significativa del tiempo de desarrollo
- Mayor nivel de seguridad desde el inicio
- Uso de estándares de la industria
- Menor carga de mantenimiento en lógica de autenticación
- Centralización de la seguridad

### Negativas / Trade-offs
- Dependencia de un componente externo (Keycloak)
- Necesidad de operar y mantener la infraestructura del IdP
- Curva de aprendizaje inicial (realms, clients, roles, flows)
- Overhead en entornos simples donde una solución básica podría ser suficiente

## Implicaciones
- Todos los servicios deben validar tokens emitidos por Keycloak
- La autenticación no se implementa dentro de los microservicios
- La autorización debe basarse en claims del token (roles, scopes)
- Se deben definir correctamente:
    - Realms
    - Clients
    - Roles (realm y client)

## Referencias
- https://www.keycloak.org/documentation
- https://datatracker.ietf.org/doc/html/rfc6749 (OAuth 2.0)
- https://openid.net/specs/openid-connect-core-1_0.html