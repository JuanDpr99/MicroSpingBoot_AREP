
<h1 align="center">Taller 3 — Servidor Web + Reflexión en Java (AREP)</h1>

Implementación de un servidor HTTP simple (tipo “mini Apache”) que sirve contenidos estáticos y un micro-framework por reflexión inspirado en Spring. Se exponen endpoints GET usando anotaciones personalizadas en POJOs.

## Características

- **Servidor HTTP** en puerto `35000`.
- **Ruteo por reflexión** con:
  - `@RestController` (clase)
  - `@GetMapping("/ruta")` (método)
  - `@RequestParam("name", defaultValue="World")` (parámetro)
- **Parámetros de consulta**: inyección automática de `String` desde la query.
- **Archivos estáticos**: sirve `html/png/css/js` desde `src/main/resources/webroot`.
- **Cargas de controladores**:
  - Versión 1: pasando FQCN (nombre de clase totalmente calificado) por CLI.
  - Versión 2: **auto-scan** de un paquete de ejemplos.

## Estructura de proyecto (resumen)
```bash
src/
└─ main/
├─ java/
│ └─ com/mycompany/
│ ├─ httpserver/
│ │ ├─ HttpServer.java
│ │ ├─ HttpRequest.java
│ │ └─ HttpResponse.java
│ └─ microspingboot/
│   ├─ MicroSpingBoot.java
│   ├─ annotations/
│   |   ├─ RestController.java
│   |   ├─ GetMapping.java
│   |   └─ RequestParam.java
│   └─ examples/
│       └─ GreetingController.java
└─ webroot/
    ├─ css/
    |    └─ styles.css
    ├─ img/logo.png (opcional)
    |    └─ Foto.JPEG
    |    └─ logo.png  
    ├─ index.html
    └─ myApp.js 
```
**Clonar y compilar**
```bash
git clone https://github.com/<tu-usuario>/<tu-repo>.git
cd <tu-repo>
find src/main/java -name "*.java" | xargs javac -d target/classes
```
