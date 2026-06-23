package service;

import dao.UsuarioDAO;

public class UsuarioService {

    private UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    // Aquí puedes añadir tus métodos de negocio de usuarios más adelante
}