# Decisión: Usar Repository Pattern en lugar de Active Record con Panache

- Estado: Aceptado
- Fecha: 20/03/2026

## Contexto y problema

El proyecto utiliza Quarkus con Panache para la capa de persistencia. Panache ofrece dos enfoques principales para interactuar con la base de datos:

- Active Record (mediante `PanacheEntity`)
- Repository Pattern (mediante `PanacheRepository`)

Es necesario definir un enfoque consistente para todo el proyecto que permita mantener calidad de código, testeo adecuado y una arquitectura sostenible a largo plazo.

## Drivers de la decisión

- Necesidad de testear lógica de negocio mediante tests unitarios sin depender de la base de datos
- Mantener una separación clara entre dominio y acceso a datos
- Alinear el diseño con principios de Clean Architecture
- Aprovechar la experiencia previa del equipo en arquitecturas en capas (Service / Repository / Entity)
- Facilitar la evolución futura del sistema (cambios de ORM o fuentes de datos)

## Opciones consideradas

### 1. Panache Entity (Active Record)

La entidad extiende `PanacheEntity` y contiene sus propios métodos de persistencia (`persist()`, `find()`, `delete()`, etc.).

### 2. Panache Repository

Una clase separada implementa `PanacheRepository<T>` y centraliza el acceso a datos. La entidad se mantiene como un POJO sin lógica de persistencia.

## Decisión

Se adopta el uso de **Panache Repository** como patrón estándar para la capa de persistencia.

## Justificación

- **Realización de tests**  
  Permite mockear la capa de repositorio fácilmente en tests unitarios.  
  El enfoque Active Record dificulta el mocking debido al uso de métodos estáticos.

- **Separación de responsabilidades**  
  Las entidades permanecen como modelos de dominio puros, sin lógica de acceso a datos, alineándose con Clean Architecture.

- **Familiaridad del equipo**  
  El equipo tiene experiencia previa con patrones similares (por ejemplo, Spring Data), lo que reduce la curva de aprendizaje.

- **Flexibilidad y mantenibilidad**  
  Cambios en la tecnología de persistencia o en la fuente de datos quedan encapsulados en la capa de repositorios.

## Alternativas descartadas

### Panache Entity (Active Record)

- Mezcla lógica de dominio con persistencia
- Dificulta el testing unitario
- Menor alineación con arquitecturas desacopladas

## Consecuencias

### Positivas

- Tests unitarios más simples, rápidos y aislados
- Modelo de dominio más limpio y mantenible
- Mayor coherencia arquitectónica
- Menor acoplamiento entre capas

### Negativas / Trade-offs

- Incremento en la cantidad de clases (un repositorio por entidad)
- Mayor boilerplate inicial
- Se pierde la simplicidad de uso directo (`Entity.persist()`, `Entity.find()`)

## Implicaciones

- Todo acceso a datos debe realizarse a través de repositorios
- Las entidades no deben contener lógica de persistencia
- Los servicios deben depender de interfaces de repositorio (facilita mocking y testing)
- Se recomienda mantener repositorios lo más simples posible (sin lógica de negocio)

## Referencias

- https://quarkus.io/guides/hibernate-orm-panache#solution-2-using-the-repository-pattern
- https://quarkus.io/guides/hibernate-orm-panache#pojo-with-a-repository