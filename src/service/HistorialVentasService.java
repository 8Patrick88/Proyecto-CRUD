package service;

import dao.VentaDAO;
import tads.CapaEntidadesTADS.DetalleVenta;
import tads.CapaEntidadesTADS.Venta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HistorialVentasService {
    private final VentaDAO ventaDAO = new VentaDAO("ventas.csv", "detalle_ventas.csv");
    private List<Venta> cacheVentas;
    private Map<String, List<DetalleVenta>> cacheDetalles;

    public List<Venta> listarVentas() {
        if (cacheVentas == null) {
            cacheVentas = ventaDAO.cargarVentas().stream()
                    .sorted(Comparator.comparing(Venta::getIdVenta).reversed())
                    .collect(Collectors.toList());
        }
        return cacheVentas;
    }

    public List<Venta> buscarVentas(String criterio, String fecha) {
        String texto = criterio == null ? "" : criterio.trim().toLowerCase();
        String fechaFiltro = fecha == null ? "" : fecha.trim();

        return listarVentas().stream()
                .filter(v -> texto.isEmpty()
                        || v.getIdVenta().toLowerCase().contains(texto)
                        || v.getIdUsuario().toLowerCase().contains(texto)
                        || v.getMetodoPago().toLowerCase().contains(texto))
                .filter(v -> fechaFiltro.isEmpty() || v.getFecha().equals(fechaFiltro))
                .collect(Collectors.toList());
    }

    public List<DetalleVenta> obtenerDetalleVenta(String idVenta) {
        if (cacheDetalles == null) {
            cacheDetalles = ventaDAO.cargarDetallesAgrupados();
        }
        return new ArrayList<>(cacheDetalles.getOrDefault(idVenta, List.of()));
    }

    public void invalidarCache() {
        cacheVentas = null;
        cacheDetalles = null;
    }
}
