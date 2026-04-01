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

# Semana 2
## Cosas aprendidas
- Al configurar Keycloak puede que el flow "direct grant" traiga problemas, ejemplo que exija el OTP por defecto, esto se puede resolver muchas veces generando un nuevo flow con los pasos que realmente necesitamos y haciendo el binding correspondiente con el realm
- Otro punto de conflicto pueden ser los datos del usuario, Keycloak en su versión 24 al menos parece tener activado por defecto el UserProfile (para hallar el mismo ir a 'Realm Settings' -> 'User Profile'), esto lleva a que a pesar que en la creación de usuario los campos no estén marcados como obligatorios visualmente, internamente el realm puede tener una política que los requiere para considerar el perfil "completo".
Esto último se suele evidenciar porque nos retorna un error <b>resolve_required_actions</b> que resulta engañoso, ya que no son "Required Actions" del usuario sino que Keycloak evalúa si el perfil está completo antes de emitir el token, y si falta algo dispara ese error.

- A la hora de hacer el Gateway la configuracion es muy importante, ya que es la que permite a partir de un archivo de configuracion agregar la data de los endpoints a los cuales redireccionar y ya, sin necesidad de reescribir cada endpoint
- En Spring, al utilizar el objeto RestTemplate con el enfoque de inyeccion, es neceseario colocarlo dentro de un @Bean para que sea manejado por Spring, sino la inyeccion fallaria y nos arrojaria que no hemos provisto una instancia del mismo

- Al trabajar con Spring Gateway existen 2 enfoques que varian segun la dependencia, una de ellas trabaja con el 'mvn' clasico, mientras que la otra es reactiva, ahora bien, esto nos afecta a la hora de generar el archivo de configuracion y los beans como es un por ejmplo SecurityFilterChain, en términos de la clase, la anotación va a variar entre las siguientes:
    -   @EnableWebSecurity, para MVC
    -   @EnableWebFluxSecurity, para reactivo
<br>En nuestro caso, aplica la segunda producto de la dependencia utilizada.
<br> Otro punto de diferencia es que MVC utiliza SecurityFilterChain, y el enfoque Reactivo utiliza SecurityWebFilterChain, se deja ejemplo simple donde además se evidencia variación en el primer elemento (authorizeHttpRequest vs authorizeExchange)

```
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(a -> a.anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
            .build();
}

@Bean
public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
            .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            .build();
}
```


- Método deprecado al generar pasar el dato a ''oauth2ResourceServer', es común que diferentes IAs generativas, así como ejemplos que podamos encontrar hagan uso de un bloque como el siguiente:

```
    o -> o.jwt(Customizer.withDefaults())
```
Este se encuentra deprecado, en consecuencia se recomienda ya no utilizarla, en su lugar deberíamos usar lo siguiente: 
```
    oauth2 -> oauth2.jwt(jwt -> {})
```

- Al configurar el SecurityWebFilterChain es necesario desactivar el CRSF para obtener una respuesta más acorde ante un caso donde enviemos una petición sin token, caso no lo hagamos obtendremos algo como "An expected CSRF token cannot be found".