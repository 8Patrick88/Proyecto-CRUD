package tads.CapaTADS;

import tads.CapaEntidadesTADS.Proveedor;

public class NodoProveedor {
    public Proveedor dato;
    public NodoProveedor siguiente;

    public NodoProveedor(Proveedor dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}