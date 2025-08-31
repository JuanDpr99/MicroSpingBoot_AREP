
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
git clone https://github.com/JuanDpr99/MicroSpingBoot_AREP.git
cd MicroSpingBoot_AREP
mvn clean compile
```
## Uso - Ejemplos de Endpoints
```bash
- http://localhost:35000/app/greeting?name=Juan
- http://localhost:35000/img/logo.png
- http://localhost:35000/index.html
- java -cp target/classes com.mycompany.microspingboot.MicroSpingBoot com.mycompany.microspingboot.examples.GreetingController
- java -cp target/classes com.mycompany.microspingboot.MicroSpingBoot
```
# Pruebas
<p align="center">
<img width="530" height="131" alt="image" src="https://github.com/user-attachments/assets/ee3ab053-9d9d-4b58-b181-038ba3023580" />
<img width="728" height="225" alt="image" src="https://github.com/user-attachments/assets/7dc12181-8468-40ba-8402-33de851f540f" />
<img width="807" height="310" alt="image" src="https://github.com/user-attachments/assets/64756939-0668-407f-a51e-1268960beaa7" />
</p>



