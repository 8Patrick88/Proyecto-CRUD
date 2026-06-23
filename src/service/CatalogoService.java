package service;

import dao.ProductoDAO;
import dao.ProveedorDAO;
import datastore.DataStore;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaTADS.NodoProducto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogoService {
    public static final int LIMITE_SUGERENCIAS_POS = 12;

    private final ProductoDAO productoDAO = new ProductoDAO("productos.csv");
    private final ProveedorDAO proveedorDAO = new ProveedorDAO("proveedores.csv");

    public void inicializarSistema() {
        cargarProductos();
        cargarProveedores();
    }

    public void cargarProductos() {
        if (DataStore.productos.getCabeza() == null) {
            productoDAO.cargarProductosDesdeCSV(DataStore.productos);
        }
    }

    public void cargarProveedores() {
        if (DataStore.proveedores.getCabeza() == null) {
            proveedorDAO.cargarProveedoresDesdeCSV(DataStore.proveedores);
        }
    }

    public List<Producto> listarProductos() {
        cargarProductos();
        List<Producto> productos = new ArrayList<>();
        NodoProducto aux = DataStore.productos.getCabeza();
        while (aux != null) {
            productos.add(aux.dato);
            aux = aux.siguiente;
        }
        return productos;
    }

    public Map<String, Producto> mapaProductos() {
        Map<String, Producto> mapa = new HashMap<>();
        for (Producto producto : listarProductos()) {
            mapa.put(producto.getIdProducto(), producto);
        }
        return mapa;
    }

    public Producto buscarPorId(String idProducto) {
        cargarProductos();
        return DataStore.productos.buscarPorId(idProducto);
    }

    public Producto buscarProducto(String criterio) {
        List<Producto> resultados = buscarProductos(criterio, 1);
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    public List<Producto> buscarProductos(String criterio) {
        return buscarProductos(criterio, Integer.MAX_VALUE);
    }

    public List<Producto> buscarProductos(String criterio, int limite) {
        cargarProductos();
        String texto = criterio == null ? "" : criterio.trim().toLowerCase();
        List<Producto> resultados = new ArrayList<>();
        if (texto.isEmpty() || limite <= 0) {
            return resultados;
        }

        NodoProducto aux = DataStore.productos.getCabeza();
        while (aux != null && resultados.size() < limite) {
            Producto producto = aux.dato;
            if (coincide(producto, texto)) {
                resultados.add(producto);
            }
            aux = aux.siguiente;
        }
        return resultados;
    }

    public List<Producto> filtrarProductos(String criterio) {
        cargarProductos();
        String texto = criterio == null ? "" : criterio.trim().toLowerCase();
        if (texto.isEmpty()) {
            return listarProductos();
        }

        List<Producto> resultados = new ArrayList<>();
        NodoProducto aux = DataStore.productos.getCabeza();
        while (aux != null) {
            Producto producto = aux.dato;
            if (coincide(producto, texto)
                    || producto.getCategoria().toLowerCase().contains(texto)) {
                resultados.add(producto);
            }
            aux = aux.siguiente;
        }
        return resultados;
    }

    private boolean coincide(Producto producto, String texto) {
        return producto.getIdProducto().toLowerCase().contains(texto)
                || producto.getNombre().toLowerCase().contains(texto);
    }
}
