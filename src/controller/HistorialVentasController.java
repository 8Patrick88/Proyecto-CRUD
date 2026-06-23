package controller;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import service.HistorialVentasService;
import tads.CapaEntidadesTADS.DetalleVenta;
import tads.CapaEntidadesTADS.Venta;
import util.AlertUtil;
import util.DebounceUtil;
import util.FormatUtil;
import util.KeyboardUtil;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HistorialVentasController implements Initializable {
    @FXML private TextField txtBuscar;
    @FXML private DatePicker dpFecha;
    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, String> colFactura;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colHora;
    @FXML private TableColumn<Venta, String> colUsuario;
    @FXML private TableColumn<Venta, Double> colSubtotal;
    @FXML private TableColumn<Venta, Double> colIva;
    @FXML private TableColumn<Venta, Double> colTotal;
    @FXML private TableColumn<Venta, String> colMetodoPago;
    @FXML private VBox rootPane;

    private final HistorialVentasService historialService = new HistorialVentasService();
    private final ObservableList<Venta> listaVisual = FXCollections.observableArrayList();
    private final PauseTransition debounceBusqueda = DebounceUtil.crear(this::buscarVentas, 200);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colFactura.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colIva.setCellValueFactory(new PropertyValueFactory<>("iva"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colMetodoPago.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));

        tablaVentas.setFixedCellSize(32);
        tablaVentas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                verDetalleFactura();
            }
        });

        txtBuscar.textProperty().addListener((obs, anterior, actual) -> debounceBusqueda.playFromStart());
        dpFecha.valueProperty().addListener((obs, anterior, actual) -> buscarVentas());

        KeyboardUtil.configurarAtajos(rootPane, this::buscarVentas, txtBuscar::clear, null);
        buscarVentas();
    }

    @FXML
    private void buscarVentas() {
        String criterio = txtBuscar.getText();
        String fecha = dpFecha.getValue() == null ? "" : dpFecha.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        listaVisual.setAll(historialService.buscarVentas(criterio, fecha));
        tablaVentas.setItems(listaVisual);
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        dpFecha.setValue(null);
        buscarVentas();
    }

    @FXML
    private void verDetalleFactura() {
        Venta venta = tablaVentas.getSelectionModel().getSelectedItem();
        if (venta == null) {
            AlertUtil.mostrarAdvertencia("Seleccione una venta", "Debe seleccionar una factura para ver su detalle.");
            return;
        }

        List<DetalleVenta> detalles = historialService.obtenerDetalleVenta(venta.getIdVenta());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        agregarFila(grid, 0, "Número de factura:", venta.getIdVenta());
        agregarFila(grid, 1, "Fecha:", venta.getFecha());
        agregarFila(grid, 2, "Hora:", venta.getHora());
        agregarFila(grid, 3, "Usuario:", venta.getIdUsuario());
        agregarFila(grid, 4, "Método de pago:", venta.getMetodoPago());

        int fila = 5;
        agregarFila(grid, fila++, "Productos vendidos:", "");
        for (DetalleVenta detalle : detalles) {
            agregarFila(grid, fila++, detalle.getNombreProducto(),
                    "Cant: " + detalle.getCantidad()
                            + " | Unit: " + FormatUtil.dinero(detalle.getPrecioUnitario())
                            + " | Sub: " + FormatUtil.dinero(detalle.getSubtotal()));
        }

        agregarFila(grid, fila++, "Subtotal:", FormatUtil.dinero(venta.getSubtotal()));
        agregarFila(grid, fila++, "IVA:", FormatUtil.dinero(venta.getIva()));
        agregarFila(grid, fila, "Total final:", FormatUtil.dinero(venta.getTotal()));

        AlertUtil.mostrarDialogoPersonalizado("Detalle de factura", "Factura " + venta.getIdVenta(), grid);
    }

    private void agregarFila(GridPane grid, int fila, String etiqueta, String valor) {
        grid.add(new Label(etiqueta), 0, fila);
        Label lblValor = new Label(valor);
        lblValor.setWrapText(true);
        grid.add(lblValor, 1, fila);
    }
}
