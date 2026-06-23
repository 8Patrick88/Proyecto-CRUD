import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            //  El sistema ahora inicia de forma correcta en la pantalla de Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 400, 450);
            primaryStage.setTitle("Sistema Minimarket - Acceso");
            primaryStage.setScene(scene);

            // Evitar que agranden la ventana del login para mantener el diseño centrado
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println(" Error crítico al levantar el entorno del Login:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}