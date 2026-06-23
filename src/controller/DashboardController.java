package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // Este es el contenedor del centro del BorderPane
    @FXML private StackPane contenedorCentral;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Al arrancar, podemos hacer que muestre el inventario por defecto
        cambiarModulo("/view/InventarioModulo.fxml");
    }

    @FXML
    private void mostrarModuloInventario() {
        cambiarModulo("/view/InventarioModulo.fxml");
    }

    @FXML
    private void mostrarModuloRegistrar() {
        cambiarModulo("/view/RegistrarModulo.fxml");
    }

    @FXML
    private void mostrarModuloProveedores() {
        cambiarModulo("/view/ProveedoresModulo.fxml");
    }

    @FXML
    private void mostrarModuloVentas() {
        cambiarModulo("/view/Venta.fxml");
    }

    /**
     * Método genérico que limpia el centro del dashboard y carga el módulo elegido
     */
    private void cambiarModulo(String rutaFXML) {
        try {
            contenedorCentral.getChildren().clear(); // Borra el módulo anterior

            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent nodoModulo = loader.load();

            contenedorCentral.getChildren().add(nodoModulo); // Mete el nuevo módulo

        } catch (IOException e) {
            System.err.println("Error al cargar el módulo: " + rutaFXML);
            e.printStackTrace();
        }
    }
}











