package dao;

import tads.CapaEntidadesTADS.DetalleVenta;
import tads.CapaEntidadesTADS.Venta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class VentaDAO {
    private final String rutaVentas;
    private final String rutaDetalleVentas;

    public VentaDAO(String rutaVentas, String rutaDetalleVentas) {
        this.rutaVentas = rutaVentas;
        this.rutaDetalleVentas = rutaDetalleVentas;
    }

    public void guardarVenta(Venta venta, List<DetalleVenta> detalles) {
        asegurarArchivo(rutaVentas, "idVenta,fechaHora,subtotal,iva,total,metodoPago");
        asegurarArchivo(rutaDetalleVentas, "idVenta,idProducto,cantidad,precioUnitario,subtotal");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaVentas, true))) {
            String linea = String.format(Locale.US, "%s,%s,%.2f,%.2f,%.2f,%s",
                    venta.getIdVenta(),
                    venta.getFechaHora(),
                    venta.getSubtotal(),
                    venta.getIva(),
                    venta.getTotal(),
                    venta.getMetodoPago()
            );
            bw.write(linea);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al guardar venta: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaDetalleVentas, true))) {
            for (DetalleVenta detalle : detalles) {
                detalle.setIdVenta(venta.getIdVenta());
                String linea = String.format(Locale.US, "%s,%s,%d,%.2f,%.2f",
                        detalle.getIdVenta(),
                        detalle.getIdProducto(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                );
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar detalle de venta: " + e.getMessage());
        }
    }

    public String generarSiguienteIdVenta() {
        asegurarArchivo(rutaVentas, "idVenta,fechaHora,subtotal,iva,total,metodoPago");
        int maximo = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaVentas))) {
            br.readLine();
            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(",", -1);
                if (datos.length == 0) continue;

                String id = datos[0].trim();
                if (id.startsWith("V")) {
                    try {
                        int numero = Integer.parseInt(id.substring(1));
                        if (numero > maximo) {
                            maximo = numero;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException ignored) {
        }

        return String.format("V%03d", maximo + 1);
    }

    private void asegurarArchivo(String ruta, String cabecera) {
        File archivo = new File(ruta);
        if (archivo.exists() && archivo.length() > 0) {
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write(cabecera);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al preparar archivo " + ruta + ": " + e.getMessage());
        }
    }
}
