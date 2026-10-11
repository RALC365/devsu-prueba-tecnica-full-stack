# BANCO - Frontend

Frontend en Angular 22 para gestionar clientes, cuentas, movimientos y reportes.

## Requisitos

- Node 24
- Backend corriendo en `http://localhost:8080`

## Comandos

```bash
npm install
npm start               # http://localhost:4200, con proxy de /api a localhost:8080
npm test                # pruebas
npm run test:coverage   # pruebas con cobertura
npm run lint
npm run build           # build de producción en dist/banco-frontend/browser
```

## Notas

- Las pruebas usan Jest (jest-preset-angular), no Karma.
- El build de producción tiene `inlineCritical: false` para que `index.html` no tenga scripts inline y funcione con la CSP de nginx (`script-src 'self'`).
