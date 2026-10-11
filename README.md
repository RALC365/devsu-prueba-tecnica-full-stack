# Banco

[![CI](https://github.com/RALC365/devsu-prueba-tecnica-full-stack/actions/workflows/ci.yml/badge.svg)](https://github.com/RALC365/devsu-prueba-tecnica-full-stack/actions/workflows/ci.yml)

Prueba técnica Full Stack (Java + Angular). API REST en Spring Boot para clientes, cuentas y movimientos, con estado de cuenta en JSON y PDF, y un frontend en Angular que la consume.

![Pantalla de clientes](docs/capturas/clientes.png)

## Pasos para levantar el proyecto

Requisito: Docker. La configuración viene en el `docker-compose.yml`, por lo que no se necesita un archivo `.env`.

1. Levantar la base de datos, la API y el frontend:

   ```bash
   docker compose up -d --build
   ```

2. Abrir la aplicación:

   | Servicio | URL |
   |---|---|
   | Aplicación | http://localhost:4200 |
   | API | http://localhost:8080/api |
   | Swagger | http://localhost:8080/swagger-ui.html |

3. Cargar los datos de los casos de uso ejecutando en orden la colección `postman/Banco-API.postman_collection.json` con el Runner de Postman, o desde la terminal:

   ```bash
   npx newman run postman/Banco-API.postman_collection.json
   ```

## Requerimientos

### Indicaciones generales

**Backend**

- ✅ Buenas prácticas y patrones: capas controller, service y repository; Repository con Spring Data JPA; DTOs con mappers; Strategy y Factory para los tipos de movimiento.
- ✅ Entidades con JPA (Hibernate).
- ✅ Manejo de excepciones con un `@RestControllerAdvice`: cada error se devuelve como Problem Details (RFC 9457), con un `codigo` y el detalle por campo.
- ✅ Pruebas unitarias de endpoints: `ClienteControllerTest`, `CuentaControllerTest`, `MovimientoControllerTest` y `ReporteControllerTest`. El backend tiene 132 pruebas en total.
- ✅ Despliegue en Docker.

**Frontend**

- ✅ Buenas prácticas (clean code, SOLID): componentes standalone, un store genérico para los CRUD, un interceptor para los errores y formularios tipados.
- ✅ Pruebas unitarias con Jest: 172 pruebas, 95 % de cobertura.
- ✅ Sin frameworks de estilos ni componentes prefabricados: todo el CSS es propio.

### Entidades

- ✅ Persona: nombre, género, edad, identificación, dirección y teléfono, con su PK.
- ✅ Cliente hereda de Persona (herencia JOINED): clienteId, contraseña (guardada con BCrypt) y estado, con su PK.
- ✅ Cuenta: número, tipo, saldo inicial y estado, con su PK. El número de cuenta es único.
- ✅ Movimiento: fecha, tipo, valor y saldo, con su PK.

### Funcionalidades del backend

- ✅ CRUD con GET, POST, PUT, PATCH y DELETE en `/api/clientes`, `/api/cuentas` y `/api/movimientos`.

  ![Swagger](docs/capturas/swagger.png)

- ✅ Los créditos se guardan con valor positivo y los débitos con valor negativo. Cada movimiento guarda el saldo disponible con el que queda la cuenta.
- ✅ Si el saldo no alcanza para un débito, la API responde "Saldo no disponible".

  ![Saldo no disponible](docs/capturas/saldo-no-disponible.png)

- ✅ Límite diario de retiro de $1000, configurable con `BANCO_LIMITE_DIARIO_RETIRO`. Al superarlo, la API responde "Cupo diario Excedido".

  ![Cupo diario Excedido](docs/capturas/cupo-diario-excedido.png)

- ✅ Estado de cuenta por cliente y rango de fechas, con las cuentas, sus saldos y el total de créditos y débitos, en JSON y en PDF (base64):
  - `GET /api/reportes?clienteId=2&fechaInicio=2026-10-01&fechaFin=2026-10-10`
  - `GET /api/reportes/pdf` con los mismos parámetros

  Cada movimiento del reporte incluye los campos del ejemplo del enunciado:

  ```json
  {
    "fecha": "2026-10-10T17:56:09",
    "cliente": "Marianela Montalvo",
    "numeroCuenta": "225487",
    "tipoCuenta": "CORRIENTE",
    "saldoInicial": 100,
    "estado": true,
    "tipoMovimiento": "CREDITO",
    "movimiento": 600,
    "saldoDisponible": 700
  }
  ```

### Casos de uso

- ✅ Los cinco casos del enunciado (usuarios, cuentas, la nueva cuenta corriente de Jose Lema, los movimientos y el listado por fechas) se crean con la colección de Postman. La prueba de integración `CasosDeUsoIntegrationTest` también los reproduce.

  ![Cuentas](docs/capturas/cuentas.png)

  ![Movimientos](docs/capturas/movimientos.png)

### Funcionalidades del frontend

- ✅ Layout del diseño propuesto: encabezado, menú lateral y una pantalla por entidad.
- ✅ CRUD de clientes, cuentas y movimientos: alta y edición en un panel lateral, eliminación con confirmación.
- ✅ Reporte visual con resumen, gráfico y tablas, y botón para descargar el PDF.

  ![Reporte](docs/capturas/reporte.png)

  ![PDF del estado de cuenta](docs/capturas/reporte-pdf.png)

- ✅ Búsqueda rápida en todas las tablas, por cualquier columna.

  ![Búsqueda rápida](docs/capturas/busqueda.png)

- ✅ Mensajes de validación en pantalla, tanto los del formulario como los que devuelve la API.

  ![Validaciones](docs/capturas/cliente-validaciones.png)

### Extras

- ✅ Lambdas y programación funcional: streams y Optional en el backend; signals y RxJS en el frontend.
- ✅ Exception handlers (`@RestControllerAdvice`).
- ✅ Validación de atributos a nivel de modelo con Bean Validation y validadores propios.
- ✅ Patrones de diseño: Strategy, Factory, Repository y MVC.

### Despliegue y entregables

- ✅ `BaseDatos.sql` con el esquema completo. PostgreSQL lo ejecuta al crear el contenedor.
- ✅ Despliegue con Docker Compose: base de datos, API y frontend.
- ✅ Colección de Postman con 52 peticiones y 105 verificaciones. El workflow de GitHub Actions la ejecuta contra Docker en cada push.
- ✅ Repositorio público con el código, `BaseDatos.sql` y la colección de Postman.

## Ejecución de pruebas

```bash
cd backend && ./mvnw verify   # 132 pruebas; falla si la cobertura baja del 85 %
cd frontend && npm test       # 172 pruebas con Jest
```

## Ejecución sin Docker

Backend con JDK 21 (usa la base de datos del contenedor en `localhost:5433`) y frontend con Node 24:

```bash
docker compose up -d db
cd backend && ./mvnw spring-boot:run
cd frontend && npm install && npm start
```
