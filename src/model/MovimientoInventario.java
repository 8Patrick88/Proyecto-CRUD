package model;

public class MovimientoInventario {
    private String idMovimiento;
    private String usuario;
    private String fecha;
    private String hora;
    private String tipo;
    private String idProducto;
    private String nombreProducto;
    private int cantidadAnterior;
    private int cantidadNueva;
    private String observacion;

    public MovimientoInventario() {
    }

    public MovimientoInventario(String idMovimiento, String usuario, String fecha, String hora, String tipo,
                                String idProducto, String nombreProducto, int cantidadAnterior,
                                int cantidadNueva, String observacion) {
        this.idMovimiento = idMovimiento;
        this.usuario = usuario;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidadAnterior = cantidadAnterior;
        this.cantidadNueva = cantidadNueva;
        this.observacion = observacion;
    }

    public String getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(String idMovimiento) { this.idMovimiento = idMovimiento; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidadAnterior() { return cantidadAnterior; }
    public void setCantidadAnterior(int cantidadAnterior) { this.cantidadAnterior = cantidadAnterior; }

    public int getCantidadNueva() { return cantidadNueva; }
    public void setCantidadNueva(int cantidadNueva) { this.cantidadNueva = cantidadNueva; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
