package tads.CapaTADS;

import tads.CapaEntidadesTADS.Transaccion;

public class NodoTransaccion {
    public Transaccion dato;
    public NodoTransaccion siguiente;

    public NodoTransaccion(Transaccion dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}