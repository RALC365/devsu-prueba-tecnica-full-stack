# Banco

Prueba técnica Full Stack (Java + Angular). Es una API en Spring Boot para manejar clientes, cuentas y movimientos, con un reporte de estado de cuenta en JSON y en PDF, y un frontend en Angular que la consume.

## Tecnologías

- Backend: Java 21, Spring Boot 4.1, Spring Data JPA, PostgreSQL 17
- Frontend: Angular 22, pruebas con Jest
- Docker y Docker Compose

## Cómo levantarlo

```bash
docker compose up -d --build
```

No hace falta crear un `.env`: el `docker-compose.yml` ya trae valores por defecto (el `.env.example` es solo para cambiarlos).

- Frontend: http://localhost:4200
- API: http://localhost:8080/api
- Swagger: http://localhost:8080/swagger-ui.html

La base se crea con `BaseDatos.sql` la primera vez que arranca el contenedor de PostgreSQL (queda en el puerto 5433 del host). Para empezar de cero: `docker compose down -v`.

Los datos de los casos de uso del enunciado se cargan con la colección de Postman (`postman/`), ejecutándola en orden con el Runner o con newman:

```bash
npx newman run postman/Banco-API.postman_collection.json
```

### Sin Docker

Backend (JDK 21). Por defecto se conecta a `localhost:5433`, así que se puede usar solo el contenedor de la base:

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

## Endpoints

| Recurso | Ruta |
|---|---|
| Clientes | `/api/clientes` |
| Cuentas | `/api/cuentas` (opcional `?clienteId=`) |
| Movimientos | `/api/movimientos` (opcional `?numeroCuenta=`) |
| Reporte | `/api/reportes?clienteId=1&fechaInicio=2022-02-01&fechaFin=2022-02-28` |
| Reporte en PDF | `/api/reportes/pdf` con los mismos parámetros (el PDF viene en base64) |

Clientes, cuentas y movimientos aceptan GET, POST, PUT, PATCH y DELETE. Los errores se devuelven como Problem Details (RFC 9457) con un campo `codigo`, y en los de validación viene el detalle por campo en `errores`.

En los movimientos el valor se envía positivo y el tipo define si suma o resta:

```json
{ "numeroCuenta": "478758", "tipoMovimiento": "DEBITO", "valor": 575 }
```

## Reglas

- Los créditos se guardan con valor positivo y los débitos con valor negativo. Cada movimiento guarda el saldo con el que queda la cuenta.
- Si el saldo no alcanza para un débito se responde "Saldo no disponible".
- El límite de retiro diario es 1000 por cuenta (variable `BANCO_LIMITE_DIARIO_RETIRO`). Si se supera se responde "Cupo diario Excedido".
- Al registrar un movimiento se bloquea la fila de la cuenta (`SELECT ... FOR UPDATE`), así dos retiros simultáneos no pueden usar el mismo saldo.
- Si se edita o elimina un movimiento se recalculan los saldos siguientes; si alguno queda negativo el cambio no se aplica.
- No se puede eliminar un cliente con cuentas ni una cuenta con movimientos, y las cuentas o clientes inactivos no pueden hacer movimientos.

## Pruebas

```bash
cd backend && ./mvnw verify
cd frontend && npm test
```

En el backend hay pruebas unitarias, de controladores, de repositorio y de integración, y el build falla si la cobertura baja del 85 %. El workflow de GitHub Actions corre las pruebas de los dos proyectos, levanta todo con Docker Compose y ejecuta la colección de Postman contra esa instalación.

## Notas

- `Persona` y `Cliente` usan herencia JOINED: persona tiene su propia PK y cliente usa la misma como PK y FK.
- El esquema se crea con `BaseDatos.sql`; Hibernate solo lo valida al arrancar.
- Los tipos de movimiento se manejan con el patrón Strategy (`CreditoStrategy` y `DebitoStrategy`).
- Las contraseñas se guardan con BCrypt y no se devuelven en ninguna respuesta.
- En `k8s/` hay manifiestos básicos para Kubernetes.
