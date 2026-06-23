package service;

import model.Usuario;

public class SesionUsuario {
    private static Usuario usuarioActual;

    private SesionUsuario() {
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static boolean esAdmin() {
        return usuarioActual != null && usuarioActual.esAdmin();
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}
