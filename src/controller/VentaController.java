package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import service.VentaService;
import tads.CapaEntidadesTADS.DetalleVenta;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaEntidadesTADS.Venta;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;

public class VentaController implements Initializable {
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
    private final ObservableList<DetalleVenta> detalles = FXCollections.observableArrayList();
    private final DecimalFormat dinero = new DecimalFormat("$0.00");
    private Producto productoSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ventaService.cargarProductosSiEsNecesario();

        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotalDetalle.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaVenta.setItems(detalles);
        tablaVenta.setRowFactory(tv -> new TableRow<DetalleVenta>() {
            @Override
            protected void updateItem(DetalleVenta detalle, boolean empty) {
                super.updateItem(detalle, empty);
                if (empty || detalle == null) {
                    setStyle("");
                    return;
                }
                Producto producto = ventaService.buscarProducto(detalle.getIdProducto());
                setStyle(producto != null && producto.isEstadoAlertado() ? "-fx-background-color: #fdecea;" : "");
            }
        });

        cmbMetodoPago.setItems(FXCollections.observableArrayList("EFECTIVO", "TRANSFERENCIA"));
        cmbMetodoPago.getSelectionModel().select("EFECTIVO");

        txtBusqueda.setOnAction(event -> buscarProducto());
        txtCantidad.setOnAction(event -> agregarProducto());
        txtPagoRecibido.textProperty().addListener((obs, anterior, actual) -> actualizarCambio());
        cmbMetodoPago.valueProperty().addListener((obs, anterior, actual) -> actualizarCambio());
        tablaVenta.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                eliminarProductoSeleccionado();
            }
        });

        iniciarReloj();
        iniciarNuevaVenta();
    }

    @FXML
    private void buscarProducto() {
        productoSeleccionado = ventaService.buscarProducto(txtBusqueda.getText());
        if (productoSeleccionado == null) {
            lblProductoEncontrado.setText("Producto no encontrado");
            lblProductoEncontrado.setStyle("-fx-text-fill: #c0392b;");
            return;
        }

        lblProductoEncontrado.setText(productoSeleccionado.getIdProducto() + " - " + productoSeleccionado.getNombre()
                + " | Stock: " + productoSeleccionado.getCantidadStock()
                + " | Precio: " + dinero.format(productoSeleccionado.getPrecioVenta()));
        lblProductoEncontrado.setStyle(productoSeleccionado.isEstadoAlertado() ? "-fx-text-fill: #c0392b;" : "-fx-text-fill: #2c3e50;");
        txtCantidad.requestFocus();
    }

    @FXML
    private void agregarProducto() {
        if (productoSeleccionado == null) {
            buscarProducto();
        }
        if (productoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Producto inválido", "No existe un producto con ese código o nombre.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cantidad inválida", "La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cantidad inválida", "La cantidad debe ser mayor que cero.");
            return;
        }

        DetalleVenta existente = buscarDetalle(productoSeleccionado.getIdProducto());
        int cantidadActualEnVenta = existente == null ? 0 : existente.getCantidad();
        if (cantidadActualEnVenta + cantidad > productoSeleccionado.getCantidadStock()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Stock insuficiente", "Stock insuficiente para completar la venta.");
            return;
        }

        if (existente != null) {
            existente.aumentarCantidad(cantidad);
            tablaVenta.refresh();
        } else {
            detalles.add(new DetalleVenta(
                    productoSeleccionado.getIdProducto(),
                    productoSeleccionado.getNombre(),
                    cantidad,
                    productoSeleccionado.getPrecioVenta()
            ));
        }

        FXCollections.sort(detalles, Comparator.comparing(DetalleVenta::getNombreProducto));
        limpiarEntradaProducto();
        actualizarResumen();
    }

    @FXML
    private void finalizarVenta() {
        if (detalles.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Venta vacía", "No se puede finalizar una venta sin productos.");
            return;
        }

        String metodoPago = cmbMetodoPago.getValue();
        if (metodoPago == null || metodoPago.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Método de pago", "Seleccione un método de pago.");
            return;
        }

        if ("EFECTIVO".equals(metodoPago)) {
            Double pagoRecibido = obtenerPagoRecibido();
            if (pagoRecibido == null || pagoRecibido < ventaService.calcularTotal(detalles)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Pago insuficiente", "El pago recibido debe cubrir el total de la venta.");
                return;
            }
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Finalizar venta");
        confirmacion.setHeaderText("¿Desea confirmar la venta?");
        confirmacion.setContentText("Se descontará el stock y se guardará la factura.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        Venta venta = ventaService.finalizarVenta(detalles, metodoPago);
        String factura = generarFactura(venta);
        mostrarFactura(factura);
        iniciarNuevaVenta();
    }

    @FXML
    private void cancelarVenta() {
        if (detalles.isEmpty() && formularioEntradaVacio()) {
            iniciarNuevaVenta();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar venta");
        confirmacion.setHeaderText("¿Desea cancelar la venta actual?");
        confirmacion.setContentText("Los productos agregados se eliminarán de la venta en pantalla.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            iniciarNuevaVenta();
        }
    }

    @FXML
    private void eliminarProductoSeleccionado() {
        DetalleVenta seleccionado = tablaVenta.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            detalles.remove(seleccionado);
            actualizarResumen();
        }
    }

    private DetalleVenta buscarDetalle(String idProducto) {
        for (DetalleVenta detalle : detalles) {
            if (detalle.getIdProducto().equals(idProducto)) {
                return detalle;
            }
        }
        return null;
    }

    private void actualizarResumen() {
        lblSubtotal.setText(dinero.format(ventaService.calcularSubtotal(detalles)));
        lblIva.setText(dinero.format(ventaService.calcularIva(detalles)));
        lblTotal.setText(dinero.format(ventaService.calcularTotal(detalles)));
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

        double cambio = Math.max(0.0, pagoRecibido - ventaService.calcularTotal(detalles));
        lblCambio.setText(dinero.format(cambio));
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
        StringBuilder factura = new StringBuilder();
        factura.append("FACTURA ").append(venta.getIdVenta()).append("\n");
        factura.append("Fecha: ").append(venta.getFechaHora()).append("\n\n");

        for (DetalleVenta detalle : detalles) {
            factura.append(detalle.getNombreProducto())
                    .append(" x").append(detalle.getCantidad())
                    .append(" -> ").append(dinero.format(detalle.getSubtotal()))
                    .append("\n");
        }

        factura.append("\nSubtotal: ").append(dinero.format(venta.getSubtotal())).append("\n");
        factura.append("IVA (15%): ").append(dinero.format(venta.getIva())).append("\n");
        factura.append("TOTAL: ").append(dinero.format(venta.getTotal())).append("\n");
        factura.append("Pago: ").append(venta.getMetodoPago()).append("\n");

        if ("EFECTIVO".equals(venta.getMetodoPago())) {
            Double pagoRecibido = obtenerPagoRecibido();
            if (pagoRecibido != null) {
                factura.append("Recibido: ").append(dinero.format(pagoRecibido)).append("\n");
                factura.append("Cambio: ").append(dinero.format(Math.max(0.0, pagoRecibido - venta.getTotal()))).append("\n");
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
        limpiarEntradaProducto();
        cmbMetodoPago.getSelectionModel().select("EFECTIVO");
        txtPagoRecibido.clear();
        actualizarResumen();
        txtBusqueda.requestFocus();
    }

    private void limpiarEntradaProducto() {
        txtBusqueda.clear();
        txtCantidad.clear();
        lblProductoEncontrado.setText("Sin producto seleccionado");
        lblProductoEncontrado.setStyle("-fx-text-fill: #7f8c8d;");
        productoSeleccionado = null;
        txtBusqueda.requestFocus();
    }

    private boolean formularioEntradaVacio() {
        return txtBusqueda.getText().trim().isEmpty() && txtCantidad.getText().trim().isEmpty();
    }

    private void iniciarReloj() {
        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            lblHora.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
