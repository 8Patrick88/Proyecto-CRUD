package service;

import dao.ProveedorDAO;
import datastore.DataStore;
import tads.CapaEntidadesTADS.Proveedor;

public class ProveedorService {
    private final ProveedorDAO proveedorDAO = new ProveedorDAO("proveedores.csv");

    public void cargarProveedoresSiEsNecesario() {
        if (DataStore.proveedores.getCabeza() == null) {
            proveedorDAO.cargarProveedoresDesdeCSV(DataStore.proveedores);
        }
    }

    public boolean registrarProveedor(Proveedor proveedor) {
        if (proveedor.getNombre().trim().isEmpty()) {
            return false;
        }
        if (DataStore.proveedores.buscarPorId(proveedor.getIdProveedor()) != null) {
            return false;
        }
        if (DataStore.proveedores.buscarPorNombre(proveedor.getNombre()) != null) {
            return false;
        }

        DataStore.proveedores.insertar(proveedor);
        proveedorDAO.guardarProveedorEnCSV(proveedor);
        return true;
    }

    public String generarSiguienteIdProveedor() {
        return proveedorDAO.generarSiguienteIdProveedor();
    }
}
