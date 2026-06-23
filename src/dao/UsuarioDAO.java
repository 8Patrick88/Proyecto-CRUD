package dao;

import model.Usuario;

import java.util.List;

public class UsuarioDAO implements CrudDAO<Usuario> {
    private final UsuarioCSVRepository repository = new UsuarioCSVRepository("usuarios.csv");

    @Override
    public void guardar(Usuario usuario) {
        repository.guardar(usuario);
    }

    @Override
    public Usuario buscarPorId(String id) {
        return repository.buscarPorId(id);
    }

    public Usuario buscarPorUsuario(String usuario) {
        return repository.buscarPorUsuario(usuario);
    }

    public List<Usuario> listarTodos() {
        return repository.listarTodos();
    }

    public void guardarTodos(List<Usuario> usuarios) {
        repository.guardarTodos(usuarios);
    }

    public String generarSiguienteIdUsuario() {
        return repository.generarSiguienteIdUsuario();
    }

    @Override
    public boolean eliminar(String id) {
        return repository.eliminar(id);
    }
}
