package ru.kurs.petrovkurs.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.kurs.petrovkurs.service.AuthService;
import ru.kurs.petrovkurs.util.Manager;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private AuthService authService = AuthService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());

        // Для тестирования - заполним поля (можно убрать потом)
        // usernameField.setText("admin");
        // passwordField.setText("123456");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        System.out.println("Login attempt - Username: '" + username + "', Password length: " + (password != null ? password.length() : 0));

        if (username.isEmpty()) {
            showError("Введите логин");
            return;
        }

        if (password.isEmpty()) {
            showError("Введите пароль");
            return;
        }

        if (authService.login(username, password)) {
            System.out.println("Login successful!");
            showSuccessAndOpenMain();
        } else {
            System.out.println("Login failed!");
            showError("Неверный логин или пароль");
        }
    }

    @FXML
    private void handleHelp() {
        HelpController.showHelpWindow();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        // Очистка через 3 секунды
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setVisible(false);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showSuccessAndOpenMain() {
        try {
            Stage loginStage = (Stage) usernameField.getScene().getWindow();

            Stage mainStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/kurs/petrovkurs/main-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/ru/kurs/petrovkurs/main.css").toExternalForm());

            mainStage.setTitle("ТО Машин - Система управления техническим обслуживанием");
            mainStage.setScene(scene);
            mainStage.setMinWidth(1200);
            mainStage.setMinHeight(700);
            mainStage.setWidth(1600);
            mainStage.setHeight(900);

            try {
                Image icon = new Image(getClass().getResource("/images/icon_main.png").toExternalForm());
                mainStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Иконка не найдена");
            }

            mainStage.centerOnScreen();
            mainStage.show();

            Manager.mainStage = mainStage;
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка при открытии главного окна: " + e.getMessage());
        }
    }
}