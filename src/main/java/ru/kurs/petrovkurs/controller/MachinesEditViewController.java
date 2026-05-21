package ru.kurs.petrovkurs.controller;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.kurs.petrovkurs.model.MachineType;
import ru.kurs.petrovkurs.model.Machines;
import ru.kurs.petrovkurs.service.MachineTypeService;
import ru.kurs.petrovkurs.service.MachinesService;
import ru.kurs.petrovkurs.util.Manager;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static ru.kurs.petrovkurs.util.Manager.MessageBox;

public class MachinesEditViewController implements Initializable {

    @FXML private ComboBox<MachineType> ComboBoxType;
    @FXML private TextField TextFieldModel;
    @FXML private TextField TextFieldModification;
    @FXML private TextField TextFieldManufacturer;
    @FXML private TextField TextFieldManufacturingYear;
    @FXML private TextField TextFieldSerialNumber;
    @FXML private TextField TextFieldInvNumber;
    @FXML private DatePicker DatePickerCommissionedAt;
    @FXML private Button BtnCancel, BtnSave;
    @FXML private Label errorLabel;

    private MachinesService machinesService = new MachinesService();
    private MachineTypeService machineTypeService = new MachineTypeService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBoxes();

        if (Manager.currentMachines != null) {
            loadExistingData();
        } else {
            Manager.currentMachines = new Machines();
            DatePickerCommissionedAt.setValue(LocalDate.now());
        }

        setupValidation();
    }

    private void setupComboBoxes() {
        ComboBoxType.setItems(FXCollections.observableArrayList(machineTypeService.findAll()));
        ComboBoxType.setCellFactory(param -> new ListCell<MachineType>() {
            @Override
            protected void updateItem(MachineType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        ComboBoxType.setButtonCell(new ListCell<MachineType>() {
            @Override
            protected void updateItem(MachineType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    private void loadExistingData() {
        try {
            ComboBoxType.setValue(Manager.currentMachines.getMachineType());
            TextFieldModel.setText(Manager.currentMachines.getModel());
            TextFieldModification.setText(Manager.currentMachines.getModification());
            TextFieldManufacturer.setText(Manager.currentMachines.getManufacturer());

            if (Manager.currentMachines.getManufacturingYear() != null) {
                TextFieldManufacturingYear.setText(String.valueOf(Manager.currentMachines.getManufacturingYear()));
            }

            TextFieldSerialNumber.setText(Manager.currentMachines.getSerialNumber());
            TextFieldInvNumber.setText(Manager.currentMachines.getInvNumber());

            if (Manager.currentMachines.getCommissionedAt() != null) {
                DatePickerCommissionedAt.setValue(Manager.currentMachines.getCommissionedAt());
            }
        } catch (Exception e) {
            showError("Ошибка загрузки данных: " + e.getMessage());
        }
    }

    private void setupValidation() {
        TextFieldModel.textProperty().addListener((obs, oldVal, newVal) -> validateField(TextFieldModel));
        TextFieldInvNumber.textProperty().addListener((obs, oldVal, newVal) -> validateField(TextFieldInvNumber));
        TextFieldManufacturer.textProperty().addListener((obs, oldVal, newVal) -> validateField(TextFieldManufacturer));
        ComboBoxType.valueProperty().addListener((obs, oldVal, newVal) -> validateComboBox(ComboBoxType));
        DatePickerCommissionedAt.valueProperty().addListener((obs, oldVal, newVal) -> validateDatePicker(DatePickerCommissionedAt));

        TextFieldManufacturingYear.textProperty().addListener((obs, oldVal, newVal) -> validateYear(newVal));
    }

    private void validateField(TextField field) {
        field.getStyleClass().remove("error-field");
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            field.getStyleClass().add("error-field");
        }
    }

    private void validateComboBox(ComboBox<?> comboBox) {
        comboBox.getStyleClass().remove("error-field");
        if (comboBox.getValue() == null) {
            comboBox.getStyleClass().add("error-field");
        }
    }

    private void validateDatePicker(DatePicker datePicker) {
        datePicker.getStyleClass().remove("error-field");
        if (datePicker.getValue() == null) {
            datePicker.getStyleClass().add("error-field");
        }
    }

    private void validateYear(String yearText) {
        TextFieldManufacturingYear.getStyleClass().remove("error-field");
        if (yearText != null && !yearText.trim().isEmpty()) {
            try {
                int year = Integer.parseInt(yearText);
                int currentYear = LocalDate.now().getYear();
                if (year < 1900 || year > currentYear + 1) {
                    TextFieldManufacturingYear.getStyleClass().add("error-field");
                }
            } catch (NumberFormatException e) {
                TextFieldManufacturingYear.getStyleClass().add("error-field");
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        if (ComboBoxType.getValue() == null) {
            ComboBoxType.getStyleClass().add("error-field");
            errorMessage.append("• Выберите тип станка\n");
            isValid = false;
        }

        if (TextFieldModel.getText() == null || TextFieldModel.getText().trim().isEmpty()) {
            TextFieldModel.getStyleClass().add("error-field");
            errorMessage.append("• Укажите модель станка\n");
            isValid = false;
        }

        if (TextFieldManufacturer.getText() == null || TextFieldManufacturer.getText().trim().isEmpty()) {
            TextFieldManufacturer.getStyleClass().add("error-field");
            errorMessage.append("• Укажите производителя\n");
            isValid = false;
        }

        if (TextFieldInvNumber.getText() == null || TextFieldInvNumber.getText().trim().isEmpty()) {
            TextFieldInvNumber.getStyleClass().add("error-field");
            errorMessage.append("• Укажите инвентарный номер\n");
            isValid = false;
        }

        if (DatePickerCommissionedAt.getValue() == null) {
            DatePickerCommissionedAt.getStyleClass().add("error-field");
            errorMessage.append("• Укажите дату ввода\n");
            isValid = false;
        }

        String yearText = TextFieldManufacturingYear.getText();
        if (yearText != null && !yearText.trim().isEmpty()) {
            try {
                int year = Integer.parseInt(yearText);
                int currentYear = LocalDate.now().getYear();
                if (year < 1900 || year > currentYear + 1) {
                    errorMessage.append("• Год выпуска должен быть между 1900 и ").append(currentYear + 1).append("\n");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                errorMessage.append("• Год выпуска должен быть числом\n");
                isValid = false;
            }
        }

        if (!isValid && errorLabel != null) {
            errorLabel.setText(errorMessage.toString());
            errorLabel.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } else if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        return isValid;
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    @FXML
    void BtnCancelAction(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void BtnSaveAction(ActionEvent event) {
        if (!validateForm()) {
            MessageBox("Ошибка валидации", "Не все обязательные поля заполнены",
                    "Пожалуйста, заполните все поля, отмеченные звездочкой (*)", Alert.AlertType.ERROR);
            return;
        }

        try {
            Manager.currentMachines.setMachineType(ComboBoxType.getValue());
            Manager.currentMachines.setModel(TextFieldModel.getText().trim());
            Manager.currentMachines.setModification(TextFieldModification.getText().trim());
            Manager.currentMachines.setManufacturer(TextFieldManufacturer.getText().trim());

            String yearText = TextFieldManufacturingYear.getText().trim();
            if (!yearText.isEmpty()) {
                Manager.currentMachines.setManufacturingYear(Integer.parseInt(yearText));
            }

            Manager.currentMachines.setSerialNumber(TextFieldSerialNumber.getText().trim());
            Manager.currentMachines.setInvNumber(TextFieldInvNumber.getText().trim());
            Manager.currentMachines.setCommissionedAt(DatePickerCommissionedAt.getValue());

            if (Manager.currentMachines.getMachinesId() == null) {
                machinesService.save(Manager.currentMachines);
                MessageBox("Успешно", "Станок сохранен", "Данные успешно сохранены в систему", Alert.AlertType.INFORMATION);
            } else {
                machinesService.update(Manager.currentMachines);
                MessageBox("Успешно", "Станок обновлен", "Данные успешно обновлены", Alert.AlertType.INFORMATION);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            MessageBox("Ошибка", "Не удалось сохранить данные", "Произошла ошибка при сохранении: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}