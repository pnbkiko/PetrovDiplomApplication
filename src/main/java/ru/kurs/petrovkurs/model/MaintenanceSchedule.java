package ru.kurs.petrovkurs.model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "maintenance_schedule", schema = "public")
public class MaintenanceSchedule {

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machines machines;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = false)
    private MaintenanceTypes maintenanceTypes;

    @Column(name = "next_due", nullable = false)
    private LocalDate nextDue;

    @Column(name = "last_done")
    private LocalDate lastDone;

    public MaintenanceSchedule() {
    }

    public StringProperty getMachineModel() {
        return new SimpleStringProperty(machines != null ? machines.getModel() : "");
    }

    public StringProperty getTypeName() {
        return new SimpleStringProperty(maintenanceTypes != null ? maintenanceTypes.getName_() : "");
    }

    public String getTypeNames() {
        return maintenanceTypes != null ? maintenanceTypes.getName_() : "";
    }

    public Machines getMachines() {
        return machines;
    }

    public void setMachines(Machines machines) {
        this.machines = machines;
    }

    public void setType(MaintenanceTypes maintenanceTypes) {
        this.maintenanceTypes = maintenanceTypes;
    }

    public MaintenanceTypes getType() {
        return maintenanceTypes;
    }

    public void setMaintenanceTypes(MaintenanceTypes maintenanceTypes) {
        this.maintenanceTypes = maintenanceTypes;
    }

    public LocalDate getNextDue() {
        return nextDue;
    }

    public void setNextDue(LocalDate nextDue) {
        this.nextDue = nextDue;
    }

    public LocalDate getLastDone() {
        return lastDone;
    }

    public void setLastDone(LocalDate lastDone) {
        this.lastDone = lastDone;
    }

    public StringProperty getPropertyLastDone() {
        return new SimpleStringProperty(this.lastDone != null ? this.lastDone.toString() : "");
    }

    public StringProperty getPropertyNextDue() {
        return new SimpleStringProperty(this.nextDue != null ? this.nextDue.toString() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaintenanceSchedule)) return false;
        MaintenanceSchedule that = (MaintenanceSchedule) o;
        return Objects.equals(machines, that.machines) &&
                Objects.equals(maintenanceTypes, that.maintenanceTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(machines, maintenanceTypes);
    }

    @Override
    public String toString() {
        return (machines != null ? machines.getModel() : "") + " - " +
                (maintenanceTypes != null ? maintenanceTypes.getName_() : "");
    }
}