package service;

import dao.ProductoDAO;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaEntidadesTADS.Proveedor;
import datastore.DataStore;

public class ProductoService {
    // Le pasamos la ruta del CSV que está en la raíz
    private final ProductoDAO productoDAO = new ProductoDAO("productos.csv");

    public boolean registrarNuevoProducto(Producto nuevo) {
        cargarProductosSiEsNecesario();

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

        return true;
    }

    public String generarSiguienteIdProducto() {
        return productoDAO.generarSiguienteIdProducto();
    }

    public void cargarProductosSiEsNecesario() {
        if (DataStore.productos.getCabeza() == null) {
            productoDAO.cargarProductosDesdeCSV(DataStore.productos);
        }
    }
}
