package ru.kurs.petrovkurs.service;

import ru.kurs.petrovkurs.model.User;
import ru.kurs.petrovkurs.repository.UserDao;

public class AuthService {
    private static AuthService instance;
    private UserDao userDao = new UserDao();
    private User currentUser;

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        System.out.println("Попытка входа: " + username);

        // Проверяем существует ли пользователь
        if (!userDao.userExists(username)) {
            System.out.println("Пользователь не существует: " + username);
            return false;
        }

        boolean authenticated = userDao.authenticate(username, password);
        if (authenticated) {
            currentUser = userDao.findByUsername(username);
            System.out.println("Успешный вход! Пользователь: " + currentUser.getUsername() +
                    ", Роль: " + currentUser.getRole().getName());
        } else {
            System.out.println("Ошибка аутентификации: неверный пароль для " + username);
        }
        return authenticated;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean hasRole(String roleName) {
        return currentUser != null && currentUser.getRole() != null &&
                currentUser.getRole().getName().equals(roleName);
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isEngineer() {
        return hasRole("ROLE_ENGINEER");
    }

    public boolean isOperator() {
        return hasRole("ROLE_OPERATOR");
    }

    public boolean canDelete() {
        return isAdmin();
    }

    public boolean canEdit() {
        return isAdmin() || isEngineer();
    }

    public boolean canAdd() {
        return isAdmin() || isEngineer();
    }

    public boolean canView() {
        return isAuthenticated();
    }

    public boolean canEditMaintenance() {
        return isAdmin() || isEngineer();
    }

    public boolean canDeleteMaintenance() {
        return isAdmin();
    }

    public String getUserDisplayName() {
        if (currentUser == null) return "Гость";
        return currentUser.getFullName() + " (" + getRoleDisplayName() + ")";
    }

    public String getRoleDisplayName() {
        if (currentUser == null || currentUser.getRole() == null) return "";
        String roleName = currentUser.getRole().getName();
        switch (roleName) {
            case "ROLE_ADMIN": return "Администратор";
            case "ROLE_ENGINEER": return "Инженер";
            case "ROLE_OPERATOR": return "Оператор";
            default: return roleName;
        }
    }
}