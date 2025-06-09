package br.edu.utfpr.pb.ext.server.security;

import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.projeto.ProjetoRepository;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    private final ProjetoRepository projetoRepository;

    public SecurityService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    public boolean podeEditarProjeto(Long projetoId) {
        Usuario userDetails = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long usuarioLogadoId = userDetails.getId();

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado para verificação de segurança"));

        return  projeto.getEquipeExecutora().stream()
                .anyMatch(membro -> membro.getId().equals(usuarioLogadoId));
    }
}