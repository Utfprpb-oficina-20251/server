package br.edu.utfpr.pb.ext.server.generics;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDTO {
    private Long id;
    
    @NotEmpty(message = "Name is required")
    private String name;
    
    private String description;
}
