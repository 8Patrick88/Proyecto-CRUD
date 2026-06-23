package tads.CapaTADS;

import tads.CapaEntidadesTADS.Proveedor;

public class ListaProveedores {
    private NodoProveedor cabeza;

    public ListaProveedores() {
        this.cabeza = null;
    }

    public void insertar(Proveedor pr) {
        NodoProveedor nuevo = new NodoProveedor(pr);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            NodoProveedor aux = cabeza;
            while (aux.siguiente != null) {
                aux = aux.siguiente;
            }
            aux.siguiente = nuevo;
        }
    }

    public Proveedor buscarPorId(String id) {
        NodoProveedor aux = cabeza;
        while (aux != null) {
            if (aux.dato.getIdProveedor().equals(id)) {
                return aux.dato;
            }
            aux = aux.siguiente;
        }
        return null;
    }

    public Proveedor buscarPorNombre(String nombre) {
        NodoProveedor aux = cabeza;
        while (aux != null) {
            if (aux.dato.getNombre().equalsIgnoreCase(nombre)) {
                return aux.dato;
            }
            aux = aux.siguiente;
        }
        return null;
    }

    public NodoProveedor getCabeza() {
        return cabeza;
    }
}
