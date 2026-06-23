package tads.CapaTADS;

import tads.CapaEntidadesTADS.Producto;

public class ListaProductos {
    private NodoProducto cabeza;

    public ListaProductos() {
        this.cabeza = null;
    }

    public void insertar(Producto p) {
        NodoProducto nuevo = new NodoProducto(p);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            NodoProducto aux = cabeza;
            while (aux.siguiente != null) {
                aux = aux.siguiente;
            }
            aux.siguiente = nuevo;
        }
    }

    public Producto buscarPorId(String id) {
        NodoProducto aux = cabeza;
        while (aux != null) {
            if (aux.dato.getIdProducto().equals(id)) {
                return aux.dato;
            }
            aux = aux.siguiente;
        }
        return null;
    }

    public Producto buscarPorNombre(String nombre) {
        NodoProducto aux = cabeza;
        while (aux != null) {
            if (aux.dato.getNombre().equalsIgnoreCase(nombre)) {
                return aux.dato;
            }
            aux = aux.siguiente;
        }
        return null;
    }

    public boolean eliminar(String id) {
        if (cabeza == null) return false;

        if (cabeza.dato.getIdProducto().equals(id)) {
            cabeza = cabeza.siguiente;
            return true;
        }

        NodoProducto aux = cabeza;
        while (aux.siguiente != null) {
            if (aux.siguiente.dato.getIdProducto().equals(id)) {
                aux.siguiente = aux.siguiente.siguiente;
                return true;
            }
            aux = aux.siguiente;
        }
        return false;
    }

    public NodoProducto getCabeza() {
        return cabeza;
    }
}


