# Banco

Prueba técnica Full Stack (Java + Angular). Es una aplicación para administrar clientes, cuentas y movimientos de un banco, con un estado de cuenta por cliente y rango de fechas que se puede ver en pantalla o descargar en PDF.

- `backend/`: API REST en Spring Boot (Java 21) con PostgreSQL.
- `frontend/`: aplicación en Angular 22 que consume la API.
- `BaseDatos.sql`: script con el esquema y las tablas.
- `postman/Banco-API.postman_collection.json`: colección para validar los endpoints.
- `docker-compose.yml`: levanta la base, la API y el frontend.

## Cómo levantarlo

Solo hace falta tener Docker. Desde la raíz del proyecto:

```bash
docker compose up -d --build
```

No hay que crear ningún `.env`, el `docker-compose.yml` ya trae los valores por defecto. La primera vez tarda unos minutos porque compila los dos proyectos. Cuando termine:

| Qué | Dónde |
|---|---|
| Aplicación | http://localhost:4200 |
| API | http://localhost:8080/api |
| Swagger | http://localhost:8080/swagger-ui.html |

La base de datos se crea sola con `BaseDatos.sql` cuando arranca PostgreSQL por primera vez (queda expuesta en el puerto 5433). Para borrar todo y empezar de cero: `docker compose down -v`.

## Probar con Postman

1. Importar `postman/Banco-API.postman_collection.json` en Postman.
2. Ejecutar la colección completa, en orden, con el Runner.

La colección crea los datos de los casos de uso del enunciado (José Lema, Marianela Montalvo, Juan Osorio, sus cuentas y movimientos) y verifica cada respuesta, incluidos los errores de "Saldo no disponible" y "Cupo diario Excedido". Usa la variable `baseUrl`, que apunta a `http://localhost:8080/api`.

También se puede correr desde la terminal:

```bash
npx newman run postman/Banco-API.postman_collection.json
```

## Qué resuelve del enunciado

**Backend**

- CRUD de clientes, cuentas y movimientos en `/api/clientes`, `/api/cuentas` y `/api/movimientos` (GET, POST, PUT, PATCH y DELETE).
- `Persona` y `Cliente` con herencia: cliente hereda de persona y tiene su propia clave.
- Los créditos se guardan en positivo y los débitos en negativo; cada movimiento queda con el saldo de la cuenta después de aplicarse.
- Si el saldo no alcanza para un débito se responde **"Saldo no disponible"**.
- El límite de retiro diario es de 1000 por cuenta (configurable con `BANCO_LIMITE_DIARIO_RETIRO`). Al superarlo se responde **"Cupo diario Excedido"**.
- Estado de cuenta por cliente y rango de fechas en `/api/reportes?clienteId=1&fechaInicio=2022-02-01&fechaFin=2022-02-28`, en JSON. Con los mismos parámetros, `/api/reportes/pdf` devuelve el PDF en base64.
- Pruebas unitarias (servicios, validaciones y controladores, empezando por los de clientes), de repositorio y de integración.

**Frontend**

- El layout del mockup: encabezado con la marca, menú lateral y una pantalla por entidad.
- CRUD de clientes, cuentas y movimientos con validaciones en pantalla, tanto las del formulario como las que devuelve la API.
- Búsqueda rápida en todas las tablas.
- Reporte con gráfico, resumen, tablas y botón para descargar el PDF.
- Estilos propios, sin frameworks de estilos ni componentes prefabricados. Pruebas con Jest.

## Detalles de la API

Los movimientos se envían con el valor en positivo y el tipo define si suma o resta:

```json
{ "numeroCuenta": "478758", "tipoMovimiento": "DEBITO", "valor": 575 }
```

Los errores siguen el formato Problem Details (RFC 9457) e incluyen un `codigo`. En los de validación viene el detalle por campo en `errores`.

Otras reglas:

- Al registrar un movimiento se bloquea la fila de la cuenta, así dos retiros al mismo tiempo no pueden usar el mismo saldo.
- Si se edita o elimina un movimiento se recalculan los saldos siguientes; si alguno queda negativo, el cambio no se aplica.
- No se puede eliminar un cliente con cuentas ni una cuenta con movimientos. Los clientes o cuentas inactivos no pueden registrar movimientos.
- Las contraseñas se guardan con BCrypt y nunca se devuelven.

## Sin Docker

Backend (JDK 21). Por defecto se conecta a `localhost:5433`, así que se puede levantar solo la base con Docker:

```bash
docker compose up -d db
cd backend
./mvnw spring-boot:run
```

Frontend (Node 24). El servidor de desarrollo redirige `/api` al backend en el puerto 8080:

```bash
cd frontend
npm install
npm start
```

## Pruebas

```bash
cd backend && ./mvnw verify
cd frontend && npm test
```

El build del backend falla si la cobertura baja del 85 %. El workflow de GitHub Actions corre las pruebas de los dos proyectos, levanta todo con Docker Compose y ejecuta la colección de Postman contra esa instalación. En `k8s/` hay manifiestos básicos para Kubernetes.
