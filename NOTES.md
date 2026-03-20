# Semana 1
## Cosas aprendidas
- Porque utilizar una excepcion como ¨jakarta.ws.rs.NotFoundException¨ no es correcto que sea lanzada en el servicio, es una excepcion pensada para ser utilizada en la capa de presentacion (controlador para nuestro caso). Esto nos llevo a repasar el concepto de excecpciones personalizadas, de dominio, y como mapearlas para generar una el objeto Response correcto cuando se producen las mismas.
- Utilizacion de Object Value para aplicar validaciones propias de dominio
- Conflicto de zona horaria para conectar base de Postgres en un entorno Windows. Puede que Windows (10 en mi caso) tenga la zona horaria expresada en un formato no esperado por la base de postgres y esto genera conflictos a la hora de conectar con la misma.
Para el caso Postgres esperaba un timezone como el siguiente "America/Argentina/Buenos_Aires" y el equipo tiene "America/Buenos_Aires"
Para solventar el incidente por un lado con DBeaver modficamos el archivo .init, agregando la propiedad `Duser.timezone`con el valor esperado y en el caso de Quarkus, cuando se levanta la aplicación se agrega al comando de ejecucion la propiedad con el valor correcto.
- Como paginar a partir de lo que ofrece PanacheRepository (uso del metodo 'page' sobre metodo 'findAll')

## Al menos 1 decisión de diseño y porque
Se optó por utilizar PanacheRepository en lugar de PanacheEntity para mantener una mejor separación de responsabilidades entre capas, evitando acoplar las entidades del dominio a la lógica de persistencia.
Este enfoque es más alineado con una arquitectura limpia, ya que permite tratar la capa de datos como un detalle de implementación, facilitando el testeo y la evolución del sistema.
Adicionalmente, su uso resulta familiar por su similitud con el enfoque de repositorios en JPA/Spring, lo que reduce la fricción al trabajar con el patrón.
Es importante marcar tambien que como contrapartida implica mayor cantidad de código boilerplate en comparación con PanacheEntity.