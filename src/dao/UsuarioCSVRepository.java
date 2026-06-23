package dao;

import model.Usuario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioCSVRepository {
    private final String rutaArchivo;

    public UsuarioCSVRepository(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public List<Usuario> listarTodos() {
        asegurarArchivoConCabecera();
        List<Usuario> usuarios = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            br.readLine();
            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(",", -1);
                if (datos.length >= 6) {
                    usuarios.add(new Usuario(
                            datos[0].trim(),
                            datos[1].trim(),
                            datos[2].trim(),
                            datos[3].trim(),
                            datos[4].trim(),
                            datos[5].trim()
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer usuarios.csv: " + e.getMessage());
        }

        return usuarios;
    }

    public Usuario buscarPorUsuario(String nombreUsuario) {
        for (Usuario usuario : listarTodos()) {
            if (usuario.getUsuario().equalsIgnoreCase(nombreUsuario)) {
                return usuario;
            }
        }
        return null;
    }

    public Usuario buscarPorId(String idUsuario) {
        for (Usuario usuario : listarTodos()) {
            if (usuario.getIdUsuario().equalsIgnoreCase(idUsuario)) {
                return usuario;
            }
        }
        return null;
    }

    public void guardarTodos(List<Usuario> usuarios) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            bw.write("idUsuario,nombre,usuario,contrasena,rol,estado");
            bw.newLine();

            for (Usuario usuario : usuarios) {
                bw.write(String.join(",",
                        usuario.getIdUsuario(),
                        usuario.getNombre(),
                        usuario.getUsuario(),
                        usuario.getContrasena(),
                        usuario.getRol(),
                        usuario.getEstado()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios.csv: " + e.getMessage());
        }
    }

    public void guardar(Usuario nuevoUsuario) {
        List<Usuario> usuarios = listarTodos();
        usuarios.add(nuevoUsuario);
        guardarTodos(usuarios);
    }

    public boolean eliminar(String idUsuario) {
        List<Usuario> usuarios = listarTodos();
        boolean eliminado = usuarios.removeIf(usuario -> usuario.getIdUsuario().equalsIgnoreCase(idUsuario));
        if (eliminado) {
            guardarTodos(usuarios);
        }
        return eliminado;
    }

    public String generarSiguienteIdUsuario() {
        int maximo = 0;
        for (Usuario usuario : listarTodos()) {
            String id = usuario.getIdUsuario();
            if (id != null && id.startsWith("U")) {
                try {
                    int numero = Integer.parseInt(id.substring(1));
                    if (numero > maximo) {
                        maximo = numero;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("U%03d", maximo + 1);
    }

    private void asegurarArchivoConCabecera() {
        File archivo = new File(rutaArchivo);
        if (archivo.exists() && archivo.length() > 0) {
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write("idUsuario,nombre,usuario,contrasena,rol,estado");
            bw.newLine();
            bw.write("U001,Administrador,admin,admin123,ADMIN,ACTIVO");
            bw.newLine();
            bw.write("U002,Carlos Perez,carlos,1234,EMPLEADO,ACTIVO");
            bw.newLine();
            bw.write("U003,Laura Diaz,laura,1234,EMPLEADO,ACTIVO");
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al preparar usuarios.csv: " + e.getMessage());
        }
    }
}
