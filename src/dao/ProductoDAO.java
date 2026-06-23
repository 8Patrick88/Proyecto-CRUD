package dao;

import tads.CapaEntidadesTADS.Producto;
import tads.CapaTADS.ListaProductos;
import tads.CapaTADS.NodoProducto;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Locale;

public class ProductoDAO {
    private final String rutaArchivo;

    public ProductoDAO(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }






    public void guardarProductoEnCSV(Producto producto) {
        String delimitador = ",";
        asegurarArchivoConCabecera();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo, true))) {

            String nuevaLinea = String.format(Locale.US, "%s%s%s%s%s%s%.2f%s%.2f%s%d%s%d%s%s",
                    producto.getIdProducto(), delimitador,
                    producto.getNombre(), delimitador,
                    producto.getCategoria(), delimitador,
                    producto.getPrecioCompra(), delimitador,
                    producto.getPrecioVenta(), delimitador,
                    producto.getCantidadStock(), delimitador,
                    producto.getStockMinimo(), delimitador,
                    producto.getIdProveedorAsociado()
            );

            bw.write(nuevaLinea);
            bw.newLine(); // Salto de línea para el siguiente producto
            System.out.println("Éxito: Producto guardado en el archivo CSV.");

        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo CSV: " + e.getMessage());
        }
    }



    public void cargarProductosDesdeCSV(ListaProductos listaDestino) {
        String linea;
        String delimitador = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {

            br.readLine();

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(delimitador, -1);

                if (datos.length >= 8) {
                    String idProducto = datos[0].trim();
                    String nombre = datos[1].trim();
                    String categoria = datos[2].trim();
                    double precioCompra = Double.parseDouble(datos[3].trim());
                    double precioVenta = Double.parseDouble(datos[4].trim());
                    int cantidadStock = Integer.parseInt(datos[5].trim());
                    int stockMinimo = Integer.parseInt(datos[6].trim());
                    String idProveedorAsociado = datos[7].trim();

                    Producto nuevoProducto = new Producto(idProducto, nombre, categoria, precioCompra, precioVenta, cantidadStock, stockMinimo, idProveedorAsociado);

                    listaDestino.insertar(nuevoProducto);
                }
            }
            System.out.println(" Éxito: Se cargaron los registros del CSV en el DataStore global.");
        } catch (IOException e) {
            System.err.println(" Error físico al leer el archivo CSV: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println(" Error de formato numérico en el archivo: " + e.getMessage());
        }
    }

    public void sobrescribirProductosEnCSV(ListaProductos productos) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            bw.write("idProducto,nombre,categoria,precioCompra,precioVenta,cantidadStock,stockMinimo,idProveedorAsociado");
            bw.newLine();

            NodoProducto aux = productos.getCabeza();
            while (aux != null) {
                Producto producto = aux.dato;
                String linea = String.format(Locale.US, "%s,%s,%s,%.2f,%.2f,%d,%d,%s",
                        producto.getIdProducto(),
                        producto.getNombre(),
                        producto.getCategoria(),
                        producto.getPrecioCompra(),
                        producto.getPrecioVenta(),
                        producto.getCantidadStock(),
                        producto.getStockMinimo(),
                        producto.getIdProveedorAsociado()
                );
                bw.write(linea);
                bw.newLine();
                aux = aux.siguiente;
            }
        } catch (IOException e) {
            System.err.println("Error al actualizar productos.csv: " + e.getMessage());
        }
    }

    public String generarSiguienteIdProducto() {
        int maximo = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            br.readLine();
            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(",", -1);
                if (datos.length == 0) continue;

                String id = datos[0].trim();
                if (id.startsWith("PROD-")) {
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

        return String.format("PROD-%04d", maximo + 1);
    }

    private void asegurarArchivoConCabecera() {
        File archivo = new File(rutaArchivo);
        if (archivo.exists() && archivo.length() > 0) {
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write("idProducto,nombre,categoria,precioCompra,precioVenta,cantidadStock,stockMinimo,idProveedorAsociado");
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al preparar el archivo CSV: " + e.getMessage());
        }
    }
}



