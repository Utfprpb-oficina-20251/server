
# Análise das Classes DTO

Este documento lista todas as classes DTO existentes no projeto e detalha suas anotações OpenAPI (@Schema) atualmente presentes, tanto a nível de classe quanto de campo.

## Classe: CadastroUsuarioDTO  
_Arquivo:_ src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/CadastroUsuarioDTO.java

**Anotação de classe atual**  
@Schema(name = "CadastroUsuarioDTO", description = "Dados para cadastro de usuário")

- campo: username  
  - @Schema(description = "Nome de usuário", example = "usuario123")
- campo: password  
  - @Schema(description = "Senha do usuário", example = "senhaSecreta")
- campo: email  
  - Sem anotação @Schema de campo

## Classe: LoginUsuarioDTO  
_Arquivo:_ src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/LoginUsuarioDTO.java

**Anotação de classe atual**  
@Schema(name = "LoginUsuarioDTO")

- campo: username  
  - Sem anotação @Schema de campo
- campo: password  
  - @Schema(description = "Senha do usuário", example = "senha")

## Classe: RespostaLoginDTO  
_Arquivo:_ src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/RespostaLoginDTO.java

**Anotação de classe atual**  
@Schema(name = "RespostaLoginDTO", description = "Resposta de login")

- campo: token  
  - @Schema(description = "Token JWT de acesso")
- campo: expiresIn  
  - Sem anotação @Schema de campo

## Classe: UsuarioCadastradoDTO  
_Arquivo:_ src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/UsuarioCadastradoDTO.java

**Anotação de classe atual**  
Sem anotação @Schema de classe

- campo: id  
  - @Schema(description = "ID do usuário cadastrado")
- campo: username  
  - Sem anotação @Schema de campo
- campo: email  
  - Sem anotação @Schema de campo

## Classe: ProjetoDTO  
_Arquivo:_ src/main/java/br/edu/utfpr/pb/ext/server/projeto/ProjetoDTO.java

**Anotação de classe atual**  
@Schema(name = "ProjetoDTO", description = "Dados do projeto")

- campo: id  
  - @Schema(description = "ID do projeto")
- campo: nome  
  - Sem anotação @Schema de campo
- campo: descricao  
  - Sem anotação @Schema de campo

## Classe: UsuarioServidorRequestDTO  
_Arquivo:_ src/main/java/br/edu/utfpr/pb/ext/server/Usuario/dto/UsuarioServidorRequestDTO.java

**Anotação de classe atual**  
@Schema(name = "UsuarioServidorRequestDTO", description = "Requisição de usuário servidor")

- campo: servidorId  
  - @Schema(description = "ID do servidor")
- campo: payload  
  - Sem anotação @Schema de campo

## Classe: UsuarioServidorResponseDTO  
_Arquivo:_ src/main/java/br/edu/utfpr/pb/ext/server/Usuario/dto/UsuarioServidorResponseDTO.java

**Anotação de classe atual**  
@Schema(name = "UsuarioServidorResponseDTO", description = "Resposta de usuário servidor")

- campo: servidorId  
  - Sem anotação @Schema de campo
- campo: status  
  - @Schema(description = "Status da operação")
- campo: detalhes  
  - Sem anotação @Schema de campo

## Classe: TestDTO  
_Arquivo:_ src/test/java/br/edu/utfpr/pb/ext/server/generics/TestDTO.java

**Anotação de classe atual**  
Sem anotação @Schema de classe

- campo: testField  
  - Sem anotação @Schema de campo
- campo: genericField  
  - @Schema(description = "Campo genérico de teste")

**Total de DTOs encontrados:** 8  
**DTOs sem anotação de classe:** 2  
**DTOs com campos sem anotação:** 8  
