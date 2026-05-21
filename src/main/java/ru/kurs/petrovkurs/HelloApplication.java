package ru.kurs.petrovkurs;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Открываем окно входа
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/kurs/petrovkurs/login-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Вход в систему");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            try {
                Image icon = new Image(getClass().getResource("/images/icon_main.png").toExternalForm());
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Иконка не найдена");
            }

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка запуска приложения: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}