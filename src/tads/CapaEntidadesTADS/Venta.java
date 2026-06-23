package tads.CapaEntidadesTADS;

public class Venta {
    private String idVenta;
    private String fechaHora;
    private double subtotal;
    private double iva;
    private double total;
    private String metodoPago;

    public Venta(String idVenta, String fechaHora, double subtotal, double iva, double total, String metodoPago) {
        this.idVenta = idVenta;
        this.fechaHora = fechaHora;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
        this.metodoPago = metodoPago;
    }

    public String getIdVenta() { return idVenta; }
    public void setIdVenta(String idVenta) { this.idVenta = idVenta; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}
