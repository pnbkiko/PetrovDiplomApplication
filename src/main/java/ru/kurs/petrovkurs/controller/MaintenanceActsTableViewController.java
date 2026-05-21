package ru.kurs.petrovkurs.controller;

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
import ru.kurs.petrovkurs.model.MaintenanceActs;
import ru.kurs.petrovkurs.service.MaintenanceActsService;
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

public class MaintenanceActsTableViewController implements Initializable {

    private int itemsCount;
    private MaintenanceActsService maintenanceActsService = new MaintenanceActsService();

    // Права доступа
    private boolean canEdit = true;      // Добавление/редактирование
    private boolean canDelete = true;    // Удаление

    @FXML private MenuItem MenuItemAdd;
    @FXML private MenuItem MenuItemDelete;

    @FXML private DatePicker DatePickerFilter;
    @FXML private TableColumn<MaintenanceActs, String> TableColumnMachines;
    @FXML private TableColumn<MaintenanceActs, String> TableColumnTypes;
    @FXML private TableColumn<MaintenanceActs, String> TableColumnDate;
    @FXML private TableColumn<MaintenanceActs, String> TableColumnEngineer;
    @FXML private TableColumn<MaintenanceActs, String> TableColumnNotes;
    @FXML private TableColumn<MaintenanceActs, String> TableColumnSigned;
    @FXML private Label LabelInfo;
    @FXML private Label LabelDate;
    @FXML private TableView<MaintenanceActs> TableViewMaintenanceActs;

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

        // Если нет прав на редактирование - скрываем кнопку добавления
        if (!canEdit) {
            MenuItemAdd.setVisible(false);
            MenuItemDelete.setVisible(false);
            TableViewMaintenanceActs.setContextMenu(null);
        }
        // Если есть права на редактирование, но нет на удаление - показываем только добавление
        else if (!canDelete) {
            MenuItemAdd.setVisible(true);
            MenuItemDelete.setVisible(false);
        }
        // Админ - всё видно
        else {
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
        List<MaintenanceActs> maintenanceActs = maintenanceActsService.findAll();
        itemsCount = maintenanceActs.size();

        List<MaintenanceActs> filteredList = maintenanceActs.stream()
                .filter(act -> {
                    if (dateFilter != null) {
                        return act.getDate_() != null && act.getDate_().equals(dateFilter);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        TableViewMaintenanceActs.getItems().setAll(filteredList);
        LabelInfo.setText("Всего записей " + filteredList.size() + " из " + itemsCount);
    }

    private void setCellValueFactories() {
        TableColumnMachines.setCellValueFactory(cellData -> cellData.getValue().getMachineModel());
        TableColumnTypes.setCellValueFactory(cellData -> cellData.getValue().getTypeName());

        TableColumnDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDate_();
            if (date != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ru"));
                return new SimpleStringProperty(formatter.format(date));
            }
            return new SimpleStringProperty("");
        });

        TableColumnEngineer.setCellValueFactory(cellData -> cellData.getValue().getPropertyEngineer());
        TableColumnNotes.setCellValueFactory(cellData -> cellData.getValue().getPropertyNotes());
        TableColumnSigned.setCellValueFactory(cellData -> cellData.getValue().getPropertySigned());
    }

    void ShowEditProductWindow() {
        Stage newWindow = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("maintenance-acts-edit-view.fxml"));

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
        newWindow.setTitle("Добавить акт ТО");
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
        Manager.currentMaintenanceActs = null;
        ShowEditProductWindow();
        filterData(null);
    }

    @FXML
    void MenuItemDeleteAction(ActionEvent event) {
        if (!canDelete) {
            showAccessDenied();
            return;
        }

        MaintenanceActs maintenanceActs = TableViewMaintenanceActs.getSelectionModel().getSelectedItem();

        if (maintenanceActs == null) {
            Manager.MessageBox("Внимание", "Не выбрана запись", "Пожалуйста, выберите запись для удаления", Alert.AlertType.WARNING);
            return;
        }

        Optional<ButtonType> result = ShowConfirmPopup();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                maintenanceActsService.delete(maintenanceActs);
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
        MaintenanceActs selectedAct = TableViewMaintenanceActs.getSelectionModel().getSelectedItem();
        if (selectedAct != null) {
            Manager.currentMaintenanceActs = selectedAct;
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