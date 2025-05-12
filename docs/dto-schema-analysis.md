
# Análise de Anotações @Schema em Classes DTO

## Listagem de DTOs
- src/main/java/br/edu/utfpr/pb/ext/server/Usuario/dto/UsuarioServidorRequestDTO.java  
- src/main/java/br/edu/utfpr/pb/ext/server/Usuario/dto/UsuarioServidorResponseDTO.java  
- src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/CadastroUsuarioDTO.java  
- src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/LoginUsuarioDTO.java  
- src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/RespostaLoginDTO.java  
- src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/UsuarioCadastradoDTO.java  
- src/main/java/br/edu/utfpr/pb/ext/server/projeto/ProjetoDTO.java  
- src/test/java/br/edu/utfpr/pb/ext/server/generics/TestDTO.java  

## Anotações @Schema Encontradas
- **CadastroUsuarioDTO.java**  
  - Classe: Sim  
  - Campos anotados: 3  
- **RespostaLoginDTO.java**  
  - Classe: Sim  
  - Campos anotados: 0  
- **ProjetoDTO.java**  
  - Classe: Sim  
  - Campos anotados: 0  
- **LoginUsuarioDTO.java**  
  - Classe: Não  
  - Campos anotados: 0  
- **UsuarioCadastradoDTO.java**  
  - Classe: Não  
  - Campos anotados: 0  
- **UsuarioServidorRequestDTO.java**  
  - Classe: Não  
  - Campos anotados: 0  
- **UsuarioServidorResponseDTO.java**  
  - Classe: Não  
  - Campos anotados: 0  
- **TestDTO.java**  
  - Classe: Não  
  - Campos anotados: 0  

## Subsecções por DTO

### CadastroUsuarioDTO.java
- Caminho: src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/CadastroUsuarioDTO.java  
- Total de campos: 3  
- Classe com @Schema: Sim  
- Campos com @Schema: 3  
- Tipos complexos detectados: nenhum  

### RespostaLoginDTO.java
- Caminho: src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/RespostaLoginDTO.java  
- Total de campos: 2  
- Classe com @Schema: Sim  
- Campos com @Schema: 0  
- Tipos complexos detectados: nenhum  

### ProjetoDTO.java
- Caminho: src/main/java/br/edu/utfpr/pb/ext/server/projeto/ProjetoDTO.java  
- Total de campos: 10  
- Classe com @Schema: Sim  
- Campos com @Schema: 0  
- Tipos complexos detectados: Date, List, enum  

### LoginUsuarioDTO.java
- Caminho: src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/LoginUsuarioDTO.java  
- Total de campos: 2  
- Classe com @Schema: Não  
- Campos com @Schema: 0  
- Tipos complexos detectados: nenhum  

### UsuarioCadastradoDTO.java
- Caminho: src/main/java/br/edu/utfpr/pb/ext/server/auth/dto/UsuarioCadastradoDTO.java  
- Total de campos: 2  
- Classe com @Schema: Não  
- Campos com @Schema: 0  
- Tipos complexos detectados: nenhum  

### UsuarioServidorRequestDTO.java
- Caminho: src/main/java/br/edu/utfpr/pb/ext/server/Usuario/dto/UsuarioServidorRequestDTO.java  
- Total de campos: 8  
- Classe com @Schema: Não  
- Campos com @Schema: 0  
- Tipos complexos detectados: nenhum  

### UsuarioServidorResponseDTO.java
- Caminho: src/main/java/br/edu/utfpr/pb/ext/server/Usuario/dto/UsuarioServidorResponseDTO.java  
- Total de campos: 6  
- Classe com @Schema: Não  
- Campos com @Schema: 0  
- Tipos complexos detectados: nenhum  

### TestDTO.java
- Caminho: src/test/java/br/edu/utfpr/pb/ext/server/generics/TestDTO.java  
- Total de campos: (verificar com grep -c 'private ')  
- Classe com @Schema: Não  
- Campos com @Schema: 0  
- Tipos complexos detectados: nenhum  

## Resumo Geral
- **Total de DTOs encontrados:** 8  
- **DTOs sem anotação @Schema de classe:** 5  
- **DTOs sem anotação @Schema de campo:** 7  
- **DTOs com anotações básicas:** 3  
- **Prioridade para melhorias:**  
  1. LoginUsuarioDTO.java  
  2. UsuarioCadastradoDTO.java  
  3. UsuarioServidorRequestDTO.java  
  4. UsuarioServidorResponseDTO.java  
