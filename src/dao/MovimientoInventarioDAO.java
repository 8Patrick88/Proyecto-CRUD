package dao;

import model.MovimientoInventario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovimientoInventarioDAO {
    private static final String RUTA = "movimientos_inventario.csv";
    private static final String CABECERA = "idMovimiento,usuario,fecha,hora,tipo,idProducto,nombreProducto,cantidadAnterior,cantidadNueva,observacion";

    public void guardar(MovimientoInventario movimiento) {
        asegurarArchivo();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, true))) {
            String linea = String.format(Locale.US, "%s,%s,%s,%s,%s,%s,%s,%d,%d,%s",
                    escapar(movimiento.getIdMovimiento()),
                    escapar(movimiento.getUsuario()),
                    escapar(movimiento.getFecha()),
                    escapar(movimiento.getHora()),
                    escapar(movimiento.getTipo()),
                    escapar(movimiento.getIdProducto()),
                    escapar(movimiento.getNombreProducto()),
                    movimiento.getCantidadAnterior(),
                    movimiento.getCantidadNueva(),
                    escapar(movimiento.getObservacion())
            );
            bw.write(linea);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al guardar movimiento de inventario: " + e.getMessage());
        }
    }

    public List<MovimientoInventario> listarTodos() {
        asegurarArchivo();
        List<MovimientoInventario> movimientos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA))) {
            br.readLine();
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] datos = linea.split(",", -1);
                if (datos.length < 10) {
                    continue;
                }
                movimientos.add(new MovimientoInventario(
                        datos[0].trim(),
                        datos[1].trim(),
                        datos[2].trim(),
                        datos[3].trim(),
                        datos[4].trim(),
                        datos[5].trim(),
                        datos[6].trim(),
                        Integer.parseInt(datos[7].trim()),
                        Integer.parseInt(datos[8].trim()),
                        datos[9].trim()
                ));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al leer movimientos de inventario: " + e.getMessage());
        }

        return movimientos;
    }

    public int obtenerMaximoNumero() {
        int maximo = 0;
        for (MovimientoInventario movimiento : listarTodos()) {
            String id = movimiento.getIdMovimiento();
            if (id.startsWith("MOV-")) {
                try {
                    int numero = Integer.parseInt(id.substring(4));
                    if (numero > maximo) {
                        maximo = numero;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return maximo;
    }

    public String generarSiguienteId() {
        return String.format("MOV-%04d", obtenerMaximoNumero() + 1);
    }

    private void asegurarArchivo() {
        File archivo = new File(RUTA);
        if (archivo.exists() && archivo.length() > 0) {
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write(CABECERA);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al preparar movimientos_inventario.csv: " + e.getMessage());
        }
    }

    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace(",", " ");
    }
}
