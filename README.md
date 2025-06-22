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

`SWAGGER_ENABLED` Define se o Swagger deve exibir a documentação da API, opcional

`SENDGRID_API_KEY` Chave de API do SendGrid para envio de e-mails, opcional para desenvolvimento

`MINIO_URL` Url do Minio para envio de arquivos, opcional para desenvolvimento

`MINIO_ACCESS_KEY` Chave de acesso ao Minio para envio de arquivos, opcional para desenvolvimento

`MINIO_SECRET_KEY` Chave de acesso ao Minio para envio de arquivos, opcional para desenvolvimento

`MINIO_BUCKET` Nome do bucket do Minio para envio de arquivos, opcional para desenvolvimento

`MAX_FILE_SIZE` Define o tamanho máximo de um arquivo enviado ao servidor, opcional

`MAX_REQUEST_SIZE` Define o tamanho maximo de uma requisição ao servidor, opcional

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
SWAGGER_ENABLED=
SENDGRID_API_KEY=
MINIO_URL=
MINIO_ACCESS_KEY=
MINIO_SECRET_KEY=
MINIO_BUCKET=
MAX_FILE_SIZE=
MAX_REQUEST_SIZE=
```