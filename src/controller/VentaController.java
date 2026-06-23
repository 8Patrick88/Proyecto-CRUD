package controller;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import javafx.util.Duration;
import service.CatalogoService;
import service.VentaService;
import tads.CapaEntidadesTADS.DetalleVenta;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaEntidadesTADS.Venta;
import util.AlertUtil;
import util.DebounceUtil;
import util.FormatUtil;
import util.KeyboardUtil;
import util.StockUtil;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class VentaController implements Initializable {
    @FXML private BorderPane rootPane;
    @FXML private Label lblHora;
    @FXML private TextField txtBusqueda;
    @FXML private TextField txtCantidad;
    @FXML private Label lblProductoEncontrado;
    @FXML private TableView<DetalleVenta> tablaVenta;
    @FXML private TableColumn<DetalleVenta, String> colProducto;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colPrecioUnitario;
    @FXML private TableColumn<DetalleVenta, Double> colSubtotalDetalle;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private TextField txtPagoRecibido;
    @FXML private Label lblCambio;

    private final VentaService ventaService = new VentaService();
    private final CatalogoService catalogoService = new CatalogoService();
    private final ObservableList<DetalleVenta> detalles = FXCollections.observableArrayList();
    private final ObservableList<Producto> sugerencias = FXCollections.observableArrayList();
    private final Map<String, DetalleVenta> indiceDetalles = new HashMap<>();
    private final Map<String, Producto> catalogoCache = new HashMap<>();
    private final Popup popupSugerencias = new Popup();
    private final ListView<Producto> listaSugerencias = new ListView<>();
    private final PauseTransition debounceBusqueda = DebounceUtil.crear(this::ejecutarBusqueda, 120);

    private Producto productoSeleccionado;
    private double totalActual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        catalogoService.inicializarSistema();
        refrescarCatalogoCache();

        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotalDetalle.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaVenta.setItems(detalles);
        tablaVenta.setFixedCellSize(32);
        tablaVenta.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(DetalleVenta detalle, boolean empty) {
                super.updateItem(detalle, empty);
                if (empty || detalle == null) {
                    setStyle("");
                    return;
                }
                Producto producto = catalogoCache.get(detalle.getIdProducto());
                setStyle(producto != null ? StockUtil.estiloFila(producto) : "");
            }
        });

        cmbMetodoPago.setItems(FXCollections.observableArrayList("EFECTIVO", "TRANSFERENCIA"));
        cmbMetodoPago.getSelectionModel().select("EFECTIVO");

        configurarAutocompletado();
        configurarTecladoBusqueda();
        txtCantidad.setText("1");

        txtCantidad.setOnAction(event -> agregarProducto());
        txtPagoRecibido.textProperty().addListener((obs, anterior, actual) -> actualizarCambio());
        cmbMetodoPago.valueProperty().addListener((obs, anterior, actual) -> actualizarCambio());

        KeyboardUtil.configurarAtajos(rootPane,
                this::manejarEnter,
                this::cancelarVenta,
                this::eliminarProductoSeleccionado);

        iniciarReloj();
        iniciarNuevaVenta();
    }

    private void refrescarCatalogoCache() {
        catalogoCache.clear();
        catalogoCache.putAll(catalogoService.mapaProductos());
    }

    private void configurarAutocompletado() {
        listaSugerencias.setItems(sugerencias);
        listaSugerencias.setPrefWidth(520);
        listaSugerencias.setPrefHeight(160);
        listaSugerencias.setFixedCellSize(28);
        listaSugerencias.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                if (empty || producto == null) {
                    setText(null);
                } else {
                    setText(producto.getIdProducto() + " - " + producto.getNombre()
                            + " | Stock: " + producto.getCantidadStock()
                            + " | " + FormatUtil.dinero(producto.getPrecioVenta()));
                }
            }
        });
        popupSugerencias.getContent().add(listaSugerencias);

        txtBusqueda.textProperty().addListener((obs, anterior, actual) -> debounceBusqueda.playFromStart());

        listaSugerencias.setOnMouseClicked(event -> {
            Producto seleccionado = listaSugerencias.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                seleccionarProducto(seleccionado);
            }
        });

        txtBusqueda.focusedProperty().addListener((obs, anterior, enfocado) -> {
            if (!enfocado) {
                popupSugerencias.hide();
            }
        });
    }

    private void configurarTecladoBusqueda() {
        txtBusqueda.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!popupSugerencias.isShowing() || sugerencias.isEmpty()) {
                return;
            }

            int indice = listaSugerencias.getSelectionModel().getSelectedIndex();
            if (event.getCode() == KeyCode.DOWN) {
                listaSugerencias.getSelectionModel().select(Math.min(indice + 1, sugerencias.size() - 1));
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                listaSugerencias.getSelectionModel().select(Math.max(indice - 1, 0));
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                Producto seleccionado = listaSugerencias.getSelectionModel().getSelectedItem();
                if (seleccionado == null && !sugerencias.isEmpty()) {
                    seleccionado = sugerencias.get(0);
                }
                seleccionarProducto(seleccionado);
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                popupSugerencias.hide();
                event.consume();
            }
        });
    }

    private void ejecutarBusqueda() {
        String texto = txtBusqueda.getText();
        List<Producto> resultados = ventaService.buscarProductos(texto);
        sugerencias.setAll(resultados);

        if (!texto.trim().isEmpty() && !resultados.isEmpty()) {
            listaSugerencias.getSelectionModel().select(0);
            mostrarSugerencias();
        } else {
            popupSugerencias.hide();
            if (texto.trim().isEmpty()) {
                productoSeleccionado = null;
                lblProductoEncontrado.setText("Sin producto seleccionado");
                lblProductoEncontrado.setStyle("-fx-text-fill: #7f8c8d;");
            }
        }
    }

    private void manejarEnter() {
        if (txtBusqueda.isFocused()) {
            if (popupSugerencias.isShowing() && !sugerencias.isEmpty()) {
                Producto seleccionado = listaSugerencias.getSelectionModel().getSelectedItem();
                seleccionarProducto(seleccionado != null ? seleccionado : sugerencias.get(0));
            } else {
                buscarProducto();
            }
        } else if (txtCantidad.isFocused()) {
            agregarProducto();
        } else {
            finalizarVenta();
        }
    }

    private void mostrarSugerencias() {
        Bounds bounds = txtBusqueda.localToScreen(txtBusqueda.getBoundsInLocal());
        if (bounds != null) {
            popupSugerencias.show(txtBusqueda, bounds.getMinX(), bounds.getMaxY());
        }
    }

    private void seleccionarProducto(Producto producto) {
        if (producto == null) {
            return;
        }
        productoSeleccionado = producto;
        txtBusqueda.setText(producto.getIdProducto() + " - " + producto.getNombre());
        popupSugerencias.hide();
        mostrarProductoSeleccionado();
        txtCantidad.requestFocus();
        txtCantidad.selectAll();
    }

    @FXML
    private void buscarProducto() {
        productoSeleccionado = ventaService.buscarProducto(txtBusqueda.getText());
        if (productoSeleccionado == null) {
            lblProductoEncontrado.setText("Producto no encontrado");
            lblProductoEncontrado.setStyle("-fx-text-fill: #c0392b;");
            return;
        }
        mostrarProductoSeleccionado();
        txtCantidad.requestFocus();
    }

    private void mostrarProductoSeleccionado() {
        StockUtil.NivelStock nivel = StockUtil.calcularNivel(productoSeleccionado);
        lblProductoEncontrado.setText(productoSeleccionado.getIdProducto() + " - " + productoSeleccionado.getNombre()
                + " | Stock: " + productoSeleccionado.getCantidadStock()
                + " | Precio: " + FormatUtil.dinero(productoSeleccionado.getPrecioVenta())
                + " | " + nivel.getEtiqueta());
        lblProductoEncontrado.setStyle("-fx-text-fill: " + nivel.getColorTexto() + "; -fx-font-weight: bold;");
    }

    @FXML
    private void incrementarCantidad() {
        txtCantidad.setText(String.valueOf(obtenerCantidadActual() + 1));
    }

    @FXML
    private void decrementarCantidad() {
        int cantidad = obtenerCantidadActual();
        if (cantidad > 1) {
            txtCantidad.setText(String.valueOf(cantidad - 1));
        }
    }

    private int obtenerCantidadActual() {
        try {
            return Math.max(1, Integer.parseInt(txtCantidad.getText().trim()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @FXML
    private void agregarProducto() {
        if (productoSeleccionado == null) {
            buscarProducto();
        }
        if (productoSeleccionado == null) {
            AlertUtil.mostrarAdvertencia("Producto inválido", "No existe un producto con ese código o nombre.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException e) {
            AlertUtil.mostrarAdvertencia("Cantidad inválida", "La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            AlertUtil.mostrarAdvertencia("Cantidad inválida", "La cantidad debe ser mayor que cero.");
            return;
        }

        DetalleVenta existente = indiceDetalles.get(productoSeleccionado.getIdProducto());
        int cantidadActualEnVenta = existente == null ? 0 : existente.getCantidad();
        if (cantidadActualEnVenta + cantidad > productoSeleccionado.getCantidadStock()) {
            AlertUtil.mostrarError("Stock insuficiente", "Stock insuficiente para completar la venta.");
            return;
        }

        if (existente != null) {
            existente.aumentarCantidad(cantidad);
            tablaVenta.refresh();
        } else {
            DetalleVenta nuevo = new DetalleVenta(
                    productoSeleccionado.getIdProducto(),
                    productoSeleccionado.getNombre(),
                    cantidad,
                    productoSeleccionado.getPrecioVenta()
            );
            detalles.add(nuevo);
            indiceDetalles.put(nuevo.getIdProducto(), nuevo);
        }

        FXCollections.sort(detalles, Comparator.comparing(DetalleVenta::getNombreProducto));
        limpiarEntradaProducto();
        actualizarResumen();
    }

    @FXML
    private void finalizarVenta() {
        if (detalles.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Venta vacía", "No se puede finalizar una venta sin productos.");
            return;
        }

        String metodoPago = cmbMetodoPago.getValue();
        if (metodoPago == null || metodoPago.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Método de pago", "Seleccione un método de pago.");
            return;
        }

        if ("EFECTIVO".equals(metodoPago)) {
            Double pagoRecibido = obtenerPagoRecibido();
            if (pagoRecibido == null || pagoRecibido < totalActual) {
                AlertUtil.mostrarAdvertencia("Pago insuficiente", "El pago recibido debe cubrir el total de la venta.");
                return;
            }
        }

        if (!AlertUtil.confirmar("Finalizar venta", "¿Desea confirmar la venta?",
                "Se descontará el stock y se guardará la factura.")) {
            return;
        }

        Venta venta = ventaService.finalizarVenta(detalles, metodoPago);
        refrescarCatalogoCache();
        mostrarFactura(generarFactura(venta));
        iniciarNuevaVenta();
    }

    @FXML
    private void cancelarVenta() {
        if (detalles.isEmpty() && formularioEntradaVacio()) {
            iniciarNuevaVenta();
            return;
        }

        if (AlertUtil.confirmar("Cancelar venta", "¿Desea cancelar la venta actual?",
                "Los productos agregados se eliminarán de la venta en pantalla.")) {
            iniciarNuevaVenta();
        }
    }

    @FXML
    private void eliminarProductoSeleccionado() {
        DetalleVenta seleccionado = tablaVenta.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            detalles.remove(seleccionado);
            indiceDetalles.remove(seleccionado.getIdProducto());
            actualizarResumen();
        }
    }

    private void actualizarResumen() {
        double[] totales = ventaService.calcularTotales(detalles);
        lblSubtotal.setText(FormatUtil.dinero(totales[0]));
        lblIva.setText(FormatUtil.dinero(totales[1]));
        lblTotal.setText(FormatUtil.dinero(totales[2]));
        totalActual = totales[2];
        actualizarCambio();
    }

    private void actualizarCambio() {
        if (!"EFECTIVO".equals(cmbMetodoPago.getValue())) {
            lblCambio.setText("$0.00");
            txtPagoRecibido.setDisable(true);
            txtPagoRecibido.clear();
            return;
        }

        txtPagoRecibido.setDisable(false);
        Double pagoRecibido = obtenerPagoRecibido();
        if (pagoRecibido == null) {
            lblCambio.setText("$0.00");
            return;
        }

        lblCambio.setText(FormatUtil.dinero(Math.max(0.0, pagoRecibido - totalActual)));
    }

    private Double obtenerPagoRecibido() {
        String texto = txtPagoRecibido.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String generarFactura(Venta venta) {
        StringBuilder factura = new StringBuilder(256);
        factura.append("FACTURA ").append(venta.getIdVenta()).append('\n');
        factura.append("Fecha: ").append(venta.getFecha()).append('\n');
        factura.append("Hora: ").append(venta.getHora()).append('\n');
        factura.append("Usuario: ").append(venta.getIdUsuario()).append("\n\n");

        for (DetalleVenta detalle : detalles) {
            factura.append(detalle.getNombreProducto())
                    .append(" x").append(detalle.getCantidad())
                    .append(" @ ").append(FormatUtil.dinero(detalle.getPrecioUnitario()))
                    .append(" -> ").append(FormatUtil.dinero(detalle.getSubtotal()))
                    .append('\n');
        }

        factura.append("\nSubtotal: ").append(FormatUtil.dinero(venta.getSubtotal())).append('\n');
        factura.append("IVA (15%): ").append(FormatUtil.dinero(venta.getIva())).append('\n');
        factura.append("TOTAL: ").append(FormatUtil.dinero(venta.getTotal())).append('\n');
        factura.append("Pago: ").append(venta.getMetodoPago()).append('\n');

        if ("EFECTIVO".equals(venta.getMetodoPago())) {
            Double pagoRecibido = obtenerPagoRecibido();
            if (pagoRecibido != null) {
                factura.append("Recibido: ").append(FormatUtil.dinero(pagoRecibido)).append('\n');
                factura.append("Cambio: ").append(FormatUtil.dinero(Math.max(0.0, pagoRecibido - venta.getTotal()))).append('\n');
            }
        }

        return factura.toString();
    }

    private void mostrarFactura(String factura) {
        TextArea contenido = new TextArea(factura);
        contenido.setEditable(false);
        contenido.setWrapText(true);
        contenido.setPrefWidth(420);
        contenido.setPrefHeight(320);

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Venta completada");
        alerta.setHeaderText("Venta registrada correctamente");
        alerta.getDialogPane().setContent(contenido);
        alerta.showAndWait();
    }

    private void iniciarNuevaVenta() {
        detalles.clear();
        indiceDetalles.clear();
        limpiarEntradaProducto();
        cmbMetodoPago.getSelectionModel().select("EFECTIVO");
        txtPagoRecibido.clear();
        txtCantidad.setText("1");
        actualizarResumen();
        txtBusqueda.requestFocus();
    }

    private void limpiarEntradaProducto() {
        txtBusqueda.clear();
        txtCantidad.setText("1");
        lblProductoEncontrado.setText("Sin producto seleccionado");
        lblProductoEncontrado.setStyle("-fx-text-fill: #7f8c8d;");
        productoSeleccionado = null;
        popupSugerencias.hide();
        txtBusqueda.requestFocus();
    }

    private boolean formularioEntradaVacio() {
        return txtBusqueda.getText().trim().isEmpty();
    }

    private void iniciarReloj() {
        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1),
                event -> lblHora.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }
}
