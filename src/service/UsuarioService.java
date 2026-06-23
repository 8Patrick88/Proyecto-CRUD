package service;

import dao.UsuarioDAO;
import model.Usuario;

import java.util.List;

public class UsuarioService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listarTodos();
    }

    public boolean registrarUsuario(String nombre, String usuario, String contrasena, String rol, String estado) {
        if (nombre.isEmpty() || usuario.isEmpty() || contrasena.isEmpty() || rol == null || estado == null) {
            return false;
        }
        if (usuarioDAO.buscarPorUsuario(usuario) != null) {
            return false;
        }

        Usuario nuevo = new Usuario(
                usuarioDAO.generarSiguienteIdUsuario(),
                nombre,
                usuario,
                contrasena,
                rol,
                estado
        );
        usuarioDAO.guardar(nuevo);
        return true;
    }

    public boolean actualizarUsuario(Usuario usuarioActualizado) {
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        for (Usuario usuario : usuarios) {
            if (!usuario.getIdUsuario().equals(usuarioActualizado.getIdUsuario())
                    && usuario.getUsuario().equalsIgnoreCase(usuarioActualizado.getUsuario())) {
                return false;
            }
        }

        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getIdUsuario().equals(usuarioActualizado.getIdUsuario())) {
                usuarios.set(i, usuarioActualizado);
                usuarioDAO.guardarTodos(usuarios);
                return true;
            }
        }
        return false;
    }

    public boolean eliminarUsuario(String idUsuario) {
        return usuarioDAO.eliminar(idUsuario);
    }
}
