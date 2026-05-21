package ru.kurs.petrovkurs.service;

import ru.kurs.petrovkurs.model.MachineType;
import ru.kurs.petrovkurs.repository.MachineTypeDao;
import java.util.List;

public class MachineTypeService {
    private MachineTypeDao machineTypeDao = new MachineTypeDao();

    public List<MachineType> findAll() {
        return machineTypeDao.findAll();
    }

    public MachineType findOne(Long id) {
        return machineTypeDao.findOne(id);
    }

    public void save(MachineType entity) {
        if (entity != null) {
            machineTypeDao.save(entity);
        }
    }

    public void update(MachineType entity) {
        if (entity != null) {
            machineTypeDao.update(entity);
        }
    }

    public void delete(MachineType entity) {
        if (entity != null) {
            machineTypeDao.delete(entity);
        }
    }
}