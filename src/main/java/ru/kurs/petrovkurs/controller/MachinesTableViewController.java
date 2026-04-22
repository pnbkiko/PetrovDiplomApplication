package ru.kurs.petrovkurs.controller;
import javafx.beans.property.SimpleStringProperty;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
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
import ru.kurs.petrovkurs.model.Machines;
import ru.kurs.petrovkurs.service.MachinesService;
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

public class MachinesTableViewController implements Initializable {

    private int itemsCount;
    private MachinesService machinesService = new MachinesService();

    @FXML
    private TableColumn<Machines, String> TableColumnModel;
    @FXML
    private TableColumn<Machines, String> TableColumnInvNumber;
    @FXML
    private TableColumn<Machines, String> TableColumnCommissionedAt;
    @FXML
    private Label LabelInfo;
    @FXML
    private Label LabelDate;
    @FXML
    private TextField TextFieldSearch;
    @FXML
    private TableView<Machines> TableViewMachines;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initController();
    }

    public void initController() {
        // Устанавливаем русский формат даты
        setupRussianDateFormat();

        setCellValueFactories();

        TextFieldSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });

        filterData("");
    }

    private void setupRussianDateFormat() {
        // Устанавливаем текущую дату в русском формате с месяцем прописью
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("ru"));
        String todayDate = LocalDate.now().format(formatter);
        LabelDate.setText("Дата: " + todayDate);
    }
    public static int countFilteredItems(List<Machines> allItems, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return allItems.size();
        }

        String searchLower = searchText.toLowerCase();
        return (int) allItems.stream()
                .filter(machine -> {
                    String model = machine.getModel();
                    return model != null && model.toLowerCase().contains(searchLower);
                })
                .count();
    }
    public static String formatItemsCountInfo(int filteredCount, int totalCount) {
        return String.format("Всего записей %d из %d", filteredCount, totalCount);
    }
    void filterData(String searchText) {
        List<Machines> machines = machinesService.findAll();
        itemsCount = machines.size();

        List<Machines> filteredList = machines.stream()
                .filter(machine -> {
                    String model = machine.getPropertyModel().get();
                    if (model != null) {
                        return model.toLowerCase().contains(searchText.toLowerCase());
                    }
                    return false;
                })
                .collect(Collectors.toList());

        TableViewMachines.getItems().setAll(filteredList);

        int filteredItemsCount = countFilteredItems(machines,searchText);
        LabelInfo.setText(formatItemsCountInfo(filteredItemsCount,itemsCount));
    }

    private void setCellValueFactories() {
        TableColumnInvNumber.setCellValueFactory(cellData -> cellData.getValue().getPropertyInvNumber());
        TableColumnModel.setCellValueFactory(cellData -> cellData.getValue().getPropertyModel());

        // Устанавливаем форматирование даты в таблице
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
        Image icon = new Image(getClass().getResource("/images/icon_main.png").toExternalForm());
        newWindow.getIcons().add(icon);
        newWindow.initOwner(Manager.secondStage);
        newWindow.initModality(Modality.WINDOW_MODAL);
        newWindow.setScene(scene);
        Manager.currentStage = newWindow;
        newWindow.showAndWait();
        Manager.currentStage = null;
        filterData("");
    }

    @FXML
    private void MenuItemAddAction(ActionEvent event) {
        Manager.currentMachines = null;
        ShowEditProductWindow();
        filterData("");
    }

    @FXML
    private void MenuItemBackAction(ActionEvent event) {
        if (Manager.secondStage != null) {
            Manager.secondStage.close();
        }
        Manager.mainStage.show();
    }

    @FXML
    private void MenuItemDeleteAction(ActionEvent event) {
        Machines machines = TableViewMachines.getSelectionModel().getSelectedItem();
        Optional<ButtonType> result = ShowConfirmPopup();
        if (result.get() == ButtonType.OK) {
            machinesService.delete(machines);
            filterData("");
        }
    }

    // Добавляем метод для очистки поиска
    @FXML
    private void clearSearch(ActionEvent event) {
        TextFieldSearch.clear();
        filterData("");
    }
}