package br.edu.utfpr.pb.ext.server.security;

import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.projeto.ProjetoRepository;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioServiceImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService") // O nome do bean que a SpEL procura
public class SecurityService {

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;

    // Injeção de dependências necessárias
    public SecurityService(ProjetoRepository projetoRepository, UsuarioRepository usuarioRepository) {
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public boolean podeEditarProjeto(Long projetoId) {
        Usuario userDetails = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long usuarioLogadoId = userDetails.getId();

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado para verificação de segurança"));

        boolean isDaEquipe = projeto.getEquipeExecutora().stream()
                .anyMatch(membro -> membro.getId().equals(usuarioLogadoId));

        return  isDaEquipe;
    }
}