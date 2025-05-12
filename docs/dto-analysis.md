
# DTO Classes Analysis

This document summarizes all DTO classes and their current OpenAPI (`@Schema`) and validation annotations.

## List of DTO classes

- ./git/src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/CadastroUsuarioDTO.java
- ./git/src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/LoginUsuarioDTO.java
- ./git/src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/RespostaLoginDTO.java
- ./git/src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/UsuarioCadastradoDTO.java
- ./git/src/main/java/br/edu/utfpr/pb/ext/server/projeto/ProjetoDTO.java
- ./git/src/main/java/br/edu/utfpr/pb/ext/server/Usuario/dto/UsuarioServidorRequestDTO.java
- ./git/src/main/java/br/edu/utfpr/pb/ext/server/Usuario/dto/UsuarioServidorResponseDTO.java
- ./git/src/main/java/br/edu/utfpr/pb/ext/server/email/EmailCodeDto.java
- ./git/src/test/java/br/edu/utfpr/pb/ext/server/generics/TestDTO.java

## Class-level @Schema Annotations

- CadastroUsuarioDTO.java: has `@Schema(description = "Dados de cadastro de usuário")`
- LoginUsuarioDTO.java: no class-level `@Schema`
- RespostaLoginDTO.java: has `@Schema(name = "RespostaLoginDTO", description = "Resposta de login")`
- UsuarioCadastradoDTO.java: no class-level `@Schema`
- ProjetoDTO.java: no class-level `@Schema`
- UsuarioServidorRequestDTO.java: no class-level `@Schema`
- UsuarioServidorResponseDTO.java: no class-level `@Schema`
- EmailCodeDto.java: no class-level `@Schema`
- TestDTO.java: no class-level `@Schema`

## Field-level Annotations

- CadastroUsuarioDTO.java: fields have basic `@Schema(description, example)` and validations via `@Email`, `@NotBlank`.
- LoginUsuarioDTO.java: no field-level `@Schema`; fields have `@NotBlank`, `@Email`.
- RespostaLoginDTO.java: no field-level `@Schema`; no validation annotations.
- UsuarioCadastradoDTO.java: no field-level `@Schema`; no validation annotations.
- ProjetoDTO.java: no field-level `@Schema`; no validation annotations.
- UsuarioServidorRequestDTO.java: no field-level `@Schema`; no validation annotations.
- UsuarioServidorResponseDTO.java: no field-level `@Schema`; no validation annotations.
- EmailCodeDto.java: no field-level `@Schema`; no validation annotations.
- TestDTO.java: no field-level `@Schema`; no validation annotations.
