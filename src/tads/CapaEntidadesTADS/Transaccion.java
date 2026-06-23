package tads.CapaEntidadesTADS;

public class Transaccion {
    private String idTransaccion;
    private String tipo;
    private int cantidad;
    private String fecha;

    public Transaccion(String idTransaccion, String tipo, int cantidad, String fecha) {
        this.idTransaccion = idTransaccion;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    public String getIdTransaccion() { return idTransaccion; }
    public String getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public String getFecha() { return fecha; }
}






