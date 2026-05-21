package ru.kurs.petrovkurs.controller;

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
import ru.kurs.petrovkurs.service.AuthService;
import ru.kurs.petrovkurs.service.MaintenanceScheduleService;
import ru.kurs.petrovkurs.util.Manager;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    @FXML
    private StackPane contentStack;

    @FXML
    private Button btnMachines, btnActs, btnSchedule, btnTypes, btnPdf, btnClose;

    @FXML
    private VBox detailsPane;

    @FXML
    private ScrollPane detailsScroll;

    @FXML
    private Label notificationCountLabel;

    @FXML
    private Label totalNotificationsLabel;

    @FXML
    private Label userInfoLabel;

    private AuthService authService = AuthService.getInstance();
    private MaintenanceScheduleService maintenanceScheduleService = new MaintenanceScheduleService();
    private boolean notificationsViewed = false;
    private String upcomingMaintenanceMessage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUserInfo();
        setupPermissions();
        handleMachines();
        checkUpcomingMaintenance();
    }

    public void setUserInfo() {
        if (userInfoLabel != null) {
            userInfoLabel.setText(authService.getUserDisplayName());
        }
    }

    private void setupPermissions() {
        AuthService auth = AuthService.getInstance();

        // Для оператора отключаем кнопку PDF отчета
        if (auth.isOperator()) {
            btnPdf.setDisable(true);
            btnPdf.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 14; -fx-padding: 12 20; -fx-alignment: CENTER_LEFT; -fx-border-width: 0 0 1 0; -fx-border-color: #4a6572;");
            Tooltip tooltip = new Tooltip("У вас нет прав для создания отчетов");
            Tooltip.install(btnPdf, tooltip);
        }
    }

    private void checkUpcomingMaintenance() {
        prepareUpcomingMaintenanceMessage();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            Platform.runLater(() -> notifyUpcomingMaintenance());
        });
        delay.play();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> updateNotificationCount())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void prepareUpcomingMaintenanceMessage() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);
        List<LocalDate> targetDates = Arrays.asList(today, tomorrow, dayAfterTomorrow);

        List<MaintenanceSchedule> allSchedules = maintenanceScheduleService.findAll();
        Map<LocalDate, List<MaintenanceSchedule>> schedulesByDate = allSchedules.stream()
                .filter(ms -> ms.getNextDue() != null && targetDates.contains(ms.getNextDue()))
                .collect(Collectors.groupingBy(MaintenanceSchedule::getNextDue));

        long notificationCount = schedulesByDate.values().stream().mapToLong(List::size).sum();

        if (!notificationsViewed) {
            Platform.runLater(() -> {
                if (notificationCount > 0) {
                    notificationCountLabel.setText(String.valueOf(notificationCount));
                    notificationCountLabel.setVisible(true);
                    animateNotificationCounter();
                } else {
                    notificationCountLabel.setVisible(false);
                }
            });
        }

        StringBuilder messageBuilder = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("ru"));

        if (schedulesByDate.containsKey(today)) {
            messageBuilder.append("📅 СЕГОДНЯ (").append(today.format(dateFormatter)).append(")\n");
            messageBuilder.append("══════════════════════════\n");
            for (MaintenanceSchedule ms : schedulesByDate.get(today)) {
                messageBuilder.append("🔧 ").append(ms.getMachineModel().get()).append("\n");
                messageBuilder.append("   📋 ").append(ms.getTypeNames()).append("\n\n");
            }
        }

        if (schedulesByDate.containsKey(tomorrow)) {
            messageBuilder.append("📅 ЗАВТРА (").append(tomorrow.format(dateFormatter)).append(")\n");
            messageBuilder.append("══════════════════════════\n");
            for (MaintenanceSchedule ms : schedulesByDate.get(tomorrow)) {
                messageBuilder.append("🔧 ").append(ms.getMachineModel().get()).append("\n");
                messageBuilder.append("   📋 ").append(ms.getTypeNames()).append("\n\n");
            }
        }

        if (schedulesByDate.containsKey(dayAfterTomorrow)) {
            messageBuilder.append("📅 ПОСЛЕЗАВТРА (").append(dayAfterTomorrow.format(dateFormatter)).append(")\n");
            messageBuilder.append("══════════════════════════\n");
            for (MaintenanceSchedule ms : schedulesByDate.get(dayAfterTomorrow)) {
                messageBuilder.append("🔧 ").append(ms.getMachineModel().get()).append("\n");
                messageBuilder.append("   📋 ").append(ms.getTypeNames()).append("\n\n");
            }
        }

        if (messageBuilder.length() == 0) {
            upcomingMaintenanceMessage = "✅ Нет запланированных ТО на ближайшие 3 дня.";
        } else {
            upcomingMaintenanceMessage = messageBuilder.toString();
        }
    }

    private void updateNotificationCount() {
        if (notificationsViewed) return;

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);
        List<LocalDate> targetDates = Arrays.asList(today, tomorrow, dayAfterTomorrow);

        List<MaintenanceSchedule> allSchedules = maintenanceScheduleService.findAll();

        long count = allSchedules.stream()
                .filter(ms -> ms.getNextDue() != null && targetDates.contains(ms.getNextDue()))
                .count();

        Platform.runLater(() -> {
            if (count > 0 && !notificationsViewed) {
                notificationCountLabel.setText(String.valueOf(count));
                notificationCountLabel.setVisible(true);
            } else if (!notificationsViewed) {
                notificationCountLabel.setVisible(false);
            }
        });
    }

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

    private void animateNotificationCounter() {
        if (notificationCountLabel == null) return;

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

    private void notifyUpcomingMaintenance() {
        if (notificationsViewed) return;

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);
        List<LocalDate> targetDates = Arrays.asList(today, tomorrow, dayAfterTomorrow);

        List<MaintenanceSchedule> allSchedules = maintenanceScheduleService.findAll();
        Map<LocalDate, List<MaintenanceSchedule>> schedulesByDate = allSchedules.stream()
                .filter(ms -> ms.getNextDue() != null && targetDates.contains(ms.getNextDue()))
                .collect(Collectors.groupingBy(MaintenanceSchedule::getNextDue));

        long notificationCount = schedulesByDate.values().stream().mapToLong(List::size).sum();

        if (notificationCount == 0) {
            showCompactNotification("✅ Все ТО выполнены вовремя",
                    "На ближайшие 3 дня запланированных ТО не найдено.", "#4CAF50");
        } else {
            showDetailedNotification("🔔 Предстоящие ТО (" + notificationCount + ")",
                    upcomingMaintenanceMessage, schedulesByDate.containsKey(today) ? "#FF9800" : "#2196F3",
                    (int) notificationCount);
        }
    }

    private void showCompactNotification(String title, String message, String color) {
        Stage notificationStage = new Stage();
        String textColor = getContrastColor(color);

        VBox root = new VBox();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f9fa); " +
                "-fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1; " +
                "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2); -fx-padding: 0;");
        root.setOpacity(0.0);
        root.setMaxWidth(300);

        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10 10 0 0; -fx-padding: 12 15; -fx-alignment: center-left;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + "; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 0; -fx-min-width: 20; -fx-min-height: 20; -fx-background-radius: 10; -fx-cursor: hand;");
        closeButton.setOnAction(e -> notificationStage.close());

        header.getChildren().addAll(titleLabel, spacer, closeButton);

        VBox content = new VBox();
        content.setStyle("-fx-padding: 15; -fx-spacing: 8; -fx-alignment: center;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 12; -fx-text-alignment: center; -fx-wrap-text: true;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(250);

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

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        PauseTransition autoClose = new PauseTransition(Duration.seconds(5));
        autoClose.setOnFinished(e -> closeNotification(notificationStage, root));
        autoClose.play();

        root.setOnMouseEntered(e -> autoClose.stop());
        root.setOnMouseExited(e -> autoClose.play());
    }

    private void showDetailedNotification(String title, String message, String color, int itemCount) {
        Stage notificationStage = new Stage();
        int height = Math.min(350 + (itemCount * 25), 550);

        VBox root = new VBox();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f9fa); -fx-background-radius: 15; " +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); -fx-padding: 0;");
        root.setOpacity(0.0);
        root.setMaxWidth(380);
        root.setPrefHeight(height);

        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15 15 0 0; -fx-padding: 12 15; -fx-alignment: center-left;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 0; -fx-min-width: 24; -fx-min-height: 24; -fx-background-radius: 12; -fx-cursor: hand;");
        closeButton.setOnAction(e -> notificationStage.close());

        header.getChildren().addAll(titleLabel, spacer, closeButton);

        VBox content = new VBox();
        content.setStyle("-fx-padding: 10; -fx-spacing: 5;");

        TextArea messageArea = new TextArea(message);
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-background-color: transparent; -fx-border-color: #e0e0e0; -fx-border-radius: 5; " +
                "-fx-border-width: 1; -fx-text-fill: #333333; -fx-font-size: 12; -fx-padding: 5;");
        messageArea.setPrefHeight(height - 130);
        messageArea.setPrefWidth(340);
        messageArea.setMaxWidth(340);

        content.getChildren().add(messageArea);

        HBox footer = new HBox(10);
        footer.setStyle("-fx-padding: 10 15 10 15; -fx-alignment: center-right;");

        Button detailsButton = new Button("Подробнее");
        detailsButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 11; " +
                "-fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 5; -fx-cursor: hand;");
        detailsButton.setOnAction(e -> {
            notificationStage.close();
            toggleDetailsPane();
        });

        Button laterButton = new Button("Напомнить позже");
        laterButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 11; " +
                "-fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 5; -fx-cursor: hand;");
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
        notificationStage.setX(bounds.getMaxX() - 400);
        notificationStage.setY(bounds.getMaxY() - height - 20);
        notificationStage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        PauseTransition autoClose = new PauseTransition(Duration.seconds(10));
        autoClose.setOnFinished(e -> closeNotification(notificationStage, root));
        autoClose.play();

        root.setOnMouseEntered(e -> autoClose.stop());
        root.setOnMouseExited(e -> autoClose.play());
    }

    private String getContrastColor(String hexColor) {
        if (hexColor == null || hexColor.length() < 7) return "black";
        hexColor = hexColor.replace("#", "");
        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);
        double brightness = (r * 0.299 + g * 0.587 + b * 0.114) / 255;
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
    private void toggleDetailsPane() {
        boolean isVisible = detailsPane.isVisible();

        if (isVisible) {
            hideDetailsPane();
            handleMachines();
        } else {
            showDetailsOnRightPane(upcomingMaintenanceMessage);
            detailsPane.setVisible(true);
            detailsPane.setManaged(true);
            resetButtonStyles();
            showDetailsPaneWithAnimation();
            clearNotificationCount();
            notificationsViewed = true;
            btnClose.getStyleClass().add("button-selected");
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
        updateTotalNotificationsCount();
    }

    private void updateTotalNotificationsCount() {
        long totalCount = countTotalNotifications();
        if (totalNotificationsLabel != null) {
            totalNotificationsLabel.setText(String.valueOf(totalCount));
        }
    }

    private void clearNotificationCount() {
        Platform.runLater(() -> {
            if (notificationCountLabel != null) {
                notificationCountLabel.setText("0");
                notificationCountLabel.setVisible(false);
            }
        });
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

    private void resetButtonStyles() {
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

    @FXML
    private void btnPdfAction() {
        if (!authService.canView()) {
            showAccessDenied();
            return;
        }
        generatePdfOverdueReport();
    }

    // ==================== PDF ОТЧЕТ ====================

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
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Загрузка шрифта Arial
            BaseFont baseFont = loadArialFont();

            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font subtitleFont = new Font(baseFont, 14, Font.BOLD);
            Font headerFont = new Font(baseFont, 11, Font.BOLD);
            Font contentFont = new Font(baseFont, 10, Font.NORMAL);
            Font warningFont = new Font(baseFont, 10, Font.BOLD, BaseColor.RED);
            Font infoFont = new Font(baseFont, 9, Font.ITALIC, BaseColor.DARK_GRAY);

            // ==================== СТРАНИЦА 1: ОСНОВНАЯ ИНФОРМАЦИЯ ====================

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

            PdfPTable statsTable = new PdfPTable(1);
            statsTable.setWidthPercentage(60);
            statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            statsTable.setSpacingBefore(5);
            statsTable.setSpacingAfter(15);

            double avgOverdue = overdueList.stream()
                    .filter(ms -> ms.getNextDue() != null)
                    .mapToLong(ms -> ChronoUnit.DAYS.between(ms.getNextDue(), LocalDate.now()))
                    .average()
                    .orElse(0.0);

            OptionalLong maxOverdue = overdueList.stream()
                    .filter(ms -> ms.getNextDue() != null)
                    .mapToLong(ms -> ChronoUnit.DAYS.between(ms.getNextDue(), LocalDate.now()))
                    .max();

            addStatsRowVertical(statsTable, "Всего просроченных ТО:",
                    String.valueOf(overdueList.size()),
                    new Font(baseFont, 10, Font.BOLD),
                    new Font(baseFont, 12, Font.BOLD, new BaseColor(0, 102, 204)));

            addStatsRowVertical(statsTable, "Средний срок просрочки:",
                    String.format("%.1f дней", avgOverdue),
                    new Font(baseFont, 10, Font.BOLD),
                    new Font(baseFont, 12, Font.BOLD, new BaseColor(0, 102, 204)));

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

            PdfPTable mainTable = new PdfPTable(6);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingBefore(5);
            mainTable.setSpacingAfter(20);

            addColoredHeader(mainTable, "№", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Модель станка", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Тип ТО", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Дата следующего ТО", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Дата последнего ТО", headerFont, BaseColor.LIGHT_GRAY);
            addColoredHeader(mainTable, "Дней просрочки", headerFont, BaseColor.LIGHT_GRAY);

            int counter = 1;
            LocalDate today = LocalDate.now();

            for (MaintenanceSchedule ms : overdueList) {
                PdfPCell cellNum = createCell(String.valueOf(counter++), contentFont, Element.ALIGN_CENTER);

                String machineModel = ms.getMachineModel() != null ? ms.getMachineModel().get() : "Не указано";
                PdfPCell cellModel = createCell(machineModel, contentFont, Element.ALIGN_LEFT);

                String typeName = ms.getTypeNames() != null ? ms.getTypeNames() : "Не указано";
                PdfPCell cellType = createCell(typeName, contentFont, Element.ALIGN_LEFT);

                String nextDueStr = "Не указано";
                if (ms.getNextDue() != null) {
                    nextDueStr = ms.getNextDue().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                }
                PdfPCell cellNextDue = createCell(nextDueStr, contentFont, Element.ALIGN_CENTER);

                String lastDoneStr = "Не указано";
                if (ms.getLastDone() != null) {
                    lastDoneStr = ms.getLastDone().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                }
                PdfPCell cellLastDone = createCell(lastDoneStr, contentFont, Element.ALIGN_CENTER);

                long daysOverdue = 0;
                if (ms.getNextDue() != null) {
                    daysOverdue = ChronoUnit.DAYS.between(ms.getNextDue(), today);
                }
                PdfPCell cellOverdue = createCell(daysOverdue + " дн.", contentFont, Element.ALIGN_CENTER);

                if (daysOverdue > 30) {
                    cellOverdue.setBackgroundColor(new BaseColor(255, 200, 200));
                } else if (daysOverdue > 7) {
                    cellOverdue.setBackgroundColor(new BaseColor(255, 255, 200));
                } else {
                    cellOverdue.setBackgroundColor(new BaseColor(200, 255, 200));
                }

                mainTable.addCell(cellNum);
                mainTable.addCell(cellModel);
                mainTable.addCell(cellType);
                mainTable.addCell(cellNextDue);
                mainTable.addCell(cellLastDone);
                mainTable.addCell(cellOverdue);
            }

            document.add(mainTable);

            // ==================== СТРАНИЦА 2: АНАЛИЗ И РЕКОМЕНДАЦИИ ====================
            document.newPage();

            // === 6. АНАЛИЗ ПО ТИПАМ ТО ===
            Paragraph analysisTitle = new Paragraph("АНАЛИЗ ПО ТИПАМ ТЕХНИЧЕСКОГО ОБСЛУЖИВАНИЯ", subtitleFont);
            analysisTitle.setAlignment(Element.ALIGN_CENTER);
            analysisTitle.setSpacingAfter(15);
            document.add(analysisTitle);

            Map<String, Long> byType = overdueList.stream()
                    .filter(ms -> ms.getTypeNames() != null)
                    .collect(Collectors.groupingBy(
                            MaintenanceSchedule::getTypeNames,
                            Collectors.counting()
                    ));

            if (!byType.isEmpty()) {
                PdfPTable typeTable = new PdfPTable(2);
                typeTable.setWidthPercentage(70);
                typeTable.setHorizontalAlignment(Element.ALIGN_CENTER);
                typeTable.setSpacingBefore(10);
                typeTable.setSpacingAfter(20);

                addColoredHeader(typeTable, "Тип ТО", headerFont, new BaseColor(52, 152, 219));
                addColoredHeader(typeTable, "Количество", headerFont, new BaseColor(52, 152, 219));

                byType.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .forEach(entry -> {
                            typeTable.addCell(createCell(entry.getKey(), contentFont, Element.ALIGN_LEFT));
                            typeTable.addCell(createCell(String.valueOf(entry.getValue()), contentFont, Element.ALIGN_CENTER));
                        });

                document.add(typeTable);
            }

            // === 7. АНАЛИЗ ПО СТАНКАМ ===
            Paragraph machinesTitle = new Paragraph("АНАЛИЗ ПО СТАНКАМ", subtitleFont);
            machinesTitle.setAlignment(Element.ALIGN_CENTER);
            machinesTitle.setSpacingAfter(15);
            document.add(machinesTitle);

            Map<String, Long> byMachine = overdueList.stream()
                    .filter(ms -> ms.getMachineModel() != null && ms.getMachineModel().get() != null)
                    .collect(Collectors.groupingBy(
                            ms -> ms.getMachineModel().get(),
                            Collectors.counting()
                    ));

            if (!byMachine.isEmpty()) {
                PdfPTable machineTable = new PdfPTable(2);
                machineTable.setWidthPercentage(70);
                machineTable.setHorizontalAlignment(Element.ALIGN_CENTER);
                machineTable.setSpacingBefore(10);
                machineTable.setSpacingAfter(20);

                addColoredHeader(machineTable, "Модель станка", headerFont, new BaseColor(46, 204, 113));
                addColoredHeader(machineTable, "Количество просрочек", headerFont, new BaseColor(46, 204, 113));

                byMachine.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .forEach(entry -> {
                            machineTable.addCell(createCell(entry.getKey(), contentFont, Element.ALIGN_LEFT));
                            machineTable.addCell(createCell(String.valueOf(entry.getValue()), contentFont, Element.ALIGN_CENTER));
                        });

                document.add(machineTable);
            }

            // === 8. ГРАФИК ПРОСРОЧЕК ПО ДНЯМ ===
            Paragraph daysTitle = new Paragraph("РАСПРЕДЕЛЕНИЕ ПО СРОКУ ПРОСРОЧКИ", subtitleFont);
            daysTitle.setAlignment(Element.ALIGN_CENTER);
            daysTitle.setSpacingAfter(15);
            document.add(daysTitle);

            long countLess7 = overdueList.stream()
                    .filter(ms -> {
                        long days = ChronoUnit.DAYS.between(ms.getNextDue(), LocalDate.now());
                        return days <= 7;
                    })
                    .count();

            long count7To30 = overdueList.stream()
                    .filter(ms -> {
                        long days = ChronoUnit.DAYS.between(ms.getNextDue(), LocalDate.now());
                        return days > 7 && days <= 30;
                    })
                    .count();

            long countMore30 = overdueList.stream()
                    .filter(ms -> {
                        long days = ChronoUnit.DAYS.between(ms.getNextDue(), LocalDate.now());
                        return days > 30;
                    })
                    .count();

            PdfPTable daysTable = new PdfPTable(2);
            daysTable.setWidthPercentage(60);
            daysTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            daysTable.setSpacingBefore(10);
            daysTable.setSpacingAfter(25);

            addColoredHeader(daysTable, "Срок просрочки", headerFont, new BaseColor(155, 89, 182));
            addColoredHeader(daysTable, "Количество", headerFont, new BaseColor(155, 89, 182));

            daysTable.addCell(createCell("До 7 дней (зеленый)", contentFont, Element.ALIGN_LEFT));
            daysTable.addCell(createCell(String.valueOf(countLess7), contentFont, Element.ALIGN_CENTER));

            daysTable.addCell(createCell("От 7 до 30 дней (желтый)", contentFont, Element.ALIGN_LEFT));
            daysTable.addCell(createCell(String.valueOf(count7To30), contentFont, Element.ALIGN_CENTER));

            daysTable.addCell(createCell("Более 30 дней (красный)", contentFont, Element.ALIGN_LEFT));
            daysTable.addCell(createCell(String.valueOf(countMore30), contentFont, Element.ALIGN_CENTER));

            document.add(daysTable);

            // === 9. РЕКОМЕНДАЦИИ ===
            Paragraph recommendationsTitle = new Paragraph("РЕКОМЕНДАЦИИ", subtitleFont);
            recommendationsTitle.setAlignment(Element.ALIGN_CENTER);
            recommendationsTitle.setSpacingBefore(10);
            recommendationsTitle.setSpacingAfter(15);
            document.add(recommendationsTitle);

            // Рекомендации в виде списка с иконками
            List<String> recommendations = Arrays.asList(
                    "⚠️ Немедленно выполнить просроченные ТО согласно графику",
                    "🔍 Проверить причины просрочки (отсутствие запчастей, занятость персонала)",
                    "📅 Пересмотреть график ТО для предотвращения повторных просрочек",
                    "📧 Уведомить ответственных лиц о необходимости срочного выполнения",
                    "⚙️ Внести изменения в систему планирования ТО",
                    "📊 Провести анализ загруженности оборудования",
                    "👨‍🔧 Обучить персонал правилам соблюдения графика ТО"
            );

            for (String rec : recommendations) {
                Paragraph recPara = new Paragraph(rec, contentFont);
                recPara.setSpacingBefore(5);
                recPara.setIndentationLeft(20);
                document.add(recPara);
            }
            document.newPage();

            // === 10. ВЫВОДЫ ===
            Paragraph conclusionsTitle = new Paragraph("ВЫВОДЫ", subtitleFont);
            conclusionsTitle.setAlignment(Element.ALIGN_CENTER);
            conclusionsTitle.setSpacingBefore(20);
            conclusionsTitle.setSpacingAfter(15);
            document.add(conclusionsTitle);

            String conclusionText = String.format(
                    "На момент формирования отчета обнаружено %d просроченных технических обслуживаний. " +
                            "Средний срок просрочки составляет %.1f дней. " +
                            "Наибольшее количество просрочек приходится на тип ТО: %s. " +
                            "Требуется незамедлительное выполнение отложенных работ для обеспечения " +
                            "бесперебойной работы оборудования и соблюдения регламентов технического обслуживания.",
                    overdueList.size(),
                    avgOverdue,
                    byType.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("не определен")
            );

            Paragraph conclusion = new Paragraph(conclusionText, contentFont);
            conclusion.setSpacingAfter(15);
            conclusion.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(conclusion);

            // === 11. ПОДПИСИ ===
            Paragraph signatureTitle = new Paragraph("ПОДПИСИ", subtitleFont);
            signatureTitle.setAlignment(Element.ALIGN_CENTER);
            signatureTitle.setSpacingBefore(20);
            signatureTitle.setSpacingAfter(15);
            document.add(signatureTitle);

            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(80);
            signatureTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            signatureTable.setSpacingBefore(10);

            signatureTable.addCell(createCell("Главный инженер:", contentFont, Element.ALIGN_LEFT));
            signatureTable.addCell(createCell("___________________", contentFont, Element.ALIGN_LEFT));

            signatureTable.addCell(createCell("Начальник отдела ТО:", contentFont, Element.ALIGN_LEFT));
            signatureTable.addCell(createCell("___________________", contentFont, Element.ALIGN_LEFT));

            signatureTable.addCell(createCell("Дата:", contentFont, Element.ALIGN_LEFT));
            signatureTable.addCell(createCell(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), contentFont, Element.ALIGN_LEFT));

            document.add(signatureTable);

            // === 12. ИНФОРМАЦИЯ О СИСТЕМЕ ===
            Paragraph systemInfo = new Paragraph(
                    "Отчет сформирован автоматически системой управления ТО",
                    infoFont
            );
            systemInfo.setAlignment(Element.ALIGN_CENTER);
            systemInfo.setSpacingBefore(30);
            document.add(systemInfo);

            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Отчет успешно создан");
            alert.setHeaderText(null);
            alert.setContentText("Отчет успешно создан и сохранен по пути:\n" + filePath);
            alert.showAndWait();

            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (Exception e) {}
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

    private BaseFont loadArialFont() throws Exception {
        // Пробуем загрузить шрифт из resources
        try {
            java.io.InputStream inputStream = getClass().getResourceAsStream("/fonts/arial.ttf");
            if (inputStream != null) {
                System.out.println("Шрифт Arial загружен из resources/fonts/arial.ttf");
                // Сохраняем временный файл
                File tempFontFile = File.createTempFile("arial", ".ttf");
                tempFontFile.deleteOnExit();
                java.nio.file.Files.copy(inputStream, tempFontFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return BaseFont.createFont(tempFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        } catch (Exception e) {
            System.out.println("Не удалось загрузить шрифт из resources: " + e.getMessage());
        }

        // Пробуем загрузить из файловой системы
        String[] possiblePaths = {
                "fonts/arial.ttf",
                "./fonts/arial.ttf",
                "src/main/resources/fonts/arial.ttf",
                "./src/main/resources/fonts/arial.ttf",
                System.getProperty("user.dir") + "/fonts/arial.ttf",
                System.getProperty("user.dir") + "/src/main/resources/fonts/arial.ttf"
        };

        for (String path : possiblePaths) {
            try {
                File fontFile = new File(path);
                if (fontFile.exists()) {
                    System.out.println("Шрифт Arial загружен из файла: " + fontFile.getAbsolutePath());
                    return BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            } catch (Exception e) {
                // Пробуем следующий путь
            }
        }

        // Запасной вариант - Helvetica
        System.err.println("Шрифт Arial не найден! Использую Helvetica.");
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
    }

    private void addStatsRowVertical(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setPadding(5);
        labelCell.setBorderWidth(0);
        labelCell.setBackgroundColor(new BaseColor(240, 240, 240));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setPadding(8);
        valueCell.setBorderWidth(0);
        valueCell.setBackgroundColor(new BaseColor(220, 237, 200));
        valueCell.setMinimumHeight(30);
        table.addCell(valueCell);
    }

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

    // ==================== КОНЕЦ PDF ОТЧЕТА ====================

    @FXML
    private void openCalendarWindow() {
        if (!authService.canView()) {
            showAccessDenied();
            return;
        }

        try {
            Stage calendarStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/kurs/petrovkurs/maintenance-calendar-view.fxml"));
            Scene scene = new Scene(loader.load());
            try {
                Image icon = new Image(getClass().getResource("/images/icon_main.png").toExternalForm());
                calendarStage.getIcons().add(icon);
            } catch (Exception e) {}
            calendarStage.setTitle("Календарь технического обслуживания");
            calendarStage.setScene(scene);
            calendarStage.setWidth(1300);
            calendarStage.setHeight(850);
            calendarStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть календарь");
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Выход из системы");
        alert.setHeaderText("Вы действительно хотите выйти?");
        alert.setContentText("Все несохраненные данные будут потеряны.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                authService.logout();

                try {
                    Stage currentStage = (Stage) userInfoLabel.getScene().getWindow();
                    currentStage.close();

                    Stage loginStage = new Stage();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/kurs/petrovkurs/login-view.fxml"));
                    Parent root = loader.load();

                    Scene scene = new Scene(root);
                    loginStage.setTitle("Вход в систему");
                    loginStage.setScene(scene);
                    loginStage.setResizable(false);
                    loginStage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleHelp() {
        HelpController.showHelpWindow();
    }

    public void loadPane(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent pane = loader.load();

            Object controller = loader.getController();

            AuthService auth = AuthService.getInstance();
            boolean canEdit = auth.canEdit();      // true для админа и инженера
            boolean canDelete = auth.canDelete();  // true только для админа
            boolean canAdd = auth.canAdd();        // true для админа и инженера

            // Для разных типов контроллеров передаем разные права
            if (controller instanceof MachinesTableViewController) {
                // Станки - только админ может редактировать и удалять
                ((MachinesTableViewController) controller).setPermissions(auth.isAdmin(), auth.isAdmin());
            } else if (controller instanceof MaintenanceActsTableViewController) {
                // Акты ТО - инженер может добавлять/редактировать, но не удалять
                ((MaintenanceActsTableViewController) controller).setPermissions(canAdd, canDelete);
            } else if (controller instanceof MaintenanceScheduleTableViewController) {
                // График ТО - инженер может добавлять/редактировать, но не удалять
                ((MaintenanceScheduleTableViewController) controller).setPermissions(canAdd, canDelete);
            } else if (controller instanceof MaintenanceTypesTableViewController) {
                // Виды ТО - только админ может редактировать и удалять
                ((MaintenanceTypesTableViewController) controller).setPermissions(auth.isAdmin(), auth.isAdmin());
            }

            contentStack.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка загрузки страницы: " + e.getMessage());
        }
    }

    private void showAccessDenied() {
        Manager.MessageBox("Доступ запрещен", "Недостаточно прав",
                "У вашей учетной записи нет прав для выполнения этого действия",
                Alert.AlertType.WARNING);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}