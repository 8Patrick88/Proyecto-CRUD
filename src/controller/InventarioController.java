package controller;

import dao.ProductoDAO;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import service.CatalogoService;
import service.MovimientoInventarioService;
import service.SesionUsuario;
import tads.CapaEntidadesTADS.Producto;
import tads.CapaEntidadesTADS.Proveedor;
import datastore.DataStore;
import util.AlertUtil;
import util.DebounceUtil;
import util.FormatUtil;
import util.KeyboardUtil;
import util.StockUtil;

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
    @FXML private Button btnEliminar;
    @FXML private VBox rootPane;

    private final ObservableList<Producto> listaVisual = FXCollections.observableArrayList();
    private final CatalogoService catalogoService = new CatalogoService();
    private final ProductoDAO productoDAO = new ProductoDAO("productos.csv");
    private final MovimientoInventarioService movimientoService = new MovimientoInventarioService();
    private final PauseTransition debounceBusqueda = DebounceUtil.crear(this::accionBuscar, 150);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidadStock"));
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colEstado.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                Producto producto = getTableRow().getItem();
                setText(StockUtil.calcularNivel(producto).getEtiqueta());
                setStyle(StockUtil.estiloEstado(producto));
            }
        });

        tablaProductos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                setStyle(empty || producto == null ? "" : StockUtil.estiloFila(producto));
            }
        });
        tablaProductos.setFixedCellSize(32);

        catalogoService.inicializarSistema();
        btnEliminar.setVisible(SesionUsuario.esAdmin());
        btnEliminar.setManaged(SesionUsuario.esAdmin());

        txtBuscarId.textProperty().addListener((obs, anterior, actual) -> debounceBusqueda.playFromStart());
        KeyboardUtil.configurarAtajos(rootPane, this::accionBuscar, txtBuscarId::clear, this::accionEliminar);
        actualizarTablaCompleta();
    }

    private void actualizarTablaCompleta() {
        listaVisual.setAll(catalogoService.listarProductos());
        tablaProductos.setItems(listaVisual);
    }

    @FXML
    private void accionBuscar() {
        listaVisual.setAll(catalogoService.filtrarProductos(txtBuscarId.getText()));
        tablaProductos.setItems(listaVisual);
    }

    @FXML
    private void accionRestaurar() {
        txtBuscarId.clear();
        actualizarTablaCompleta();
    }

    @FXML
    private void verDetalles() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Seleccione un producto", "Debe seleccionar un producto para ver sus detalles.");
            return;
        }

        Proveedor proveedor = DataStore.proveedores.buscarPorId(seleccionado.getIdProveedorAsociado());
        String nombreProveedor = proveedor != null ? proveedor.getNombre() : seleccionado.getIdProveedorAsociado();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        agregarFila(grid, 0, "ID:", seleccionado.getIdProducto());
        agregarFila(grid, 1, "Nombre:", seleccionado.getNombre());
        agregarFila(grid, 2, "Categoría:", seleccionado.getCategoria());
        agregarFila(grid, 3, "Precio de compra:", FormatUtil.dinero(seleccionado.getPrecioCompra()));
        agregarFila(grid, 4, "Precio de venta:", FormatUtil.dinero(seleccionado.getPrecioVenta()));
        agregarFila(grid, 5, "Proveedor:", nombreProveedor);
        agregarFila(grid, 6, "Stock actual:", String.valueOf(seleccionado.getCantidadStock()));
        agregarFila(grid, 7, "Stock mínimo:", String.valueOf(seleccionado.getStockMinimo()));
        agregarFila(grid, 8, "Ganancia unitaria:", FormatUtil.dinero(StockUtil.calcularGananciaUnitaria(
                seleccionado.getPrecioVenta(), seleccionado.getPrecioCompra())));
        agregarFila(grid, 9, "Estado:", StockUtil.calcularNivel(seleccionado).getEtiqueta());

        AlertUtil.mostrarDialogoPersonalizado("Detalle de producto", seleccionado.getNombre(), grid);
    }

    @FXML
    private void accionEliminar() {
        if (!SesionUsuario.esAdmin()) {
            return;
        }

        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Seleccione un producto", "Debe seleccionar un producto para eliminar.");
            return;
        }

        if (!AlertUtil.confirmar("Eliminar producto",
                "¿Desea eliminar el producto seleccionado?",
                seleccionado.getNombre() + " será eliminado del inventario.")) {
            return;
        }

        int stockAnterior = seleccionado.getCantidadStock();
        if (DataStore.productos.eliminar(seleccionado.getIdProducto())) {
            movimientoService.registrar(
                    MovimientoInventarioService.ELIMINACION,
                    seleccionado.getIdProducto(),
                    seleccionado.getNombre(),
                    stockAnterior,
                    0,
                    "Eliminación desde inventario"
            );
            productoDAO.sobrescribirProductosEnCSV(DataStore.productos);
            actualizarTablaCompleta();
            AlertUtil.mostrarExito("Producto eliminado", "El producto fue eliminado correctamente.");
        }
    }

    private void agregarFila(GridPane grid, int fila, String etiqueta, String valor) {
        grid.add(new Label(etiqueta), 0, fila);
        Label lblValor = new Label(valor);
        lblValor.setWrapText(true);
        grid.add(lblValor, 1, fila);
    }
}
