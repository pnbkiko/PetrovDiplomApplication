package ru.kurs.petrovkurs.repository;

import ru.kurs.petrovkurs.model.Role;

public class RoleDao extends BaseDao<Role> {
    public RoleDao() {
        super(Role.class);
    }
}