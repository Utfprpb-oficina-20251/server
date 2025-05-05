package br.edu.utfpr.pb.ext.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  /**
   * Retorna uma mensagem indicando que este é um endpoint de acesso público.
   *
   * @return uma string informando que o endpoint é de acesso público
   */
  @GetMapping("/public")
  public String publicEndpoint() {
    return "Endpoint de acesso publico";
  }
}
