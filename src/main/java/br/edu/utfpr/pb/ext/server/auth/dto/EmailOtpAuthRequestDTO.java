package br.edu.utfpr.pb.ext.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailOtpAuthRequestDTO {
    @NotBlank @Email
    private String email;
    
    @NotBlank
    private String code;
}