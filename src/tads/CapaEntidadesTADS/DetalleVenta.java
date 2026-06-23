package tads.CapaEntidadesTADS;

public class DetalleVenta {
    private String idVenta;
    private String idProducto;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetalleVenta(String idProducto, String nombreProducto, int cantidad, double precioUnitario) {
        this("", idProducto, nombreProducto, cantidad, precioUnitario);
    }

    public DetalleVenta(String idVenta, String idProducto, String nombreProducto, int cantidad, double precioUnitario) {
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    public String getIdVenta() { return idVenta; }
    public void setIdVenta(String idVenta) { this.idVenta = idVenta; }

    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public void aumentarCantidad(int cantidadAdicional) {
        this.cantidad += cantidadAdicional;
        recalcularSubtotal();
    }

    private void recalcularSubtotal() {
        this.subtotal = cantidad * precioUnitario;
    }
}
