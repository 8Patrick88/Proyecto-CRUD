package datastore;

import tads.CapaEntidadesTADS.Producto;
import tads.CapaTADS.ListaProductos;
import tads.CapaTADS.ListaProveedores;
import tads.CapaTADS.ColaTransacciones;

public class DataStore {

    // Almacén estático global para los productos del inventario
    public static ListaProductos productos = new ListaProductos();

    // Almacén estático global para proveedores disponibles
    public static ListaProveedores proveedores = new ListaProveedores();

    // Almacén estático global para el historial de transacciones (Ventas/Compras)
    public static ColaTransacciones ventas = new ColaTransacciones();

    // Constructor privado para aplicar el patrón estático puro
    private DataStore() {
    }
}
