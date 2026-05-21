package ru.kurs.petrovkurs.model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "machines", schema = "public")
public class Machines {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = false)
    private MachineType machineType;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "modification")
    private String modification;

    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @Column(name = "manufacturing_year")
    private Integer manufacturingYear;

    @Column(name = "serial_number", unique = true)
    private String serialNumber;

    @Column(name = "inv_number", nullable = false, unique = true)
    private String invNumber;

    @Column(name = "commissioned_at", nullable = false)
    private LocalDate commissionedAt;

    @Column(name = "created_at")
    private LocalDate createdAt;

    public Machines() {
        this.createdAt = LocalDate.now();
    }

    // Геттеры и сеттеры
    public Long getMachinesId() {
        return id;
    }

    public void setMachinesId(Long id) {
        this.id = id;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public void setMachineType(MachineType machineType) {
        this.machineType = machineType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModification() {
        return modification;
    }

    public void setModification(String modification) {
        this.modification = modification;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getManufacturingYear() {
        return manufacturingYear;
    }

    public void setManufacturingYear(Integer manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getInvNumber() {
        return invNumber;
    }

    public void setInvNumber(String invNumber) {
        this.invNumber = invNumber;
    }

    public LocalDate getCommissionedAt() {
        return commissionedAt;
    }

    public void setCommissionedAt(LocalDate commissionedAt) {
        this.commissionedAt = commissionedAt;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    // JavaFX Property методы для таблиц
    public StringProperty getPropertyModel() {
        return new SimpleStringProperty(this.model);
    }

    public StringProperty getPropertyInvNumber() {
        return new SimpleStringProperty(this.invNumber);
    }

    public StringProperty getPropertyManufacturer() {
        return new SimpleStringProperty(this.manufacturer);
    }

    public StringProperty getPropertyModification() {
        return new SimpleStringProperty(this.modification != null ? this.modification : "");
    }

    public StringProperty getPropertySerialNumber() {
        return new SimpleStringProperty(this.serialNumber != null ? this.serialNumber : "");
    }

    public StringProperty getPropertyManufacturingYear() {
        return new SimpleStringProperty(this.manufacturingYear != null ? String.valueOf(this.manufacturingYear) : "");
    }

    public StringProperty getPropertyCommissionedAt() {
        return new SimpleStringProperty(this.commissionedAt != null ? this.commissionedAt.toString() : "");
    }

    public String getTypeName() {
        return machineType != null ? machineType.getName() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Machines)) return false;
        Machines machines = (Machines) o;
        return Objects.equals(id, machines.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return model + (modification != null ? " (" + modification + ")" : "") + " - " + invNumber;
    }
}