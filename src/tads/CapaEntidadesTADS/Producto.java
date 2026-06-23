package tads.CapaEntidadesTADS;

public class Producto {
    private String idProducto;
    private String nombre;
    private String categoria;
    private double precioCompra;
    private double precioVenta;
    private int cantidadStock;
    private int stockMinimo;
    private String idProveedorAsociado;

    public Producto(String idProducto, String nombre, int cantidadStock, int stockMinimo, String idProveedorAsociado) {
        this(idProducto, nombre, "", 0.0, 0.0, cantidadStock, stockMinimo, idProveedorAsociado);
    }

    public Producto(String idProducto, String nombre, String categoria, double precioCompra, double precioVenta,
                    int cantidadStock, int stockMinimo, String idProveedorAsociado) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.cantidadStock = cantidadStock;
        this.stockMinimo = stockMinimo;
        this.idProveedorAsociado = idProveedorAsociado;
    }

    public Producto(String idProducto, String nombre, String categoria, double precioCompra, double precioVenta,
                    int cantidadStock, int stockMinimo, String idProveedorAsociado, String estado) {
        this(idProducto, nombre, categoria, precioCompra, precioVenta, cantidadStock, stockMinimo, idProveedorAsociado);
    }

    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    public int getCantidadStock() { return cantidadStock; }
    public void setCantidadStock(int cantidadStock) { this.cantidadStock = cantidadStock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getIdProveedorAsociado() { return idProveedorAsociado; }
    public void setIdProveedorAsociado(String idProveedorAsociado) { this.idProveedorAsociado = idProveedorAsociado; }

    public String getEstado() { return util.StockUtil.calcularNivel(this).getEtiqueta(); }
    public void setEstado(String estado) { }

    public boolean isEstadoAlertado() { return cantidadStock <= stockMinimo; }
    public void setEstadoAlertado(boolean estadoAlertado) { }
}
