package service;

import tads.CapaEntidadesTADS.Producto;
import util.StockUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NotificacionService {
    private final CatalogoService catalogoService = new CatalogoService();

    public List<String> obtenerNotificaciones() {
        Set<String> notificaciones = new LinkedHashSet<>();
        for (Producto producto : catalogoService.listarProductos()) {
            StockUtil.NivelStock nivel = StockUtil.calcularNivel(producto);
            if (nivel == StockUtil.NivelStock.BAJO) {
                notificaciones.add("Stock bajo: " + producto.getNombre()
                        + " (" + producto.getCantidadStock() + " / mín " + producto.getStockMinimo() + ")");
            } else if (nivel == StockUtil.NivelStock.CRITICO) {
                notificaciones.add("Stock crítico: " + producto.getNombre()
                        + " (" + producto.getCantidadStock() + " / mín " + producto.getStockMinimo() + ")");
            }
        }
        return new ArrayList<>(notificaciones);
    }

    public int contarNotificaciones() {
        int contador = 0;
        for (Producto producto : catalogoService.listarProductos()) {
            StockUtil.NivelStock nivel = StockUtil.calcularNivel(producto);
            if (nivel == StockUtil.NivelStock.BAJO || nivel == StockUtil.NivelStock.CRITICO) {
                contador++;
            }
        }
        return contador;
    }
}
