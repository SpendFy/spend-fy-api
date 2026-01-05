# SpendFy API

API para gestão financeira pessoal desenvolvida como projeto acadêmico na UFAPE.

## Integrantes
- Felipe Mendes
- Guilherme Felix
- Lucas Tchaikovsky
- Pedro Medeiros

## Sobre o Projeto
O SpendFy API fornece recursos para controlar e gerenciar finanças pessoais, permitindo o registro de receitas, despesas, categorias financeiras, consulta de relatórios e demais operações para apoiar o planejamento financeiro.

## Tecnologias Utilizadas
- **Java**: Linguagem principal para desenvolvimento da API.
- **Spring Boot**: Framework para construção de aplicações Java e APIs RESTful.
- **Docker**: Containerização e implantação da aplicação.
- **PostgreSQL**: Banco de dados relacional para ambiente produtivo.
- **H2**: Banco em memória para desenvolvimento/testes (caso configurado).

## Como executar o projeto

### Pré-requisitos
- Java 21
- Maven 3.9+
- Docker e Docker Compose (opcional, para subir banco/serviço via contêiner)
- PostgreSQL 14+ (se rodar localmente sem Docker)

### Variáveis de ambiente (exemplo)
Configure as variáveis antes de rodar (ou utilize um arquivo `.env`):
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/spendfy
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_JPA_HIBERNATE_DDL_AUTO=update   # ou validate, conforme sua configuração
SPRING_PROFILES_ACTIVE=prod            # ou dev
SERVER_PORT=8080
```

### Execução com Maven (sem Docker)
```bash
# Instalar dependências e compilar
mvn clean install

# Iniciar a aplicação
mvn spring-boot:run
# A API ficará disponível em http://localhost:${SERVER_PORT:-8080}
```

### Execução com Docker (usando o Dockerfile do projeto)
```bash
# Build da imagem
docker build -t spendfy-api .

# Subir a API apontando para um PostgreSQL acessível
docker run -d --name spendfy-api \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/spendfy \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e SPRING_PROFILES_ACTIVE=prod \
  spendfy-api
```

### Execução com Docker Compose (API + PostgreSQL)
Caso utilize Docker Compose, crie um `docker-compose.yml` (se não existir) e execute:
```bash
docker compose up -d --build
```
Garanta que as variáveis de ambiente estejam alinhadas com as indicadas na seção anterior.

### Testes
```bash
mvn test
```

### Endpoints básicos (referência)
- Base URL: `http://localhost:8080`
- Saúde: `GET /actuator/health` (se o actuator estiver habilitado)
- Demais rotas: consulte os controllers expostos pela API.

Observação: ajuste os valores de ambiente conforme suas credenciais e host. Se utilizar o perfil `dev`, aponte para o banco de desenvolvimento ou configure um H2 caso o projeto ofereça suporte.

## Licença
Este projeto é destinado para fins acadêmicos na UFAPE.

Sinta-se à vontade para contribuir ou relatar problemas na seção de [Issues](https://github.com/SpendFy/spend-fy-api/issues).
