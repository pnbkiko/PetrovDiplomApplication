package ru.kurs.petrovkurs.model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "maintenance_acts", schema = "public")
public class MaintenanceActs {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machines machines;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = false)
    private MaintenanceTypes maintenanceTypes;

    @Column(name = "date", nullable = false)
    private LocalDate date_;

    @Column(name = "engineer", nullable = false)
    private String engineer;

    @Column(name = "notes")
    private String notes;

    @Column(name = "signed", nullable = false)
    private Boolean signed = false;

    public MaintenanceActs() {
    }

    public Long getMaintenanceActsId() {
        return id;
    }

    public void setMaintenanceActsId(Long id) {
        this.id = id;
    }

    public StringProperty getMachineModel() {
        return new SimpleStringProperty(machines != null ? machines.getModel() : "");
    }

    public StringProperty getTypeName() {
        return new SimpleStringProperty(maintenanceTypes != null ? maintenanceTypes.getName_() : "");
    }

    public Machines getMachines() {
        return machines;
    }

    public void setMachines(Machines machines) {
        this.machines = machines;
    }

    public MaintenanceTypes getType() {
        return maintenanceTypes;
    }

    public void setMaintenanceTypes(MaintenanceTypes maintenanceTypes) {
        this.maintenanceTypes = maintenanceTypes;
    }

    public LocalDate getDate_() {
        return date_;
    }

    public void setDate_(LocalDate date_) {
        this.date_ = date_;
    }

    public String getEngineer() {
        return engineer;
    }

    public void setEngineer(String engineer) {
        this.engineer = engineer;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getSigned() {
        return signed;
    }

    public void setSigned(Boolean signed) {
        this.signed = signed;
    }

    public StringProperty getPropertyEngineer() {
        return new SimpleStringProperty(this.engineer != null ? this.engineer : "");
    }

    public StringProperty getPropertyNotes() {
        return new SimpleStringProperty(this.notes != null ? this.notes : "");
    }

    public StringProperty getPropertySigned() {
        return new SimpleStringProperty(this.signed != null ? (this.signed ? "Да" : "Нет") : "Нет");
    }

    public StringProperty getPropertyDate_() {
        return new SimpleStringProperty(this.date_ != null ? this.date_.toString() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaintenanceActs)) return false;
        MaintenanceActs that = (MaintenanceActs) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (machines != null ? machines.getModel() : "") + " - " + date_;
    }
}