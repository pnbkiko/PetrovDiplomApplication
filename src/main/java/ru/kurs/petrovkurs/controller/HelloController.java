package ru.kurs.petrovkurs.controller;
import java.io.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.awt.Desktop;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import ru.kurs.petrovkurs.model.MaintenanceSchedule;
import ru.kurs.petrovkurs.service.MaintenanceScheduleService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.stage.FileChooser;
import java.awt.Desktop;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class HelloController implements Initializable {
    private boolean notificationsViewed = false;
    private Font customFont;
    @FXML
    private VBox notificationPane;
    @FXML
    private ScrollPane notificationScroll;
    private MaintenanceScheduleService maintenanceScheduleService = new MaintenanceScheduleService();
    @FXML
    private StackPane contentStack;
    @FXML
    private Button btnMachines, btnActs, btnSchedule, btnTypes, btnClose;
    private String upcomingMaintenanceMessage;

    // Добавленные поля для счетчика
    @FXML
    private Label notificationCountLabel;

    // Добавляем ссылку на метку общего количества уведомлений
    @FXML
    private Label totalNotificationsLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Menu calendarMenu = new Menu("Календарь");
        MenuItem calendarMenuItem = new MenuItem("Открыть календарь ТО");
        calendarMenuItem.setOnAction(e -> openCalendarWindow());
        calendarMenu.getItems().add(calendarMenuItem);
        try {
            // Укажите путь к вашему файлу шрифта
            String fontPath = getClass().getResource("/fonts/arial.ttf").toExternalForm();
            BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            customFont = new Font(baseFont, 12, Font.NORMAL);

        } catch (IOException | com.itextpdf.text.DocumentException e) {
            e.printStackTrace();
            // В случае ошибки можно оставить шрифт по умолчанию
            customFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        }

        // Инициализация счетчика уведомлений
        updateNotificationCount();

        prepareUpcomingMaintenanceMessage();

        PauseTransition delay = new PauseTransition(Duration.seconds(5));

        delay.setOnFinished(event -> {
            Platform.runLater(() -> notifyUpcomingMaintenance());
        });
        delay.play();
        handleMachines();

        detailsPane.setPrefWidth(300);

        // Обновляем счетчик каждые 30 секунд
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            updateNotificationCount();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    public List<LocalDate> createDateRangeForNotifications(LocalDate startDate) {
        return Arrays.asList(
                startDate,
                startDate.plusDays(1),
                startDate.plusDays(2)
        );
    }
    // Метод для обновления счетчика уведомлений
    private void updateNotificationCount() {


        List<LocalDate> targetDates = createDateRangeForNotifications(LocalDate.now());

        List<MaintenanceSchedule> allSchedules = maintenanceScheduleService.findAll();

        long count = allSchedules.stream()
                .filter(ms -> ms.getNextDue() != null && targetDates.contains(ms.getNextDue()))
                .count();

        // Обновляем счетчик только если уведомления не просмотрены
        if (!notificationsViewed) {
            Platform.runLater(() -> {
                if (count > 0) {
                    notificationCountLabel.setText(String.valueOf(count));
                    notificationCountLabel.setVisible(true);
                } else {
                    notificationCountLabel.setVisible(false);
                }
            });
        }
    }

    // Метод для очистки счетчика
    private void clearNotificationCount() {
        Platform.runLater(() -> {
            notificationCountLabel.setText("0");
            notificationCountLabel.setVisible(false);
        });
    }

    private void prepareUpcomingMaintenanceMessage() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        List<LocalDate> targetDates = Arrays.asList(today, tomorrow, dayAfterTomorrow);

        List<MaintenanceSchedule> allSchedules = maintenanceScheduleService.findAll();

        // Группируем по дате следующего ТО
        Map<LocalDate, List<MaintenanceSchedule>> schedulesByDate = allSchedules.stream()
                .filter(ms -> ms.getNextDue() != null && targetDates.contains(ms.getNextDue()))
                .collect(Collectors.groupingBy(MaintenanceSchedule::getNextDue));

        // Обновляем счетчик только если уведомления не просмотрены
        long notificationCount = schedulesByDate.values().stream()
                .mapToLong(List::size)
                .sum();

        // Обновляем счетчик только если уведомления не просмотрены
        if (!notificationsViewed) {
            Platform.runLater(() -> {
                if (notificationCount > 0) {
                    notificationCountLabel.setText(String.valueOf(notificationCount));
                    notificationCountLabel.setVisible(true);
                } else {
                    notificationCountLabel.setVisible(false);
                }
            });
        }

        StringBuilder messageBuilder = new StringBuilder();

        // Обработка для каждого дня отдельно
        if (schedulesByDate.containsKey(today)) {
            messageBuilder.append("ТО на сегодня:\n");
            for (MaintenanceSchedule ms : schedulesByDate.get(today)) {
                messageBuilder.append("Модель: ").append(ms.getMachineModel().get()).append("\n");
                messageBuilder.append("Тип ТО: ").append(ms.getTypeNames()).append("\n");
                messageBuilder.append("Следующее ТО: ").append(ms.getNextDue().toString()).append("\n\n");
            }
            messageBuilder.append("\n");
        }

        if (schedulesByDate.containsKey(tomorrow)) {
            messageBuilder.append("ТО на завтра:\n");
            for (MaintenanceSchedule ms : schedulesByDate.get(tomorrow)) {
                messageBuilder.append("Модель: ").append(ms.getMachineModel().get()).append("\n");
                messageBuilder.append("Тип ТО: ").append(ms.getTypeNames()).append("\n");
                messageBuilder.append("Следующее ТО: ").append(ms.getNextDue().toString()).append("\n\n");
            }
            messageBuilder.append("\n");
        }

        if (schedulesByDate.containsKey(dayAfterTomorrow)) {
            messageBuilder.append("ТО на послезавтра:\n");
            for (MaintenanceSchedule ms : schedulesByDate.get(dayAfterTomorrow)) {
                messageBuilder.append("Модель: ").append(ms.getMachineModel().get()).append("\n");
                messageBuilder.append("Тип ТО: ").append(ms.getTypeNames()).append("\n");
                messageBuilder.append("Следующее ТО: ").append(ms.getNextDue().toString()).append("\n\n");
            }
        }

        if (messageBuilder.length() == 0) {
            upcomingMaintenanceMessage = "Нет запланированных ТО на сегодня, завтра или послезавтра.";
        } else {
            upcomingMaintenanceMessage = messageBuilder.toString();
        }
    }

    // Метод для подсчета общего количества уведомлений
    private long countTotalNotifications() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        List<LocalDate> targetDates = Arrays.asList(today, tomorrow, dayAfterTomorrow);

        List<MaintenanceSchedule> allSchedules = maintenanceScheduleService.findAll();

        return allSchedules.stream()
                .filter(ms -> ms.getNextDue() != null && targetDates.contains(ms.getNextDue()))
                .count();
    }

    private void resetButtonStyles() {
        // Удаляем стиль у всех кнопок, чтобы только одна была выделена
        btnMachines.getStyleClass().remove("button-selected");
        btnActs.getStyleClass().remove("button-selected");
        btnSchedule.getStyleClass().remove("button-selected");
        btnTypes.getStyleClass().remove("button-selected");
        btnClose.getStyleClass().remove("button-selected");
    }

    @FXML
    private void handleMachines() {
        loadPane("/ru/kurs/petrovkurs/machines-table-view.fxml");
        resetButtonStyles();
        btnMachines.getStyleClass().add("button-selected");
    }

    @FXML
    private void handleActs() {
        loadPane("/ru/kurs/petrovkurs/maintenance-acts-table-view.fxml");
        resetButtonStyles();
        btnActs.getStyleClass().add("button-selected");
    }

    @FXML
    private void handleSchedule() {
        loadPane("/ru/kurs/petrovkurs/maintenance-schedule-table-view.fxml");
        resetButtonStyles();
        btnSchedule.getStyleClass().add("button-selected");
    }

    @FXML
    private void handleTypes() {
        loadPane("/ru/kurs/petrovkurs/maintenance-types-table-view.fxml");
        resetButtonStyles();
        btnTypes.getStyleClass().add("button-selected");
    }

    public void loadPane(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent pane = loader.load();
            contentStack.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyUpcomingMaintenance() {
        // Если уведомления уже просмотрены, не показываем всплывающее окно и не обновляем счетчик
        if (notificationsViewed) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        // Создаём списки для каждого дня
        List<LocalDate> targetDates = Arrays.asList(today, tomorrow, dayAfterTomorrow);

        List<MaintenanceSchedule> allSchedules = maintenanceScheduleService.findAll();

        // Группируем по дате следующего ТО
        Map<LocalDate, List<MaintenanceSchedule>> schedulesByDate = allSchedules.stream()
                .filter(ms -> ms.getNextDue() != null && targetDates.contains(ms.getNextDue()))
                .collect(Collectors.groupingBy(MaintenanceSchedule::getNextDue));

        // Обновляем счетчик только если уведомления не просмотрены
        long notificationCount = schedulesByDate.values().stream()
                .mapToLong(List::size)
                .sum();

        Platform.runLater(() -> {
            if (notificationCount > 0) {
                notificationCountLabel.setText(String.valueOf(notificationCount));
                notificationCountLabel.setVisible(true);
                // Анимация счетчика
                animateNotificationCounter();
            } else {
                notificationCountLabel.setVisible(false);
            }
        });

        StringBuilder message = new StringBuilder();
        int totalItems = 0;

        // Форматирование даты
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM", new Locale("ru"));
        DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ru"));

        // Обработка для каждого дня отдельно
        if (schedulesByDate.containsKey(today)) {
            int count = schedulesByDate.get(today).size();
            message.append("📅 СЕГОДНЯ (").append(today.format(dateFormatter)).append(") - ").append(count).append(" ТО\n");
            message.append("══════════════════════════\n");
            for (MaintenanceSchedule ms : schedulesByDate.get(today)) {
                message.append("🔧 ").append(ms.getMachineModel().get()).append("\n");
                message.append("   📋 ").append(ms.getTypeNames()).append("\n\n");
                totalItems++;
            }
            message.append("\n");
        }

        if (schedulesByDate.containsKey(tomorrow)) {
            int count = schedulesByDate.get(tomorrow).size();
            message.append("📅 ЗАВТРА (").append(tomorrow.format(dateFormatter)).append(") - ").append(count).append(" ТО\n");
            message.append("══════════════════════════\n");
            for (MaintenanceSchedule ms : schedulesByDate.get(tomorrow)) {
                message.append("🔧 ").append(ms.getMachineModel().get()).append("\n");
                message.append("   📋 ").append(ms.getTypeNames()).append("\n\n");
                totalItems++;
            }
            message.append("\n");
        }

        if (schedulesByDate.containsKey(dayAfterTomorrow)) {
            int count = schedulesByDate.get(dayAfterTomorrow).size();
            message.append("📅 ПОСЛЕЗАВТРА (").append(dayAfterTomorrow.format(dateFormatter)).append(") - ").append(count).append(" ТО\n");
            message.append("══════════════════════════\n");
            for (MaintenanceSchedule ms : schedulesByDate.get(dayAfterTomorrow)) {
                message.append("🔧 ").append(ms.getMachineModel().get()).append("\n");
                message.append("   📋 ").append(ms.getTypeNames()).append("\n\n");
                totalItems++;
            }
        }

        if (message.length() == 0) {
            // Компактное уведомление для случая "нет ТО"
            showCompactNotification(
                    "✅ Все ТО выполнены вовремя",
                    "На ближайшие 3 дня запланированных ТО не найдено.\n" +
                            "Дата: " + today.format(dateFormatter) + " " + today.getYear() + " г.",
                    "#4CAF50" // Зеленый
            );
        } else {
            String title = "🔔 Предстоящие ТО (" + totalItems + ")";
            showDetailedNotification(title, message.toString(),
                    schedulesByDate.containsKey(today) ? "#FF9800" : "#2196F3", // Оранжевый если есть сегодня, иначе синий
                    totalItems);
        }
    }
    private void animateNotificationCounter() {
        // Анимация пульсации счетчика
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), notificationCountLabel);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.3);
        scale.setToY(1.3);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);

        RotateTransition rotate = new RotateTransition(Duration.millis(150), notificationCountLabel);
        rotate.setFromAngle(0);
        rotate.setToAngle(10);
        rotate.setCycleCount(4);
        rotate.setAutoReverse(true);

        ParallelTransition parallel = new ParallelTransition(scale, rotate);
        parallel.play();
    }
    private void showCompactNotification(String title, String message, String color) {
        Stage notificationStage = new Stage();

        // Определяем цвет текста для лучшей читаемости
        String textColor = getContrastColor(color);

        // Основной контейнер (компактный)
        VBox root = new VBox();
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f9fa); " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2); " +
                        "-fx-padding: 0;"
        );
        root.setOpacity(0.0);
        root.setMaxWidth(300);
        root.setMaxHeight(180);

        // Заголовок
        HBox header = new HBox();
        header.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-background-radius: 10 10 0 0; " +
                        "-fx-padding: 12 15; " +
                        "-fx-alignment: center-left;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Кнопка закрытия
        Button closeButton = new Button("✕");
        closeButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 12; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 0; " +
                        "-fx-min-width: 20; " +
                        "-fx-min-height: 20; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> notificationStage.close());

        header.getChildren().addAll(titleLabel, spacer, closeButton);

        // Контент (компактный)
        VBox content = new VBox();
        content.setStyle("-fx-padding: 15; -fx-spacing: 8; -fx-alignment: center;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle(
                "-fx-text-fill: #444444; " +
                        "-fx-font-size: 12; " +
                        "-fx-font-family: 'Segoe UI', Arial, sans-serif; " +
                        "-fx-text-alignment: center; " +
                        "-fx-wrap-text: true;"
        );
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(250);

        // Иконка
        Label iconLabel = new Label("✅");
        iconLabel.setStyle("-fx-font-size: 32; -fx-padding: 0 0 5 0;");

        content.getChildren().addAll(iconLabel, messageLabel);
        root.getChildren().addAll(header, content);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        notificationStage.setScene(scene);
        notificationStage.setAlwaysOnTop(true);
        notificationStage.initStyle(StageStyle.TRANSPARENT);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        notificationStage.setX(bounds.getMaxX() - 320);
        notificationStage.setY(bounds.getMaxY() - 200);

        notificationStage.show();

        // Анимация появления
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), root);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        ParallelTransition entrance = new ParallelTransition(fadeIn, scaleIn);
        entrance.play();

        // Автоматическое закрытие через 5 секунд
        PauseTransition autoClose = new PauseTransition(Duration.seconds(5));
        autoClose.setOnFinished(e -> closeNotification(notificationStage, root));
        autoClose.play();

        // Останавливаем авто-закрытие при наведении
        root.setOnMouseEntered(e -> autoClose.stop());
        root.setOnMouseExited(e -> autoClose.play());
    }

    private void showDetailedNotification(String title, String message, String color, int itemCount) {
        Stage notificationStage = new Stage();

        // УВЕЛИЧИВАЕМ ВЫСОТУ УВЕДОМЛЕНИЯ
        int height = Math.min(250+ (itemCount * 30), 550);

        // Основной контейнер
        VBox root = new VBox();
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f9fa); " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); " +
                        "-fx-padding: 0;"
        );
        root.setOpacity(0.0);
        root.setMaxWidth(350);
        root.setPrefHeight(height);

        // Заголовок
        HBox header = new HBox();
        header.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-background-radius: 15 15 0 0; " +
                        "-fx-padding: 12 15; " +
                        "-fx-alignment: center-left;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Кнопка закрытия
        Button closeButton = new Button("✕");
        closeButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 0; " +
                        "-fx-min-width: 24; " +
                        "-fx-min-height: 24; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> notificationStage.close());

        header.getChildren().addAll(titleLabel, spacer, closeButton);

        // Контент - используем просто TextArea с настройками
        VBox content = new VBox();
        content.setStyle("-fx-padding: 10; -fx-spacing: 5;");

        TextArea messageArea = new TextArea(message);
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 5; " +
                        "-fx-border-width: 1; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-size: 12; " +
                        "-fx-font-family: 'Segoe UI', Arial, sans-serif; " +
                        "-fx-padding: 5;"
        );

        // Настраиваем высоту TextArea
        messageArea.setPrefHeight(height - 120); // Высота минус заголовок и футер

        // Убираем горизонтальную полосу прокрутки (если нужно)
        messageArea.setPrefWidth(320);
        messageArea.setMaxWidth(320);

        content.getChildren().add(messageArea);

        // Нижняя панель
        HBox footer = new HBox(10);
        footer.setStyle("-fx-padding: 10 15 10 15; -fx-alignment: center-right;");

        Button detailsButton = new Button("Подробнее");
        detailsButton.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 11; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 12; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        detailsButton.setOnAction(e -> {
            notificationStage.close();
            toggleDetailsPane();
        });

        Button laterButton = new Button("Напомнить позже");
        laterButton.setStyle(
                "-fx-background-color: #6c757d; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 11; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 12; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        laterButton.setOnAction(e -> {
            notificationsViewed = true;
            notificationStage.close();
        });

        footer.getChildren().addAll(laterButton, detailsButton);
        root.getChildren().addAll(header, content, footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        notificationStage.setScene(scene);
        notificationStage.setAlwaysOnTop(true);
        notificationStage.initStyle(StageStyle.TRANSPARENT);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        notificationStage.setX(bounds.getMaxX() - 380);
        notificationStage.setY(bounds.getMaxY() - height - 20);

        notificationStage.show();

        // Анимация
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Автоматическое закрытие
        PauseTransition autoClose = new PauseTransition(Duration.seconds(8));
        autoClose.setOnFinished(e -> closeNotification(notificationStage, root));
        autoClose.play();

        root.setOnMouseEntered(e -> autoClose.stop());
        root.setOnMouseExited(e -> autoClose.play());
    }

    // Метод для определения контрастного цвета текста
    private String getContrastColor(String hexColor) {
        if (hexColor == null || hexColor.length() < 7) return "black";

        // Убираем # если есть
        hexColor = hexColor.replace("#", "");

        // Преобразуем hex в RGB
        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);

        // Рассчитываем яркость по формуле
        double brightness = (r * 0.299 + g * 0.587 + b * 0.114) / 255;

        // Если яркость > 0.5, используем черный текст, иначе белый
        return brightness > 0.5 ? "black" : "white";
    }

    private void closeNotification(Stage stage, VBox root) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> stage.close());
        fadeOut.play();
    }

    @FXML
    private VBox detailsPane;
    @FXML
    private ScrollPane detailsScroll;

    @FXML
    private void toggleDetailsPane() {
        boolean isVisible = detailsPane.isVisible();

        if (isVisible) {
            // Если панель уже видима, закрываем её
            hideDetailsPane();
            handleMachines();
        } else {
            // Иначе показываем панель уведомлений
            showDetailsOnRightPane(upcomingMaintenanceMessage);
            detailsPane.setVisible(true);
            detailsPane.setManaged(true);
            resetButtonStyles();
            showDetailsPaneWithAnimation();

            // Очищаем счетчик при нажатии на кнопку
            clearNotificationCount();

            // Устанавливаем флаг, что уведомления просмотрены
            notificationsViewed = true;

            // Добавляем стиль к текущей кнопке
            btnClose.getStyleClass().add("button-selected");

            // Обновляем счетчик общего количества уведомлений
            updateTotalNotificationsCount();
        }
    }

    @FXML
    private void hideDetailsPane() {
        resetButtonStyles();
        hideDetailsPaneWithAnimation();
    }

    private void showDetailsOnRightPane(String message) {
        if (message == null || message.isEmpty()) {
            detailsPane.setVisible(false);
            detailsPane.setManaged(false);
            return;
        }
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);
        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 14; -fx-text-fill: black;");
        VBox content = new VBox(label);
        content.setPadding(new Insets(10));
        content.setSpacing(10);
        detailsScroll.setContent(content);

        // Обновляем счетчик уведомлений в правой панели
        updateTotalNotificationsCount();
    }

    private void updateTotalNotificationsCount() {
        long totalCount = countTotalNotifications();
        if (totalNotificationsLabel != null) {
            totalNotificationsLabel.setText(String.valueOf(totalCount));
        }
    }

    private void showDetailsPaneWithAnimation() {
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);
        double width = detailsPane.getWidth();
        detailsPane.setTranslateX(width);

        TranslateTransition transition = new TranslateTransition(Duration.millis(300), detailsPane);
        transition.setFromX(width);
        transition.setToX(0);
        transition.play();
    }

    private void hideDetailsPaneWithAnimation() {
        double width = detailsPane.getWidth();

        if (width == 0) {
            Platform.runLater(() -> hideDetailsPaneWithAnimation());
            return;
        }

        TranslateTransition transition = new TranslateTransition(Duration.millis(300), detailsPane);
        transition.setFromX(0);
        transition.setToX(width);
        transition.setOnFinished(e -> {
            detailsPane.setVisible(false);
            detailsPane.setManaged(false);
        });
        transition.play();
    }

    private void generatePdfOverdueReport() {
        List<MaintenanceSchedule> overdueList = maintenanceScheduleService.findAll().stream()
                .filter(ms -> ms.getNextDue() != null && ms.getNextDue().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(MaintenanceSchedule::getNextDue))
                .collect(Collectors.toList());

        if (overdueList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Отчет");
            alert.setHeaderText(null);
            alert.setContentText("Нет просроченных технических обслуживаний для отчета.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить PDF отчет");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF файлы", "*.pdf"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        // Устанавливаем имя по умолчанию
        String defaultFileName = "Отчет_просроченные_ТО_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy")) + ".pdf";
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(null);
        if (file == null) {
            return;
        }

        String filePath = file.getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            filePath += ".pdf";
        }

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Загрузка шрифта (используйте ваш способ)
            BaseFont baseFont = loadFont();

            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font subtitleFont = new Font(baseFont, 14, Font.BOLD);
            Font headerFont = new Font(baseFont, 11, Font.BOLD);
            Font contentFont = new Font(baseFont, 10, Font.NORMAL);
            Font warningFont = new Font(baseFont, 10, Font.BOLD, BaseColor.RED);
            Font highlightFont = new Font(baseFont, 10, Font.BOLD, BaseColor.BLUE);
            Font infoFont = new Font(baseFont, 9, Font.ITALIC, BaseColor.DARK_GRAY);

            // === 1. ЗАГОЛОВОК ===
            Paragraph title = new Paragraph("ОТЧЕТ О ПРОСРОЧЕННЫХ ТЕХНИЧЕСКИХ ОБСЛУЖИВАНИЯХ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // === 2. ИНФОРМАЦИЯ О ФОРМИРОВАНИИ ===
            Paragraph reportInfo = new Paragraph(
                    "Дата формирования отчета: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                            "   Время: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                    infoFont
            );
            reportInfo.setAlignment(Element.ALIGN_CENTER);
            reportInfo.setSpacingAfter(15);
            document.add(reportInfo);

            // === 3. СВОДНАЯ СТАТИСТИКА ===
            Paragraph summaryTitle = new Paragraph("СВОДНАЯ ИНФОРМАЦИЯ", subtitleFont);
            summaryTitle.setSpacingAfter(10);
            document.add(summaryTitle);

// Создаем таблицу для статистики
            PdfPTable statsTable = new PdfPTable(1); // Теперь 1 колонка для вертикального расположения
            statsTable.setWidthPercentage(60); // Уменьшаем ширину для лучшего вида
            statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            statsTable.setSpacingBefore(5);
            statsTable.setSpacingAfter(15);

// Средний срок просрочки
            double avgOverdue = overdueList.stream()
                    .filter(ms -> ms.getNextDue() != null)
                    .mapToLong(ms -> ChronoUnit.DAYS.between(ms.getNextDue(), LocalDate.now()))
                    .average()
                    .orElse(0.0);

// Самая длительная просрочка
            OptionalLong maxOverdue = overdueList.stream()
                    .filter(ms -> ms.getNextDue() != null)
                    .mapToLong(ms -> ChronoUnit.DAYS.between(ms.getNextDue(), LocalDate.now()))
                    .max();

// Добавляем метку и значение отдельными строками
// 1. Всего просроченных ТО
            addStatsRowVertical(statsTable, "Всего просроченных ТО:",
                    String.valueOf(overdueList.size()),
                    new Font(baseFont, 10, Font.BOLD),
                    new Font(baseFont, 12, Font.BOLD, new BaseColor(0, 102, 204)));

// 2. Средний срок просрочки
            addStatsRowVertical(statsTable, "Средний срок просрочки:",
                    String.format("%.1f дней", avgOverdue),
                    new Font(baseFont, 10, Font.BOLD),
                    new Font(baseFont, 12, Font.BOLD, new BaseColor(0, 102, 204)));

// 3. Максимальная просрочка
            if (maxOverdue.isPresent()) {
                Font warningValueFont = new Font(baseFont, 12, Font.BOLD, BaseColor.RED);
                addStatsRowVertical(statsTable, "Максимальная просрочка:",
                        maxOverdue.getAsLong() + " дней",
                        new Font(baseFont, 10, Font.BOLD),
                        warningValueFont);
            }

            document.add(statsTable);
            // === 4. ПРЕДУПРЕЖДЕНИЕ ===
            Paragraph warning = new Paragraph("ВНИМАНИЕ! Имеются просроченные технические обслуживания", warningFont);
            warning.setSpacingBefore(10);
            warning.setSpacingAfter(15);
            document.add(warning);

            // === 5. ДЕТАЛЬНАЯ ТАБЛИЦА ===
            Paragraph tableTitle = new Paragraph("ДЕТАЛЬНЫЙ СПИСОК ПРОСРОЧЕННЫХ ТО", subtitleFont);
            tableTitle.setSpacingAfter(10);
            document.add(tableTitle);

            // Основная таблица с расширенными колонками
            PdfPTable mainTable = new PdfPTable(6);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingBefore(5);
            mainTable.setSpacingAfter(20);

            // Заголовки таблицы
            addColoredHeader(mainTable, "№", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Модель станка", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Тип ТО", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Дата следующего ТО", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Дата последнего ТО", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Дней просрочки", headerFont, BaseColor.LIGHT_GRAY);

            // Заполняем таблицу с цветовой индикацией
            int counter = 1;
            LocalDate today = LocalDate.now();

            for (MaintenanceSchedule ms : overdueList) {
                // Номер
                PdfPCell cellNum = createCell(String.valueOf(counter++), contentFont, Element.ALIGN_CENTER);

                // Модель станка
                String machineModel = ms.getMachineModel() != null ? ms.getMachineModel().get() : "Не указано";
                PdfPCell cellModel = createCell(machineModel, contentFont, Element.ALIGN_LEFT);

                // Тип ТО
                String typeName = ms.getTypeNames() != null ? ms.getTypeNames() : "Не указано";
                PdfPCell cellType = createCell(typeName, contentFont, Element.ALIGN_LEFT);

                // Следующее ТО
                String nextDueStr = "Не указано";
                if (ms.getNextDue() != null) {
                    nextDueStr = ms.getNextDue().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                }
                PdfPCell cellNextDue = createCell(nextDueStr, contentFont, Element.ALIGN_CENTER);

                // Последнее ТО
                String lastDoneStr = "Не указано";
                if (ms.getLastDone() != null) {
                    lastDoneStr = ms.getLastDone().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                }
                PdfPCell cellLastDone = createCell(lastDoneStr, contentFont, Element.ALIGN_CENTER);

                // Дней просрочки
                long daysOverdue = 0;
                if (ms.getNextDue() != null) {
                    daysOverdue = ChronoUnit.DAYS.between(ms.getNextDue(), today);
                }
                PdfPCell cellOverdue = createCell(daysOverdue + " дн.", contentFont, Element.ALIGN_CENTER);

                // Добавляем цвет в зависимости от срока просрочки
                if (daysOverdue > 30) {
                    cellOverdue.setBackgroundColor(new BaseColor(255, 200, 200)); // Красный для большой просрочки
                } else if (daysOverdue > 7) {
                    cellOverdue.setBackgroundColor(new BaseColor(255, 255, 200)); // Желтый для средней просрочки
                } else {
                    cellOverdue.setBackgroundColor(new BaseColor(200, 255, 200)); // Зеленый для небольшой просрочки
                }

                mainTable.addCell(cellNum);
                mainTable.addCell(cellModel);
                mainTable.addCell(cellType);
                mainTable.addCell(cellNextDue);
                mainTable.addCell(cellLastDone);
                mainTable.addCell(cellOverdue);
            }

            document.add(mainTable);

            // === 6. АНАЛИЗ ПО ТИПАМ ТО ===
            Paragraph analysisTitle = new Paragraph("АНАЛИЗ ПО ТИПАМ ТЕХНИЧЕСКОГО ОБСЛУЖИВАНИЯ", subtitleFont);
            analysisTitle.setSpacingAfter(10);
            document.add(analysisTitle);

            // Группируем по типам ТО
            Map<String, Long> byType = overdueList.stream()
                    .filter(ms -> ms.getTypeNames() != null)
                    .collect(Collectors.groupingBy(
                            MaintenanceSchedule::getTypeNames,
                            Collectors.counting()
                    ));

            if (!byType.isEmpty()) {
                PdfPTable typeTable = new PdfPTable(2);
                typeTable.setWidthPercentage(60);
                typeTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                typeTable.setSpacingBefore(5);
                typeTable.setSpacingAfter(15);

                addColoredHeader(typeTable, "Тип ТО", headerFont, new BaseColor(230, 230, 255));
                addColoredHeader(typeTable, "Количество", headerFont, new BaseColor(230, 230, 255));

                byType.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .forEach(entry -> {
                            typeTable.addCell(createCell(entry.getKey(), contentFont, Element.ALIGN_LEFT));
                            typeTable.addCell(createCell(String.valueOf(entry.getValue()), contentFont, Element.ALIGN_CENTER));
                        });

                document.add(typeTable);
            }

            // === 7. РЕКОМЕНДАЦИИ ===
            Paragraph recommendationsTitle = new Paragraph("РЕКОМЕНДАЦИИ", subtitleFont);
            recommendationsTitle.setSpacingBefore(10);
            recommendationsTitle.setSpacingAfter(10);
            document.add(recommendationsTitle);

            // Список рекомендаций
            List<String> recommendations = Arrays.asList(
                    "1. Немедленно выполнить просроченные ТО согласно графику",
                    "2. Проверить причины просрочки (отсутствие запчастей, занятость персонала)",
                    "3. Пересмотреть график ТО для предотвращения повторных просрочек",
                    "4. Уведомить ответственных лиц о необходимости срочного выполнения",
                    "5. Внести изменения в систему планирования ТО"
            );

            for (String rec : recommendations) {
                Paragraph recPara = new Paragraph(rec, contentFont);
                recPara.setSpacingBefore(3);
                document.add(recPara);
            }

            // === 8. ВЫВОДЫ ===
            Paragraph conclusionsTitle = new Paragraph("\nВЫВОДЫ", subtitleFont);
            conclusionsTitle.setSpacingBefore(10);
            conclusionsTitle.setSpacingAfter(10);
            document.add(conclusionsTitle);

            String conclusionText = String.format(
                    "На момент формирования отчета обнаружено %d просроченных технических обслуживаний. " +
                            "Средний срок просрочки составляет %.1f дней. " +
                            "Требуется незамедлительное выполнение отложенных работ для обеспечения " +
                            "бесперебойной работы оборудования и соблюдения регламентов технического обслуживания.",
                    overdueList.size(), avgOverdue
            );

            Paragraph conclusion = new Paragraph(conclusionText, contentFont);
            conclusion.setSpacingAfter(15);
            document.add(conclusion);

            // === 9. ИНФОРМАЦИЯ О СИСТЕМЕ ===
            Paragraph systemInfo = new Paragraph(
                    "Отчет сформирован автоматически системой управления ТО",
                    infoFont
            );
            systemInfo.setAlignment(Element.ALIGN_CENTER);
            systemInfo.setSpacingBefore(20);
            document.add(systemInfo);

            document.close();

            // Показываем успешное сообщение
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Отчет успешно создан");
            alert.setHeaderText(null);
            alert.setContentText("Отчет успешно создан и сохранен по пути:\n" + filePath);
            alert.showAndWait();

            // Опционально: открываем PDF
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (Exception e) {
                    // Не открывать, если не поддерживается
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Ошибка при создании PDF отчета: " + e.getMessage());
            alert.showAndWait();
        }
    }
    private void addStatsRowVertical(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        // Ячейка с меткой
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setPadding(5);
        labelCell.setBorderWidth(0); // Без границ
        labelCell.setBackgroundColor(new BaseColor(240, 240, 240)); // Светлый фон
        table.addCell(labelCell);

        // Ячейка со значением
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setPadding(8);
        valueCell.setBorderWidth(0); // Без границ
        valueCell.setBackgroundColor(new BaseColor(220, 237, 200)); // Зеленоватый фон
        valueCell.setMinimumHeight(30);
        table.addCell(valueCell);
    }
    // Метод для загрузки шрифта (используйте ваш)
    private BaseFont loadFont() throws Exception {
        // Массив возможных путей к шрифту
        String[] possiblePaths = {
                "fonts/arial.ttf",                           // корень проекта
                "./fonts/arial.ttf",                         // текущая директория
                "src/main/resources/fonts/arial.ttf",        // Maven структура
                "resources/fonts/arial.ttf",                 // папка resources
                "../fonts/arial.ttf",                        // на уровень выше
                System.getProperty("user.dir") + "/fonts/arial.ttf"  // абсолютный путь
        };

        for (String path : possiblePaths) {
            try {
                File fontFile = new File(path);
                if (fontFile.exists()) {
                    System.out.println("Загружаю шрифт из: " + fontFile.getAbsolutePath());
                    return BaseFont.createFont(
                            fontFile.getAbsolutePath(),
                            BaseFont.IDENTITY_H,
                            BaseFont.EMBEDDED
                    );
                }
            } catch (Exception e) {
                // Пробуем следующий путь
                continue;
            }
        }

        // Если не нашли шрифт, используем запасной вариант
        System.err.println("Шрифт arial.ttf не найден. Использую запасной вариант.");
        return getFallbackFont();
    }

    private BaseFont getFallbackFont() throws Exception {
        try {
            return BaseFont.createFont("Times-Roman", "CP1251", BaseFont.EMBEDDED);
        } catch (Exception e) {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
        }
    }

    // Вспомогательные методы
    private void addColoredHeader(PdfPTable table, String text, Font font, BaseColor color) {
        PdfPCell header = new PdfPCell(new Phrase(text, font));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.setPadding(8);
        header.setMinimumHeight(25);
        header.setBackgroundColor(color);
        table.addCell(header);
    }



    private PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setMinimumHeight(20);
        return cell;
    }





    // Класс для колонтитулов


    @FXML
    private void btnPdfAction() {
        generatePdfOverdueReport();
    }

    @FXML
    private void openCalendarWindow() {
        try {
            Stage calendarStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/kurs/petrovkurs/maintenance-calendar-view.fxml"));
            Scene scene = new Scene(loader.load());
            Image icon = new Image(getClass().getResource("/images/icon_main.png").toExternalForm());
            calendarStage.getIcons().add(icon);

            calendarStage.setTitle("Календарь технического обслуживания");
            calendarStage.setScene(scene);
            calendarStage.setWidth(1920);
            calendarStage.setHeight(1080);
            calendarStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть календарь");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }


}