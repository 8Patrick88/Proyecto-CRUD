package controller;

import datastore.DataStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import service.ProveedorService;
import tads.CapaEntidadesTADS.Proveedor;
import tads.CapaTADS.NodoProveedor;

import java.net.URL;
import java.util.ResourceBundle;

public class ProveedorController implements Initializable {
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, String> colId;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colTelefono;

    private final ProveedorService proveedorService = new ProveedorService();
    private final ObservableList<Proveedor> listaVisual = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        proveedorService.cargarProveedoresSiEsNecesario();
        actualizarTabla();
    }

    @FXML
    private void clickEnRegistrarProveedor() {
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "El nombre del proveedor es obligatorio.");
            return;
        }
        if (DataStore.proveedores.buscarPorNombre(nombre) != null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Duplicado", "Ya existe un proveedor con ese nombre.");
            return;
        }

        String id = proveedorService.generarSiguienteIdProveedor();
        Proveedor proveedor = new Proveedor(id, nombre, telefono);

        if (proveedorService.registrarProveedor(proveedor)) {
            txtNombre.clear();
            txtTelefono.clear();
            actualizarTabla();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso", "Proveedor registrado con ID " + id + ".");
        }
    }

    private void actualizarTabla() {
        listaVisual.clear();
        NodoProveedor aux = DataStore.proveedores.getCabeza();
        while (aux != null) {
            listaVisual.add(aux.dato);
            aux = aux.siguiente;
        }
        tablaProveedores.setItems(listaVisual);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
