package br.edu.utfpr.pb.ext.server.security;

import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.projeto.ProjetoRepository;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    private final ProjetoRepository projetoRepository;

    public SecurityService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    public boolean podeEditarProjeto(Long projetoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario)) {
            throw new SecurityException("Usuário não autenticado ou inválido");
        }
        Usuario userDetails = (Usuario) authentication.getPrincipal();
        Long usuarioLogadoId = userDetails.getId();

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado para verificação de segurança"));

        return projeto.getEquipeExecutora().stream()
                .anyMatch(membro -> membro.getId().equals(usuarioLogadoId));
    }
}