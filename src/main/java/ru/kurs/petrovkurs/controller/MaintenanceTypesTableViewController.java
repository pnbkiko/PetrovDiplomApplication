package ru.kurs.petrovkurs.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.kurs.petrovkurs.HelloApplication;
import ru.kurs.petrovkurs.model.MaintenanceTypes;
import ru.kurs.petrovkurs.service.MaintenanceTypesService;
import ru.kurs.petrovkurs.util.Manager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static ru.kurs.petrovkurs.util.Manager.ShowConfirmPopup;

public class MaintenanceTypesTableViewController implements Initializable {

    private int itemsCount;
    private MaintenanceTypesService maintenanceTypesService = new MaintenanceTypesService();

    // Права доступа
    private boolean canEdit = true;
    private boolean canDelete = true;

    @FXML private MenuItem MenuItemAdd;
    @FXML private MenuItem MenuItemDelete;

    @FXML private TableColumn<MaintenanceTypes, String> TableColumnName;
    @FXML private TableColumn<MaintenanceTypes, String> TableColumnIntervalDays;
    @FXML private Label LabelInfo;
    @FXML private Label LabelDate;
    @FXML private TableView<MaintenanceTypes> TableViewMaintenanceTypes;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initController();
    }

    public void initController() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String todayDate = LocalDate.now().format(formatter);
        LabelDate.setText("Сегодня: " + todayDate);
        setCellValueFactories();
        filterData();
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

        // Виды ТО - только админ может добавлять и удалять
        if (!canEdit) {
            MenuItemAdd.setVisible(false);
            MenuItemDelete.setVisible(false);
            TableViewMaintenanceTypes.setContextMenu(null);
        } else if (!canDelete) {
            MenuItemAdd.setVisible(true);
            MenuItemDelete.setVisible(false);
        } else {
            MenuItemAdd.setVisible(true);
            MenuItemDelete.setVisible(true);
        }
    }

    void ShowEditProductWindow() {
        Stage newWindow = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("maintenance-types-edit-view.fxml"));

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
        filterData();
    }

    void filterData() {
        List<MaintenanceTypes> maintenanceTypes = maintenanceTypesService.findAll();
        itemsCount = maintenanceTypes.size();

        TableViewMaintenanceTypes.getItems().clear();
        for (MaintenanceTypes mt : maintenanceTypes) {
            TableViewMaintenanceTypes.getItems().add(mt);
        }
        LabelInfo.setText("Всего записей " + maintenanceTypes.size() + " из " + itemsCount);
    }

    private void setCellValueFactories() {
        TableColumnName.setCellValueFactory(cellData -> cellData.getValue().getPropertyName());
        TableColumnIntervalDays.setCellValueFactory(cellData -> cellData.getValue().getPropertyIntervalDays());
    }

    @FXML
    void MenuItemAddAction(ActionEvent event) {
        if (!canEdit) {
            showAccessDenied();
            return;
        }
        Manager.currentMaintenanceTypes = null;
        ShowEditProductWindow();
        filterData();
    }

    @FXML
    void MenuItemDeleteAction(ActionEvent event) {
        if (!canDelete) {
            showAccessDenied();
            return;
        }

        MaintenanceTypes maintenanceTypes = TableViewMaintenanceTypes.getSelectionModel().getSelectedItem();

        if (maintenanceTypes == null) {
            Manager.MessageBox("Внимание", "Не выбрана запись", "Пожалуйста, выберите запись для удаления", Alert.AlertType.WARNING);
            return;
        }

        Optional<ButtonType> result = ShowConfirmPopup();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                maintenanceTypesService.delete(maintenanceTypes);
                filterData();
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
        MaintenanceTypes selectedType = TableViewMaintenanceTypes.getSelectionModel().getSelectedItem();
        if (selectedType != null) {
            Manager.currentMaintenanceTypes = selectedType;
            ShowEditProductWindow();
        }
    }

    private void showAccessDenied() {
        Manager.MessageBox("Доступ запрещен", "Недостаточно прав", "У вашей учетной записи нет прав для выполнения этого действия", Alert.AlertType.WARNING);
    }
}