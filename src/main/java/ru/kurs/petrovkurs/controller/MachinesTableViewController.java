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
import ru.kurs.petrovkurs.HelloApplication;
import ru.kurs.petrovkurs.model.Machines;
import ru.kurs.petrovkurs.service.MachinesService;
import ru.kurs.petrovkurs.util.Manager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.kurs.petrovkurs.util.Manager.ShowConfirmPopup;

public class MachinesTableViewController implements Initializable {

    private int itemsCount;
    private MachinesService machinesService = new MachinesService();

    // Права доступа
    private boolean canEdit = true;
    private boolean canDelete = true;

    @FXML private MenuItem MenuItemAdd;
    @FXML private MenuItem MenuItemDelete;

    @FXML private TableColumn<Machines, String> TableColumnType;
    @FXML private TableColumn<Machines, String> TableColumnModel;
    @FXML private TableColumn<Machines, String> TableColumnModification;
    @FXML private TableColumn<Machines, String> TableColumnManufacturer;
    @FXML private TableColumn<Machines, String> TableColumnManufacturingYear;
    @FXML private TableColumn<Machines, String> TableColumnSerialNumber;
    @FXML private TableColumn<Machines, String> TableColumnInvNumber;
    @FXML private TableColumn<Machines, String> TableColumnCommissionedAt;

    @FXML private Label LabelInfo;
    @FXML private Label LabelDate;
    @FXML private TextField TextFieldSearch;
    @FXML private TableView<Machines> TableViewMachines;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initController();
    }

    public void initController() {
        setupRussianDateFormat();
        setCellValueFactories();

        // Поиск по всем полям при вводе текста
        TextFieldSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });

        filterData("");
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
            TableViewMachines.setContextMenu(null);
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

    void filterData(String searchText) {
        List<Machines> machines = machinesService.findAll();
        itemsCount = machines.size();

        List<Machines> filteredList = machines.stream()
                .filter(machine -> {
                    // Если поиск пустой - показываем все
                    if (searchText == null || searchText.trim().isEmpty()) {
                        return true;
                    }

                    String searchLower = searchText.toLowerCase().trim();

                    // Поиск по всем полям
                    boolean matchesModel = machine.getModel() != null &&
                            machine.getModel().toLowerCase().contains(searchLower);

                    boolean matchesManufacturer = machine.getManufacturer() != null &&
                            machine.getManufacturer().toLowerCase().contains(searchLower);

                    boolean matchesInvNumber = machine.getInvNumber() != null &&
                            machine.getInvNumber().toLowerCase().contains(searchLower);

                    boolean matchesSerialNumber = machine.getSerialNumber() != null &&
                            machine.getSerialNumber().toLowerCase().contains(searchLower);

                    boolean matchesModification = machine.getModification() != null &&
                            machine.getModification().toLowerCase().contains(searchLower);

                    boolean matchesType = machine.getTypeName() != null &&
                            machine.getTypeName().toLowerCase().contains(searchLower);

                    boolean matchesManufacturingYear = machine.getManufacturingYear() != null &&
                            String.valueOf(machine.getManufacturingYear()).contains(searchLower);

                    // Возвращаем true если хотя бы одно поле совпадает
                    return matchesModel || matchesManufacturer || matchesInvNumber ||
                            matchesSerialNumber || matchesModification || matchesType ||
                            matchesManufacturingYear;
                })
                .collect(Collectors.toList());

        TableViewMachines.getItems().setAll(filteredList);
        LabelInfo.setText("Найдено записей: " + filteredList.size() + " из " + itemsCount);
    }

    private void setCellValueFactories() {
        // Тип станка
        TableColumnType.setCellValueFactory(cellData -> {
            String typeName = cellData.getValue().getTypeName();
            return new SimpleStringProperty(typeName != null ? typeName : "");
        });

        // Модель
        TableColumnModel.setCellValueFactory(cellData -> cellData.getValue().getPropertyModel());

        // Модификация
        TableColumnModification.setCellValueFactory(cellData -> cellData.getValue().getPropertyModification());

        // Производитель
        TableColumnManufacturer.setCellValueFactory(cellData -> cellData.getValue().getPropertyManufacturer());

        // Год выпуска
        TableColumnManufacturingYear.setCellValueFactory(cellData -> cellData.getValue().getPropertyManufacturingYear());

        // Серийный номер
        TableColumnSerialNumber.setCellValueFactory(cellData -> cellData.getValue().getPropertySerialNumber());

        // Инвентарный номер
        TableColumnInvNumber.setCellValueFactory(cellData -> cellData.getValue().getPropertyInvNumber());

        // Дата ввода
        TableColumnCommissionedAt.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getCommissionedAt();
            if (date != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ru"));
                return new SimpleStringProperty(formatter.format(date));
            } else {
                return new SimpleStringProperty("");
            }
        });
    }

    void ShowEditProductWindow() {
        Stage newWindow = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("machines-edit-view.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add("main.css");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        newWindow.setTitle("Добавить станок");
        try {
            Image icon = new Image(getClass().getResource("/images/icon_main.png").toExternalForm());
            newWindow.getIcons().add(icon);
        } catch (Exception e) {}
        newWindow.initOwner(Manager.mainStage);
        newWindow.initModality(Modality.WINDOW_MODAL);
        newWindow.setScene(scene);
        Manager.currentStage = newWindow;
        newWindow.showAndWait();
        Manager.currentStage = null;
        filterData("");
    }

    @FXML
    private void MenuItemAddAction(ActionEvent event) {
        if (!canEdit) {
            showAccessDenied();
            return;
        }
        Manager.currentMachines = null;
        ShowEditProductWindow();
        filterData("");
    }

    @FXML
    private void MenuItemDeleteAction(ActionEvent event) {
        if (!canDelete) {
            showAccessDenied();
            return;
        }

        Machines machines = TableViewMachines.getSelectionModel().getSelectedItem();
        if (machines == null) {
            Manager.MessageBox("Внимание", "Не выбрана запись", "Пожалуйста, выберите запись для удаления", Alert.AlertType.WARNING);
            return;
        }

        Optional<ButtonType> result = ShowConfirmPopup();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                machinesService.delete(machines);
                filterData("");
                Manager.MessageBox("Успешно", "Запись удалена", "Запись успешно удалена из системы", Alert.AlertType.INFORMATION);
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
        Machines selectedMachine = TableViewMachines.getSelectionModel().getSelectedItem();
        if (selectedMachine != null) {
            Manager.currentMachines = selectedMachine;
            ShowEditProductWindow();
        }
    }

    @FXML
    private void clearSearch(ActionEvent event) {
        TextFieldSearch.clear();
        filterData("");
    }

    private void showAccessDenied() {
        Manager.MessageBox("Доступ запрещен", "Недостаточно прав",
                "У вашей учетной записи нет прав для выполнения этого действия", Alert.AlertType.WARNING);
    }
}