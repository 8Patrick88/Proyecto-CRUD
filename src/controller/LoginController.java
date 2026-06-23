package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Asegurarnos de que el mensaje de error inicie oculto
        lblError.setVisible(false);
    }

    /**
     * Valida las credenciales y realiza el cambio de pantalla dinámico
     */
    @FXML
    private void accionIngresar() {
        lblError.setVisible(false);

        String usuarioInput = txtUsuario.getText().trim();
        String passwordInput = txtPassword.getText().trim();

        // Validación simple para la defensa de hoy
        if (usuarioInput.equals("admin") && passwordInput.equals("1234")) {
            try {
                // 1. Cargar la vista del Dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
                Parent root = loader.load();

                // 2. Crear la nueva ventana (Stage) para el Dashboard
                Stage dashboardStage = new Stage();
                dashboardStage.setTitle("Sistema Minimarket - Panel de Control");
                dashboardStage.setScene(new Scene(root, 840, 600));

                // 3. Obtener la ventana actual (Login) a través de cualquier componente y cerrarla
                Stage loginStage = (Stage) txtUsuario.getScene().getWindow();
                loginStage.close();

                // 4. Mostrar el Dashboard
                dashboardStage.show();

            } catch (Exception e) {
                System.err.println("❌ Error al cambiar a la pantalla de Dashboard:");
                e.printStackTrace();
            }
        } else {
            // Mostrar mensaje de error visual si se equivocan
            lblError.setVisible(true);
        }
    }
}