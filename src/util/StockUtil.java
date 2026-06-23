package util;

import tads.CapaEntidadesTADS.Producto;

public final class StockUtil {

    public enum NivelStock {
        ALTO("Stock alto", "#d6eaf8", "#2471a3"),
        NORMAL("Stock normal", "#d5f5e3", "#1e8449"),
        BAJO("Stock bajo", "#fcf3cf", "#b7950b"),
        CRITICO("Stock crítico", "#fadbd8", "#c0392b");

        private final String etiqueta;
        private final String colorFondo;
        private final String colorTexto;

        NivelStock(String etiqueta, String colorFondo, String colorTexto) {
            this.etiqueta = etiqueta;
            this.colorFondo = colorFondo;
            this.colorTexto = colorTexto;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        public String getColorFondo() {
            return colorFondo;
        }

        public String getColorTexto() {
            return colorTexto;
        }
    }

    private StockUtil() {
    }

    public static NivelStock calcularNivel(int stock, int stockMinimo) {
        if (stockMinimo <= 0) {
            return stock > 0 ? NivelStock.ALTO : NivelStock.CRITICO;
        }

        double ratio = (double) stock / stockMinimo;
        if (ratio > 1.0) {
            return NivelStock.ALTO;
        }
        if (ratio >= 0.7) {
            return NivelStock.NORMAL;
        }
        if (ratio >= 0.3) {
            return NivelStock.BAJO;
        }
        return NivelStock.CRITICO;
    }

    public static NivelStock calcularNivel(Producto producto) {
        return calcularNivel(producto.getCantidadStock(), producto.getStockMinimo());
    }

    public static String estiloFila(Producto producto) {
        NivelStock nivel = calcularNivel(producto);
        return "-fx-background-color: " + nivel.getColorFondo() + ";";
    }

    public static String estiloEstado(Producto producto) {
        NivelStock nivel = calcularNivel(producto);
        return "-fx-text-fill: " + nivel.getColorTexto() + "; -fx-font-weight: bold;";
    }

    public static double calcularGananciaUnitaria(double precioVenta, double precioCompra) {
        return Math.round((precioVenta - precioCompra) * 100.0) / 100.0;
    }
}
