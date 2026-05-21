package ru.kurs.petrovkurs.service;

import ru.kurs.petrovkurs.model.Machines;
import ru.kurs.petrovkurs.repository.MachinesDao;
import java.util.List;

public class MachinesService {
    private MachinesDao machinesDao = new MachinesDao();

    public List<Machines> findAll() {
        return machinesDao.findAll();
    }

    public Machines findOne(Long id) {
        return machinesDao.findOne(id);
    }

    public void save(Machines entity) {
        if (entity != null) {
            machinesDao.save(entity);
        }
    }

    public void update(Machines entity) {
        if (entity != null) {
            machinesDao.update(entity);
        }
    }

    public void delete(Machines entity) {
        if (entity != null) {
            machinesDao.delete(entity);
        }
    }

    public void deleteById(Long id) {
        if (id != null) {
            machinesDao.deleteById(id);
        }
    }
}