package dao;

import model.Usuario;

public class UsuarioDAO implements CrudDAO<Usuario> {

    public UsuarioDAO() {

    }

    @Override
    public void guardar(Usuario usuario) {

        System.out.println(" Usuario guardado temporalmente: " + usuario.getUsuario());
    }

    @Override
    public Usuario buscarPorId(String id) {

        return null;
    }

    @Override
    public boolean eliminar(String id) {

        return false;
    }
}