package tads.PruebasTemporales;

import tads.CapaTADS.NodoProducto;
import dao.ProductoDAO;
import datastore.DataStore;

public class MainPruebaFuncionamiento {
    public static void main(String[] args) {
        System.out.println("====== AVANCE: INTEGRACIÓN GLOBAL (TAD + DAO + DATASTORE) ======");

        // 1. Instanciamos el DAO apuntando al archivo en la raíz del proyecto
        ProductoDAO productoDao = new ProductoDAO("productos.csv");

        // 2. Cargamos los datos directamente en el almacén global estático
        System.out.println("\n[DAO] Leyendo 'productos.csv' e inyectando en DataStore...");
        productoDao.cargarProductosDesdeCSV(DataStore.productos);

        // 3. Recorremos la estructura de la lista enlazada para verificar la carga exitosa
        System.out.println("\n[TAD] Recorriendo la Lista Enlazada global en memoria RAM:");
        System.out.println("-------------------------------------------------------------------------------------");

        NodoProducto aux = DataStore.productos.getCabeza();
        int contador = 0;

        while (aux != null) {
            contador++;
            System.out.println("ID: " + aux.dato.getIdProducto() +
                    " | Producto: " + aux.dato.getNombre() +
                    " | Stock: " + aux.dato.getCantidadStock() +
                    " | Estado Alerta: " + (aux.dato.isEstadoAlertado() ? "⚠️ CRÍTICO" : "OK"));
            aux = aux.siguiente;
        }

        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("📊 Total de nodos verificados en el almacén estático: " + contador);
    }
}