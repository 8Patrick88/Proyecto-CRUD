package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaTADS.NodoProducto;
import dao.ProductoDAO;
import datastore.DataStore;
import java.net.URL;
import java.util.ResourceBundle;

public class InventarioController implements Initializable {
    @FXML private TextField txtBuscarId;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Double> colPrecioVenta;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Integer> colMinimo;
    @FXML private TableColumn<Producto, String> colEstado;

    private final ObservableList<Producto> listaVisual = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidadStock"));
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        if (DataStore.productos.getCabeza() == null) {
            ProductoDAO dao = new ProductoDAO("productos.csv");
            dao.cargarProductosDesdeCSV(DataStore.productos);
        }
        actualizarTablaCompleta();
    }

    private void actualizarTablaCompleta() {
        listaVisual.clear();
        NodoProducto aux = DataStore.productos.getCabeza();
        while (aux != null) {
            listaVisual.add(aux.dato);
            aux = aux.siguiente;
        }
        tablaProductos.setItems(listaVisual);
    }

    @FXML private void accionBuscar() {
        String criterio = txtBuscarId.getText().trim().toLowerCase();
        if (criterio.isEmpty()) {
            actualizarTablaCompleta();
            return;
        }

        listaVisual.clear();
        NodoProducto aux = DataStore.productos.getCabeza();
        while (aux != null) {
            Producto producto = aux.dato;
            if (producto.getIdProducto().toLowerCase().contains(criterio)
                    || producto.getNombre().toLowerCase().contains(criterio)
                    || producto.getCategoria().toLowerCase().contains(criterio)) {
                listaVisual.add(producto);
            }
            aux = aux.siguiente;
        }
        tablaProductos.setItems(listaVisual);
    }

    @FXML private void accionRestaurar() {
        txtBuscarId.clear();
        actualizarTablaCompleta();
    }

    @FXML private void accionEliminar() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            if (DataStore.productos.eliminar(seleccionado.getIdProducto())) {
                actualizarTablaCompleta();
            }
        }
    }
}
