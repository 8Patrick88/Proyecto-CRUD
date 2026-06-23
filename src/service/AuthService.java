package service;

import dao.UsuarioCSVRepository;
import model.Usuario;

public class AuthService {
    private final UsuarioCSVRepository usuarioRepository = new UsuarioCSVRepository("usuarios.csv");

    public Usuario autenticar(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return null;
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            return null;
        }

        Usuario usuario = usuarioRepository.buscarPorUsuario(nombreUsuario.trim());
        if (usuario == null) {
            return null;
        }
        if (!usuario.estaActivo()) {
            return null;
        }
        if (!usuario.getContrasena().equals(contrasena.trim())) {
            return null;
        }

        return usuario;
    }
}
