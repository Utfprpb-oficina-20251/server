Projeto responsável pela disponibilização de API para o projeto do curso de Oficina de Projeto - 2025/1 da UTFPR-PB

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=Utfprpb-oficina-20251_server)](https://sonarcloud.io/summary/new_code?id=Utfprpb-oficina-20251_server)

## Requisitos

- Java 21
- Maven 3
- Lombok

## Instalação
Execute o goal `mvn install` e um arquivo no formato fat jar será gerado na pasta `/target`. 

Basta executar o fat jar com as variáveis de ambiente para a execução do projeto.

## Variáveis de Ambiente
As seguintes variáveis de ambiente podem ser configuradas para a execução desse projeto:


`APP_ALLOWED_ORIGINS` 
Adiciona origens permitidas pelo servidor para configuração de CORS, opcional

`JWT_SECRET_KEY`
Secret para a criação do JWT, deve ser um HSHA-512 em formato base64, obrigatório

`JWT_EXPIRATION_TIME`
Tempo em millisegundos de validade do token JWT, opcional

`DATABASE_URL`
Url do banco de dados qual a aplicação vai se conectar, opcional para teste

`DATABASE_USERNAME` Nome de usuário do banco, padrão é postgres para fim de desenvolvimento

`DATABASE_PASSWORD` Senha do banco de dados, opcional para teste

`POSTGRES_DB` Nome do banco de dados utilizado ao criar o banco pelo docker-compose.yml



### Exemplo de envfile
```.text
//.env
APP_ALLOWED_ORIGINS=
JWT_SECRET_KEY=
JWT_EXPIRATION_TIME=
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
POSTGRES_DB=
```