package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import service.CatalogoService;
import service.NotificacionService;
import service.SesionUsuario;
import util.AlertUtil;
import model.Usuario;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    private static final String ESTILO_BOTON = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 11;";
    private static final String ESTILO_BOTON_ACTIVO = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 11; -fx-font-weight: bold;";

    @FXML private StackPane contenedorCentral;
    @FXML private StackPane rootPane;
    @FXML private Label lblUsuarioSesion;
    @FXML private Label lblContadorNotificaciones;
    @FXML private Button btnVentas;
    @FXML private Button btnInventario;
    @FXML private Button btnRegistrarProducto;
    @FXML private Button btnHistorialVentas;
    @FXML private Button btnMovimientosInventario;
    @FXML private Button btnUsuarios;
    @FXML private Button btnProveedores;
    @FXML private Button btnNotificaciones;

    private final CatalogoService catalogoService = new CatalogoService();
    private final NotificacionService notificacionService = new NotificacionService();
    private final Map<String, Button> botonesModulo = new HashMap<>();
    private String moduloActivo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        registrarBotonesModulo();
        catalogoService.inicializarSistema();
        configurarAccesoPorRol();
        actualizarNotificaciones();
    }

    private void registrarBotonesModulo() {
        botonesModulo.put("/view/Venta.fxml", btnVentas);
        botonesModulo.put("/view/InventarioModulo.fxml", btnInventario);
        botonesModulo.put("/view/RegistrarModulo.fxml", btnRegistrarProducto);
        botonesModulo.put("/view/HistorialVentasModulo.fxml", btnHistorialVentas);
        botonesModulo.put("/view/MovimientoInventarioModulo.fxml", btnMovimientosInventario);
        botonesModulo.put("/view/UsuariosModulo.fxml", btnUsuarios);
        botonesModulo.put("/view/ProveedoresModulo.fxml", btnProveedores);
    }

    @FXML private void mostrarModuloVentas() { cambiarModulo("/view/Venta.fxml"); }
    @FXML private void mostrarModuloInventario() { cambiarModulo("/view/InventarioModulo.fxml"); }
    @FXML private void mostrarModuloRegistrar() { cambiarModulo("/view/RegistrarModulo.fxml"); }
    @FXML private void mostrarModuloHistorialVentas() { if (SesionUsuario.esAdmin()) cambiarModulo("/view/HistorialVentasModulo.fxml"); }
    @FXML private void mostrarModuloMovimientosInventario() { if (SesionUsuario.esAdmin()) cambiarModulo("/view/MovimientoInventarioModulo.fxml"); }
    @FXML private void mostrarModuloProveedores() { cambiarModulo("/view/ProveedoresModulo.fxml"); }
    @FXML private void mostrarModuloUsuarios() { if (SesionUsuario.esAdmin()) cambiarModulo("/view/UsuariosModulo.fxml"); }

    @FXML
    private void mostrarNotificaciones() {
        List<String> notificaciones = notificacionService.obtenerNotificaciones();
        TextArea contenido = new TextArea(notificaciones.isEmpty()
                ? "No hay notificaciones pendientes."
                : String.join("\n\n", notificaciones));
        contenido.setEditable(false);
        contenido.setWrapText(true);
        contenido.setPrefWidth(420);
        contenido.setPrefHeight(280);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(280);
        AlertUtil.mostrarDialogoPersonalizado("Notificaciones",
                "Alertas del sistema (" + notificaciones.size() + ")", scroll);
    }

    @FXML
    private void cerrarSesion() {
        if (!AlertUtil.confirmar("Cerrar sesión", "¿Desea cerrar sesión?", "Se cerrará la sesión actual.")) {
            return;
        }

        SesionUsuario.cerrarSesion();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            javafx.stage.Stage loginStage = new javafx.stage.Stage();
            loginStage.setTitle("Sistema Minimarket - Acceso");
            loginStage.setScene(new javafx.scene.Scene(root, 400, 450));

            javafx.stage.Stage actual = (javafx.stage.Stage) rootPane.getScene().getWindow();
            actual.close();
            loginStage.show();
        } catch (IOException e) {
            System.err.println("Error al cerrar sesión.");
            e.printStackTrace();
        }
    }

    private void configurarAccesoPorRol() {
        Usuario usuario = SesionUsuario.getUsuarioActual();
        if (usuario == null) {
            lblUsuarioSesion.setText("Sin sesión");
            ocultarModulosAdmin();
            cambiarModulo("/view/Venta.fxml");
            return;
        }

        lblUsuarioSesion.setText(usuario.getNombre() + " (" + usuario.getRol() + ")");

        mostrarBoton(btnVentas);
        mostrarBoton(btnInventario);
        mostrarBoton(btnNotificaciones);

        if (usuario.esAdmin()) {
            mostrarBoton(btnRegistrarProducto);
            mostrarBoton(btnHistorialVentas);
            mostrarBoton(btnMovimientosInventario);
            mostrarBoton(btnUsuarios);
            mostrarBoton(btnProveedores);
        } else {
            ocultarModulosAdmin();
        }

        cambiarModulo("/view/Venta.fxml");
    }

    private void ocultarModulosAdmin() {
        ocultarBoton(btnRegistrarProducto);
        ocultarBoton(btnHistorialVentas);
        ocultarBoton(btnMovimientosInventario);
        ocultarBoton(btnUsuarios);
        ocultarBoton(btnProveedores);
    }

    private void mostrarBoton(Button boton) {
        boton.setVisible(true);
        boton.setManaged(true);
    }

    private void ocultarBoton(Button boton) {
        boton.setVisible(false);
        boton.setManaged(false);
    }

    private void actualizarNotificaciones() {
        int cantidad = notificacionService.contarNotificaciones();
        lblContadorNotificaciones.setText(String.valueOf(cantidad));
        lblContadorNotificaciones.setVisible(cantidad > 0);
        lblContadorNotificaciones.setManaged(cantidad > 0);
    }

    private void resaltarModuloActivo(String rutaFXML) {
        moduloActivo = rutaFXML;
        for (Map.Entry<String, Button> entry : botonesModulo.entrySet()) {
            Button boton = entry.getValue();
            if (boton.isVisible()) {
                boton.setStyle(entry.getKey().equals(rutaFXML) ? ESTILO_BOTON_ACTIVO : ESTILO_BOTON);
            }
        }
    }

    private void cambiarModulo(String rutaFXML) {
        if (rutaFXML.equals(moduloActivo) && !contenedorCentral.getChildren().isEmpty()) {
            actualizarNotificaciones();
            return;
        }

        try {
            contenedorCentral.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent nodoModulo = loader.load();
            contenedorCentral.getChildren().add(nodoModulo);
            resaltarModuloActivo(rutaFXML);
            actualizarNotificaciones();
        } catch (IOException e) {
            System.err.println("Error al cargar el módulo: " + rutaFXML);
            e.printStackTrace();
        }
    }
}
