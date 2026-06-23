package dao;

import tads.CapaEntidadesTADS.DetalleVenta;
import tads.CapaEntidadesTADS.Venta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VentaDAO {
    private final String rutaVentas;
    private final String rutaDetalleVentas;

    public VentaDAO(String rutaVentas, String rutaDetalleVentas) {
        this.rutaVentas = rutaVentas;
        this.rutaDetalleVentas = rutaDetalleVentas;
    }

    public void guardarVenta(Venta venta, List<DetalleVenta> detalles) {
        asegurarArchivo(rutaVentas, "idVenta,fechaHora,subtotal,iva,total,metodoPago,idUsuario");
        asegurarArchivo(rutaDetalleVentas, "idVenta,idProducto,cantidad,precioUnitario,subtotal,nombreProducto");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaVentas, true))) {
            String linea = String.format(Locale.US, "%s,%s,%.2f,%.2f,%.2f,%s,%s",
                    venta.getIdVenta(),
                    venta.getFechaHora(),
                    venta.getSubtotal(),
                    venta.getIva(),
                    venta.getTotal(),
                    venta.getMetodoPago(),
                    venta.getIdUsuario()
            );
            bw.write(linea);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al guardar venta: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaDetalleVentas, true))) {
            for (DetalleVenta detalle : detalles) {
                detalle.setIdVenta(venta.getIdVenta());
                String linea = String.format(Locale.US, "%s,%s,%d,%.2f,%.2f,%s",
                        detalle.getIdVenta(),
                        detalle.getIdProducto(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal(),
                        escapar(detalle.getNombreProducto())
                );
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar detalle de venta: " + e.getMessage());
        }
    }

    public List<Venta> cargarVentas() {
        asegurarArchivo(rutaVentas, "idVenta,fechaHora,subtotal,iva,total,metodoPago,idUsuario");
        List<Venta> ventas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaVentas))) {
            br.readLine();
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] datos = linea.split(",", -1);
                if (datos.length < 6) {
                    continue;
                }
                String idUsuario = datos.length >= 7 ? datos[6].trim() : "Sistema";
                ventas.add(new Venta(
                        datos[0].trim(),
                        datos[1].trim(),
                        Double.parseDouble(datos[2].trim()),
                        Double.parseDouble(datos[3].trim()),
                        Double.parseDouble(datos[4].trim()),
                        datos[5].trim(),
                        idUsuario
                ));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar ventas: " + e.getMessage());
        }

        return ventas;
    }

    public List<DetalleVenta> cargarDetallesPorVenta(String idVenta) {
        asegurarArchivo(rutaDetalleVentas, "idVenta,idProducto,cantidad,precioUnitario,subtotal,nombreProducto");
        List<DetalleVenta> detalles = new ArrayList<>();
        Map<String, String> nombresPorProducto = cargarNombresProductos();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaDetalleVentas))) {
            br.readLine();
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] datos = linea.split(",", -1);
                if (datos.length < 5 || !datos[0].trim().equals(idVenta)) {
                    continue;
                }

                String idProducto = datos[1].trim();
                int cantidad = Integer.parseInt(datos[2].trim());
                double precioUnitario = Double.parseDouble(datos[3].trim());
                String nombre = datos.length >= 6 ? datos[5].trim() : nombresPorProducto.getOrDefault(idProducto, idProducto);

                DetalleVenta detalle = new DetalleVenta(idVenta, idProducto, nombre, cantidad, precioUnitario);
                detalles.add(detalle);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar detalle de venta: " + e.getMessage());
        }

        return detalles;
    }

    public Map<String, List<DetalleVenta>> cargarDetallesAgrupados() {
        asegurarArchivo(rutaDetalleVentas, "idVenta,idProducto,cantidad,precioUnitario,subtotal,nombreProducto");
        Map<String, List<DetalleVenta>> detallesPorVenta = new HashMap<>();
        Map<String, String> nombresPorProducto = cargarNombresProductos();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaDetalleVentas))) {
            br.readLine();
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] datos = linea.split(",", -1);
                if (datos.length < 5) {
                    continue;
                }

                String idVenta = datos[0].trim();
                String idProducto = datos[1].trim();
                int cantidad = Integer.parseInt(datos[2].trim());
                double precioUnitario = Double.parseDouble(datos[3].trim());
                String nombre = datos.length >= 6 ? datos[5].trim() : nombresPorProducto.getOrDefault(idProducto, idProducto);

                DetalleVenta detalle = new DetalleVenta(idVenta, idProducto, nombre, cantidad, precioUnitario);
                detallesPorVenta.computeIfAbsent(idVenta, clave -> new ArrayList<>()).add(detalle);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar detalles de venta: " + e.getMessage());
        }

        return detallesPorVenta;
    }

    private Map<String, String> cargarNombresProductos() {
        Map<String, String> nombres = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("productos.csv"))) {
            br.readLine();
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] datos = linea.split(",", -1);
                if (datos.length >= 2) {
                    nombres.put(datos[0].trim(), datos[1].trim());
                }
            }
        } catch (IOException ignored) {
        }
        return nombres;
    }

    public String generarSiguienteIdVenta() {
        asegurarArchivo(rutaVentas, "idVenta,fechaHora,subtotal,iva,total,metodoPago,idUsuario");
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

    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace(",", " ");
    }
}
