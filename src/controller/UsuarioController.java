package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Usuario;
import service.SesionUsuario;
import service.UsuarioService;
import util.AlertUtil;
import util.KeyboardUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class UsuarioController implements Initializable {
    @FXML private TextField txtNombre;
    @FXML private TextField txtUsuario;
    @FXML private TextField txtContrasena;
    @FXML private ComboBox<String> cmbRol;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private javafx.scene.layout.VBox rootPane;

    private final UsuarioService usuarioService = new UsuarioService();
    private final ObservableList<Usuario> listaVisual = FXCollections.observableArrayList();
    private Usuario usuarioSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbRol.setItems(FXCollections.observableArrayList("ADMIN", "EMPLEADO"));
        cmbEstado.setItems(FXCollections.observableArrayList("ACTIVO", "INACTIVO"));
        cmbRol.getSelectionModel().select("EMPLEADO");
        cmbEstado.getSelectionModel().select("ACTIVO");

        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> cargarFormulario(actual));
        KeyboardUtil.configurarAtajos(rootPane, this::registrarUsuario, this::limpiarFormulario, null);
        actualizarTabla();
    }

    @FXML
    private void registrarUsuario() {
        String error = validarFormulario();
        if (error != null) {
            AlertUtil.mostrarAdvertencia("Validación", error);
            return;
        }

        boolean registrado = usuarioService.registrarUsuario(
                txtNombre.getText().trim(),
                txtUsuario.getText().trim(),
                txtContrasena.getText().trim(),
                cmbRol.getValue(),
                cmbEstado.getValue()
        );

        if (registrado) {
            limpiarFormulario();
            actualizarTabla();
            AlertUtil.mostrarExito("Registro exitoso", "Usuario registrado correctamente.");
        } else {
            AlertUtil.mostrarAdvertencia("No se pudo registrar", "El nombre de usuario ya existe.");
        }
    }

    @FXML
    private void actualizarUsuario() {
        if (usuarioSeleccionado == null) {
            AlertUtil.mostrarAdvertencia("Seleccione un usuario", "Debe seleccionar un usuario de la tabla.");
            return;
        }

        String error = validarFormulario();
        if (error != null) {
            AlertUtil.mostrarAdvertencia("Validación", error);
            return;
        }

        Usuario actualizado = new Usuario(
                usuarioSeleccionado.getIdUsuario(),
                txtNombre.getText().trim(),
                txtUsuario.getText().trim(),
                txtContrasena.getText().trim(),
                cmbRol.getValue(),
                cmbEstado.getValue()
        );

        if (usuarioService.actualizarUsuario(actualizado)) {
            limpiarFormulario();
            actualizarTabla();
            AlertUtil.mostrarExito("Actualización exitosa", "Usuario actualizado correctamente.");
        } else {
            AlertUtil.mostrarAdvertencia("No se pudo actualizar", "El nombre de usuario ya existe.");
        }
    }

    @FXML
    private void desactivarUsuario() {
        if (usuarioSeleccionado == null) {
            AlertUtil.mostrarAdvertencia("Seleccione un usuario", "Debe seleccionar un usuario de la tabla.");
            return;
        }

        if (SesionUsuario.getUsuarioActual() != null
                && usuarioSeleccionado.getIdUsuario().equals(SesionUsuario.getUsuarioActual().getIdUsuario())) {
            AlertUtil.mostrarAdvertencia("Operación no permitida", "No puede desactivar el usuario con sesión activa.");
            return;
        }

        if (!AlertUtil.confirmar("Desactivar usuario", "¿Desea desactivar este usuario?",
                usuarioSeleccionado.getNombre() + " no podrá iniciar sesión.")) {
            return;
        }

        Usuario desactivado = new Usuario(
                usuarioSeleccionado.getIdUsuario(),
                usuarioSeleccionado.getNombre(),
                usuarioSeleccionado.getUsuario(),
                usuarioSeleccionado.getContrasena(),
                usuarioSeleccionado.getRol(),
                "INACTIVO"
        );

        if (usuarioService.actualizarUsuario(desactivado)) {
            limpiarFormulario();
            actualizarTabla();
            AlertUtil.mostrarExito("Usuario desactivado", "El usuario fue desactivado correctamente.");
        }
    }

    @FXML
    private void limpiarFormulario() {
        usuarioSeleccionado = null;
        tablaUsuarios.getSelectionModel().clearSelection();
        txtNombre.clear();
        txtUsuario.clear();
        txtContrasena.clear();
        cmbRol.getSelectionModel().select("EMPLEADO");
        cmbEstado.getSelectionModel().select("ACTIVO");
    }

    private void cargarFormulario(Usuario usuario) {
        usuarioSeleccionado = usuario;
        if (usuario == null) {
            return;
        }

        txtNombre.setText(usuario.getNombre());
        txtUsuario.setText(usuario.getUsuario());
        txtContrasena.setText(usuario.getContrasena());
        cmbRol.getSelectionModel().select(usuario.getRol());
        cmbEstado.getSelectionModel().select(usuario.getEstado());
    }

    private void actualizarTabla() {
        listaVisual.setAll(usuarioService.listarUsuarios());
        tablaUsuarios.setItems(listaVisual);
    }

    private String validarFormulario() {
        if (txtNombre.getText().trim().isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (txtUsuario.getText().trim().isEmpty()) {
            return "El usuario es obligatorio.";
        }
        if (txtContrasena.getText().trim().isEmpty()) {
            return "La contraseña es obligatoria.";
        }
        if (cmbRol.getValue() == null) {
            return "Debe seleccionar un rol.";
        }
        if (cmbEstado.getValue() == null) {
            return "Debe seleccionar un estado.";
        }
        return null;
    }
}
