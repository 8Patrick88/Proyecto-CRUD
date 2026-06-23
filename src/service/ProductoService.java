package service;

import dao.ProductoDAO;
import datastore.DataStore;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaEntidadesTADS.Proveedor;

public class ProductoService {
    private final ProductoDAO productoDAO = new ProductoDAO("productos.csv");
    private final CatalogoService catalogoService = new CatalogoService();
    private final MovimientoInventarioService movimientoService = new MovimientoInventarioService();

    public boolean registrarNuevoProducto(Producto nuevo) {
        catalogoService.cargarProductos();
        catalogoService.cargarProveedores();

        if (nuevo.getIdProducto().isEmpty() || nuevo.getNombre().isEmpty()) {
            return false;
        }
        if (nuevo.getCategoria().isEmpty() || nuevo.getIdProveedorAsociado().isEmpty()) {
            return false;
        }
        if (nuevo.getPrecioCompra() <= 0 || nuevo.getPrecioVenta() <= 0) {
            return false;
        }
        if (nuevo.getPrecioVenta() < nuevo.getPrecioCompra()) {
            return false;
        }
        if (nuevo.getCantidadStock() < 0 || nuevo.getStockMinimo() < 0) {
            return false;
        }
        if (DataStore.productos.buscarPorId(nuevo.getIdProducto()) != null) {
            return false;
        }
        if (DataStore.productos.buscarPorNombre(nuevo.getNombre()) != null) {
            return false;
        }

        Proveedor proveedor = DataStore.proveedores.buscarPorId(nuevo.getIdProveedorAsociado());
        if (proveedor == null) {
            return false;
        }

        DataStore.productos.insertar(nuevo);
        productoDAO.guardarProductoEnCSV(nuevo);
        movimientoService.registrar(
                MovimientoInventarioService.ALTA_PRODUCTO,
                nuevo.getIdProducto(),
                nuevo.getNombre(),
                0,
                nuevo.getCantidadStock(),
                "Registro inicial de producto"
        );

        return true;
    }

    public String generarSiguienteIdProducto() {
        return productoDAO.generarSiguienteIdProducto();
    }

    public void cargarProductosSiEsNecesario() {
        catalogoService.cargarProductos();
    }
}
