package service;

import dao.ProductoDAO;
import dao.VentaDAO;
import datastore.DataStore;
import tads.CapaEntidadesTADS.DetalleVenta;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaEntidadesTADS.Venta;
import tads.CapaTADS.NodoProducto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentaService {
    public static final double IVA_PORCENTAJE = 0.15;

    private final ProductoDAO productoDAO = new ProductoDAO("productos.csv");
    private final VentaDAO ventaDAO = new VentaDAO("ventas.csv", "detalle_ventas.csv");

    public void cargarProductosSiEsNecesario() {
        if (DataStore.productos.getCabeza() == null) {
            productoDAO.cargarProductosDesdeCSV(DataStore.productos);
        }
    }

    public Producto buscarProducto(String criterio) {
        cargarProductosSiEsNecesario();

        String texto = criterio.trim().toLowerCase();
        if (texto.isEmpty()) {
            return null;
        }

        NodoProducto aux = DataStore.productos.getCabeza();
        while (aux != null) {
            Producto producto = aux.dato;
            if (producto.getIdProducto().toLowerCase().equals(texto)
                    || producto.getNombre().toLowerCase().contains(texto)) {
                return producto;
            }
            aux = aux.siguiente;
        }
        return null;
    }

    public Venta finalizarVenta(List<DetalleVenta> detalles, String metodoPago) {
        String idVenta = ventaDAO.generarSiguienteIdVenta();
        double subtotal = calcularSubtotal(detalles);
        double iva = redondear(subtotal * IVA_PORCENTAJE);
        double total = redondear(subtotal + iva);
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        for (DetalleVenta detalle : detalles) {
            Producto producto = DataStore.productos.buscarPorId(detalle.getIdProducto());
            if (producto != null) {
                producto.setCantidadStock(producto.getCantidadStock() - detalle.getCantidad());
            }
        }

        Venta venta = new Venta(idVenta, fechaHora, redondear(subtotal), iva, total, metodoPago);
        ventaDAO.guardarVenta(venta, detalles);
        productoDAO.sobrescribirProductosEnCSV(DataStore.productos);

        return venta;
    }

    public double calcularSubtotal(List<DetalleVenta> detalles) {
        double subtotal = 0.0;
        for (DetalleVenta detalle : detalles) {
            subtotal += detalle.getSubtotal();
        }
        return redondear(subtotal);
    }

    public double calcularIva(List<DetalleVenta> detalles) {
        return redondear(calcularSubtotal(detalles) * IVA_PORCENTAJE);
    }

    public double calcularTotal(List<DetalleVenta> detalles) {
        return redondear(calcularSubtotal(detalles) + calcularIva(detalles));
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
