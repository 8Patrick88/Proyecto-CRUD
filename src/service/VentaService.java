package service;

import dao.ProductoDAO;
import dao.VentaDAO;
import datastore.DataStore;
import tads.CapaEntidadesTADS.DetalleVenta;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaEntidadesTADS.Venta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentaService {
    public static final double IVA_PORCENTAJE = 0.15;

    private final CatalogoService catalogoService = new CatalogoService();
    private final ProductoDAO productoDAO = new ProductoDAO("productos.csv");
    private final VentaDAO ventaDAO = new VentaDAO("ventas.csv", "detalle_ventas.csv");
    private final MovimientoInventarioService movimientoService = new MovimientoInventarioService();
    private final HistorialVentasService historialVentasService = new HistorialVentasService();

    public void cargarProductosSiEsNecesario() {
        catalogoService.cargarProductos();
    }

    public Producto buscarProducto(String criterio) {
        return catalogoService.buscarProducto(criterio);
    }

    public List<Producto> buscarProductos(String criterio) {
        return catalogoService.buscarProductos(criterio, CatalogoService.LIMITE_SUGERENCIAS_POS);
    }

    public Venta finalizarVenta(List<DetalleVenta> detalles, String metodoPago) {
        String idVenta = ventaDAO.generarSiguienteIdVenta();
        double[] totales = calcularTotales(detalles);
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String usuario = SesionUsuario.getUsuarioActual() != null
                ? SesionUsuario.getUsuarioActual().getNombre()
                : "Sistema";

        for (DetalleVenta detalle : detalles) {
            Producto producto = DataStore.productos.buscarPorId(detalle.getIdProducto());
            if (producto != null) {
                int stockAnterior = producto.getCantidadStock();
                producto.setCantidadStock(stockAnterior - detalle.getCantidad());
                movimientoService.registrar(
                        MovimientoInventarioService.REDUCCION_STOCK,
                        producto.getIdProducto(),
                        producto.getNombre(),
                        stockAnterior,
                        producto.getCantidadStock(),
                        "Venta " + idVenta
                );
            }
        }

        Venta venta = new Venta(idVenta, fechaHora, totales[0], totales[1], totales[2], metodoPago, usuario);
        ventaDAO.guardarVenta(venta, detalles);
        productoDAO.sobrescribirProductosEnCSV(DataStore.productos);
        historialVentasService.invalidarCache();

        return venta;
    }

    public double calcularSubtotal(List<DetalleVenta> detalles) {
        return calcularTotales(detalles)[0];
    }

    public double calcularIva(List<DetalleVenta> detalles) {
        return calcularTotales(detalles)[1];
    }

    public double calcularTotal(List<DetalleVenta> detalles) {
        return calcularTotales(detalles)[2];
    }

    public double[] calcularTotales(List<DetalleVenta> detalles) {
        double subtotal = 0.0;
        for (DetalleVenta detalle : detalles) {
            subtotal += detalle.getSubtotal();
        }
        subtotal = redondear(subtotal);
        double iva = redondear(subtotal * IVA_PORCENTAJE);
        double total = redondear(subtotal + iva);
        return new double[]{subtotal, iva, total};
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
