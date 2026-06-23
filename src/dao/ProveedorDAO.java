package dao;

import tads.CapaEntidadesTADS.Proveedor;
import tads.CapaTADS.ListaProveedores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ProveedorDAO {
    private final String rutaArchivo;

    public ProveedorDAO(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public void cargarProveedoresDesdeCSV(ListaProveedores listaDestino) {
        asegurarArchivoConDatosIniciales();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            br.readLine();
            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(",", -1);
                if (datos.length >= 3) {
                    Proveedor proveedor = new Proveedor(
                            datos[0].trim(),
                            datos[1].trim(),
                            datos[2].trim()
                    );
                    listaDestino.insertar(proveedor);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer proveedores.csv: " + e.getMessage());
        }
    }

    public void guardarProveedorEnCSV(Proveedor proveedor) {
        asegurarArchivoConDatosIniciales();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo, true))) {
            bw.write(proveedor.getIdProveedor() + "," + proveedor.getNombre() + "," + proveedor.getTelefono());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al guardar proveedor: " + e.getMessage());
        }
    }

    public String generarSiguienteIdProveedor() {
        int maximo = 0;
        asegurarArchivoConDatosIniciales();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            br.readLine();
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",", -1);
                if (datos.length == 0) continue;

                String id = datos[0].trim();
                if (id.startsWith("PROV-")) {
                    try {
                        int numero = Integer.parseInt(id.substring(5));
                        if (numero > maximo) {
                            maximo = numero;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException ignored) {
        }

        return String.format("PROV-%03d", maximo + 1);
    }

    private void asegurarArchivoConDatosIniciales() {
        File archivo = new File(rutaArchivo);
        if (archivo.exists() && archivo.length() > 0) {
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write("idProveedor,nombre,telefono");
            bw.newLine();
            bw.write("PROV-101,Distribuidora Norte,0991111111");
            bw.newLine();
            bw.write("PROV-102,Comercial Andina,0992222222");
            bw.newLine();
            bw.write("PROV-103,Importadora Central,0993333333");
            bw.newLine();
            bw.write("PROV-104,Suministros Ecuador,0994444444");
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al preparar proveedores.csv: " + e.getMessage());
        }
    }
}
