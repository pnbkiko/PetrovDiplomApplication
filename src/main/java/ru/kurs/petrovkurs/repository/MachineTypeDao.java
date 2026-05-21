package ru.kurs.petrovkurs.repository;

import ru.kurs.petrovkurs.model.MachineType;

public class MachineTypeDao extends BaseDao<MachineType> {
    public MachineTypeDao() {
        super(MachineType.class);
    }
}