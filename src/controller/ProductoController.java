package controller;

import datastore.DataStore;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import tads.CapaEntidadesTADS.Proveedor;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaTADS.NodoProveedor;
import service.ProductoService;
import service.ProveedorService;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductoController implements Initializable {
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStock;
    @FXML private TextField txtMinimo;
    @FXML private ComboBox<Proveedor> cmbProveedor;

    private final ProductoService productoService = new ProductoService();
    private final ProveedorService proveedorService = new ProveedorService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "Bebidas", "Lácteos", "Snacks", "Granos", "Limpieza",
                "Higiene", "Ferretería", "Papelería", "Mascotas", "Otros"
        ));

        proveedorService.cargarProveedoresSiEsNecesario();
        cargarComboProveedores();
    }

    @FXML
    public void clickEnRegistrarProducto() {
        try {
            String nombre = txtNombre.getText().trim();
            String categoria = cmbCategoria.getValue();
            Proveedor proveedor = cmbProveedor.getValue();
            double precioCompra = Double.parseDouble(txtPrecioCompra.getText().trim());
            double precioVenta = Double.parseDouble(txtPrecioVenta.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            int minimo = Integer.parseInt(txtMinimo.getText().trim());

            String error = validarFormulario(nombre, categoria, precioCompra, precioVenta, stock, minimo, proveedor);
            if (error != null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", error);
                return;
            }

            String id = productoService.generarSiguienteIdProducto();
            Producto nuevo = new Producto(id, nombre, categoria, precioCompra, precioVenta, stock, minimo, proveedor.getIdProveedor());
            boolean exito = productoService.registrarNuevoProducto(nuevo);

            if (exito) {
                limpiarFormulario();
                String mensaje = "Producto registrado con ID " + id + ".";
                if (nuevo.isEstadoAlertado()) {
                    mensaje += "\nAdvertencia: el producto se registró con BAJO STOCK.";
                }
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso", mensaje);
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "No se pudo registrar", "Revise que el producto no esté duplicado y que el proveedor exista.");
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos inválidos", "Los precios y stocks deben ser numéricos.");
        }
    }

    @FXML
    private void clickEnCancelarRegistro() {
        if (formularioVacio()) {
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar registro");
        confirmacion.setHeaderText("¿Desea cancelar el registro?");
        confirmacion.setContentText("Los datos ingresados se limpiarán.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            limpiarFormulario();
        }
    }

    private String validarFormulario(String nombre, String categoria, double precioCompra, double precioVenta,
                                     int stock, int minimo, Proveedor proveedor) {
        if (nombre.isEmpty()) {
            return "El nombre del producto es obligatorio.";
        }
        if (categoria == null || categoria.isEmpty()) {
            return "Debe seleccionar una categoría.";
        }
        if (precioCompra <= 0) {
            return "El precio de compra debe ser mayor que cero.";
        }
        if (precioVenta <= 0) {
            return "El precio de venta debe ser mayor que cero.";
        }
        if (precioVenta < precioCompra) {
            return "El precio de venta no puede ser menor que el precio de compra.";
        }
        if (stock < 0) {
            return "El stock inicial no puede ser negativo.";
        }
        if (minimo < 0) {
            return "El stock mínimo no puede ser negativo.";
        }
        if (proveedor == null) {
            return "Debe seleccionar un proveedor asociado.";
        }
        if (DataStore.productos.buscarPorNombre(nombre) != null) {
            return "Ya existe un producto registrado con ese nombre.";
        }
        return null;
    }

    private void cargarComboProveedores() {
        ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();
        NodoProveedor aux = DataStore.proveedores.getCabeza();
        while (aux != null) {
            proveedores.add(aux.dato);
            aux = aux.siguiente;
        }
        cmbProveedor.setItems(proveedores);
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        txtPrecioCompra.clear();
        txtPrecioVenta.clear();
        txtStock.clear();
        txtMinimo.clear();
        cmbProveedor.getSelectionModel().clearSelection();
    }

    private boolean formularioVacio() {
        return txtNombre.getText().trim().isEmpty()
                && cmbCategoria.getValue() == null
                && txtPrecioCompra.getText().trim().isEmpty()
                && txtPrecioVenta.getText().trim().isEmpty()
                && txtStock.getText().trim().isEmpty()
                && txtMinimo.getText().trim().isEmpty()
                && cmbProveedor.getValue() == null;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
