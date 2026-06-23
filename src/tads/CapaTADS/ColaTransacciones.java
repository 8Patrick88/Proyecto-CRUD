package tads.CapaTADS;

import tads.CapaEntidadesTADS.Transaccion;

public class ColaTransacciones {
    private NodoTransaccion frente;
    private NodoTransaccion fin;

    public ColaTransacciones() {
        this.frente = null;
        this.fin = null;
    }

    public void encolar(Transaccion t) {
        NodoTransaccion nuevo = new NodoTransaccion(t);
        if (estaVacia()) {
            frente = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
    }

    public Transaccion desencolar() {
        if (estaVacia()) return null;
        Transaccion dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        return dato;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public NodoTransaccion getFrente() {
        return frente;
    }
}