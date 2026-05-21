package ru.kurs.petrovkurs.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ru.kurs.petrovkurs.HelloApplication;
import ru.kurs.petrovkurs.model.MaintenanceSchedule;
import ru.kurs.petrovkurs.service.MaintenanceScheduleService;
import ru.kurs.petrovkurs.util.Manager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.kurs.petrovkurs.util.Manager.ShowConfirmPopup;

public class MaintenanceScheduleTableViewController implements Initializable {

    private int itemsCount;
    private MaintenanceScheduleService maintenanceScheduleService = new MaintenanceScheduleService();

    // Права доступа
    private boolean canEdit = true;      // Добавление/редактирование
    private boolean canDelete = true;    // Удаление

    @FXML private MenuItem MenuItemAdd;
    @FXML private MenuItem MenuItemDelete;

    @FXML private DatePicker DatePickerFilter;
    @FXML private TableColumn<MaintenanceSchedule, String> TableColumnMachines;
    @FXML private TableColumn<MaintenanceSchedule, String> TableColumnTypes;
    @FXML private TableColumn<MaintenanceSchedule, LocalDate> TableColumnNextDue;
    @FXML private TableColumn<MaintenanceSchedule, String> TableColumnLastDone;
    @FXML private Label LabelInfo;
    @FXML private Label LabelDate;
    @FXML private TableView<MaintenanceSchedule> TableViewMaintenanceSchedule;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initController();
    }

    public void initController() {
        setupRussianDateFormat();
        setupRussianDatePicker();
        setCellValueFactories();

        DatePickerFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterData(newVal);
        });

        filterData(null);
        applyPermissions();
    }

    public void setPermissions(boolean canEdit, boolean canDelete) {
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        applyPermissions();
    }

    private void applyPermissions() {
        if (MenuItemAdd == null || MenuItemDelete == null) {
            return;
        }

        if (!canEdit) {
            MenuItemAdd.setVisible(false);
            MenuItemDelete.setVisible(false);
            TableViewMaintenanceSchedule.setContextMenu(null);
        } else if (!canDelete) {
            MenuItemAdd.setVisible(true);
            MenuItemDelete.setVisible(false);
        } else {
            MenuItemAdd.setVisible(true);
            MenuItemDelete.setVisible(true);
        }
    }

    private void setupRussianDateFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("ru"));
        String todayDate = LocalDate.now().format(formatter);
        LabelDate.setText("Дата: " + todayDate);
    }

    private void setupRussianDatePicker() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ru"));

        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                }
                return null;
            }
        };

        DatePickerFilter.setConverter(converter);
        DatePickerFilter.setPromptText("дд.мм.гггг");
    }

    void filterData(LocalDate dateFilter) {
        List<MaintenanceSchedule> maintenanceSchedules = maintenanceScheduleService.findAll();
        itemsCount = maintenanceSchedules.size();

        List<MaintenanceSchedule> filteredList = maintenanceSchedules.stream()
                .filter(schedule -> {
                    if (dateFilter != null) {
                        return schedule.getLastDone() != null && schedule.getLastDone().equals(dateFilter);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        TableViewMaintenanceSchedule.getItems().setAll(filteredList);
        LabelInfo.setText("Всего записей " + filteredList.size() + " из " + itemsCount);
    }

    private void setCellValueFactories() {
        TableColumnMachines.setCellValueFactory(cellData -> cellData.getValue().getMachineModel());
        TableColumnTypes.setCellValueFactory(cellData -> cellData.getValue().getTypeName());

        TableColumnNextDue.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getNextDue()));
        TableColumnNextDue.setCellFactory(column -> new TableCell<MaintenanceSchedule, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ru"));

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatter.format(item));
                    LocalDate today = LocalDate.now();
                    if (item.isBefore(today)) {
                        setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: black;");
                    } else if (item.equals(today)) {
                        setStyle("-fx-background-color: #ffff66; -fx-text-fill: black;");
                    } else if (item.equals(today.plusDays(1))) {
                        setStyle("-fx-background-color: #66ffff; -fx-text-fill: black;");
                    } else if (item.equals(today.plusDays(2))) {
                        setStyle("-fx-background-color: #99ff99; -fx-text-fill: black;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        TableColumnLastDone.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getLastDone();
            if (date != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ru"));
                return new SimpleStringProperty(formatter.format(date));
            }
            return new SimpleStringProperty("");
        });
    }

    void ShowEditProductWindow() {
        Stage newWindow = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("maintenance-schedule-edit-view.fxml"));

        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add("main.css");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Image icon = new Image(getClass().getResource("/images/icon_main.png").toExternalForm());
            newWindow.getIcons().add(icon);
        } catch (Exception e) {}
        newWindow.setTitle("Изменить данные");
        newWindow.initOwner(Manager.mainStage);
        newWindow.initModality(Modality.WINDOW_MODAL);
        newWindow.setScene(scene);
        Manager.currentStage = newWindow;
        newWindow.showAndWait();
        Manager.currentStage = null;
        filterData(null);
    }

    @FXML
    void MenuItemAddAction(ActionEvent event) {
        if (!canEdit) {
            showAccessDenied();
            return;
        }
        Manager.currentMaintenanceSchedule = null;
        ShowEditProductWindow();
        filterData(null);
    }

    @FXML
    void MenuItemDeleteAction(ActionEvent event) {
        if (!canDelete) {
            showAccessDenied();
            return;
        }

        MaintenanceSchedule maintenanceSchedule = TableViewMaintenanceSchedule.getSelectionModel().getSelectedItem();

        if (maintenanceSchedule == null) {
            Manager.MessageBox("Внимание", "Не выбрана запись", "Пожалуйста, выберите запись для удаления", Alert.AlertType.WARNING);
            return;
        }

        Optional<ButtonType> result = ShowConfirmPopup();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                maintenanceScheduleService.delete(maintenanceSchedule);
                filterData(null);
                Manager.MessageBox("Успешно", "Запись удалена", "Запись успешно удалена", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                Manager.MessageBox("Ошибка", "Не удалось удалить запись", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void onTableDoubleClick() {
        if (!canEdit) {
            showAccessDenied();
            return;
        }
        MaintenanceSchedule selectedSchedule = TableViewMaintenanceSchedule.getSelectionModel().getSelectedItem();
        if (selectedSchedule != null) {
            Manager.currentMaintenanceSchedule = selectedSchedule;
            ShowEditProductWindow();
        }
    }

    @FXML
    private void clearFilter(ActionEvent event) {
        DatePickerFilter.setValue(null);
        filterData(null);
    }

    private void showAccessDenied() {
        Manager.MessageBox("Доступ запрещен", "Недостаточно прав",
                "У вашей учетной записи нет прав для выполнения этого действия", Alert.AlertType.WARNING);
    }
}