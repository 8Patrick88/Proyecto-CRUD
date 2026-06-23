package controller;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import model.MovimientoInventario;
import service.MovimientoInventarioService;
import util.DebounceUtil;
import util.KeyboardUtil;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MovimientoInventarioController implements Initializable {
    @FXML private TextField txtBuscar;
    @FXML private DatePicker dpFecha;
    @FXML private TableView<MovimientoInventario> tablaMovimientos;
    @FXML private TableColumn<MovimientoInventario, String> colId;
    @FXML private TableColumn<MovimientoInventario, String> colUsuario;
    @FXML private TableColumn<MovimientoInventario, String> colFecha;
    @FXML private TableColumn<MovimientoInventario, String> colHora;
    @FXML private TableColumn<MovimientoInventario, String> colTipo;
    @FXML private TableColumn<MovimientoInventario, String> colProducto;
    @FXML private TableColumn<MovimientoInventario, String> colNombreProducto;
    @FXML private TableColumn<MovimientoInventario, Integer> colAnterior;
    @FXML private TableColumn<MovimientoInventario, Integer> colNueva;
    @FXML private TableColumn<MovimientoInventario, String> colObservacion;
    @FXML private VBox rootPane;

    private final MovimientoInventarioService movimientoService = new MovimientoInventarioService();
    private final ObservableList<MovimientoInventario> listaVisual = FXCollections.observableArrayList();
    private final PauseTransition debounceBusqueda = DebounceUtil.crear(this::buscarMovimientos, 200);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idMovimiento"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombreProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colAnterior.setCellValueFactory(new PropertyValueFactory<>("cantidadAnterior"));
        colNueva.setCellValueFactory(new PropertyValueFactory<>("cantidadNueva"));
        colObservacion.setCellValueFactory(new PropertyValueFactory<>("observacion"));

        tablaMovimientos.setFixedCellSize(32);
        txtBuscar.textProperty().addListener((obs, anterior, actual) -> debounceBusqueda.playFromStart());
        dpFecha.valueProperty().addListener((obs, anterior, actual) -> buscarMovimientos());

        KeyboardUtil.configurarAtajos(rootPane, this::buscarMovimientos, txtBuscar::clear, null);
        buscarMovimientos();
    }

    @FXML
    private void buscarMovimientos() {
        String criterio = txtBuscar.getText();
        String fecha = dpFecha.getValue() == null ? "" : dpFecha.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        listaVisual.setAll(movimientoService.buscar(criterio, fecha));
        tablaMovimientos.setItems(listaVisual);
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        dpFecha.setValue(null);
        buscarMovimientos();
    }
}
