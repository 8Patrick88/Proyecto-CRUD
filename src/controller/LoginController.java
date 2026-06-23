package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Usuario;
import service.AuthService;
import service.SesionUsuario;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private VBox rootPane;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblError.setVisible(false);
        txtPassword.setOnAction(event -> accionIngresar());
        util.KeyboardUtil.configurarAtajos(rootPane, this::accionIngresar, null, null);
    }

    /**
     * Valida las credenciales y realiza el cambio de pantalla dinámico
     */
    @FXML
    private void accionIngresar() {
        lblError.setVisible(false);

        String usuarioInput = txtUsuario.getText().trim();
        String passwordInput = txtPassword.getText().trim();

        if (usuarioInput.isEmpty()) {
            mostrarError("Ingrese el usuario.");
            return;
        }
        if (passwordInput.isEmpty()) {
            mostrarError("Ingrese la contraseña.");
            return;
        }

        Usuario usuarioAutenticado = authService.autenticar(usuarioInput, passwordInput);
        if (usuarioAutenticado != null) {
            abrirDashboard(usuarioAutenticado);
        } else {
            mostrarError("Usuario, contraseña o estado inválido.");
        }
    }

    private void abrirDashboard(Usuario usuarioAutenticado) {
        try {
            SesionUsuario.setUsuarioActual(usuarioAutenticado);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
            Parent root = loader.load();

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Sistema Minimarket - " + usuarioAutenticado.getRol());
            dashboardStage.setScene(new Scene(root, 1200, 700));

            Stage loginStage = (Stage) txtUsuario.getScene().getWindow();
            loginStage.close();

            dashboardStage.show();
        } catch (Exception e) {
            System.err.println("Error al cambiar a la pantalla de Dashboard:");
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
