# SkyPilot Simulator

## Descrição
Sistema de simulador de carreira de piloto com backend Java/Spring Boot, frontend Angular e infraestrutura local pronta para uso com PostgreSQL e Docker.

## Tecnologias
- Java 21
- Spring Boot 3.3.3
- Maven
- Angular
- PostgreSQL
- Docker
- Ollama

## Estrutura
- backend
- frontend

## Como executar localmente
1. Docker completo:
   docker compose up --build
   ou
   cd backend
   mvn spring-boot:run

2. Frontend local:
   cd frontend
   npm install
   npx ng serve --host 0.0.0.0 --port 4200

3. Frontend via Docker:
   docker compose up frontend --build

## Configuração de produção
Crie um arquivo .env na raiz com as variáveis abaixo:

POSTGRES_DB=skypilot
POSTGRES_USER=skypilot
POSTGRES_PASSWORD=skypilot
JWT_SECRET=trocar-por-secret-forte
DB_URL=jdbc:postgresql://localhost:5432/skypilot
DB_USERNAME=skypilot
DB_PASSWORD=skypilot
SPRING_PROFILES_ACTIVE=prod
CORS_ALLOWED_ORIGINS=https://seu-frontend.vercel.app,https://*.vercel.app
API_BASE_URL=https://seu-backend-prod.exemplo.com

# Provê dados externos de rota/voo reais. Use valores do seu provedor aprovado.
AVIATION_DATA_PROVIDER=flightaware
AVIATION_DATA_BASE_URL=https://api.flightaware.com/1
AVIATION_DATA_API_KEY=sua-chave-api-real

O backend usa o perfil prod com PostgreSQL e o perfil test com H2 para validação automática.
Quando a configuração de provedor externo não estiver presente ou o acesso falhar, o sistema usa automaticamente um catálogo interno seguro de fallback.

### Deploy no Vercel
- Frontend: deploy do diretório frontend em um projeto Vercel.
- Backend: publicar a API Spring Boot em um host com PostgreSQL (ex.: Render, Railway, Azure App Service, VPS).
- Variáveis de ambiente do frontend:
  - API_BASE_URL=https://seu-backend-prod.exemplo.com
- Variáveis de ambiente do backend:
  - SPRING_PROFILES_ACTIVE=prod
  - DB_URL=jdbc:postgresql://<host>:5432/<database>
  - DB_USERNAME=...
  - DB_PASSWORD=...
  - JWT_SECRET=...
  - CORS_ALLOWED_ORIGINS=https://seu-frontend.vercel.app,https://*.vercel.app

> Importante: estas credenciais são sensíveis e não devem ser commitadas no repositório. Use um arquivo .env local ou um secret manager em produção.
