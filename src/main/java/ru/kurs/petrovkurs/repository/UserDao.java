package ru.kurs.petrovkurs.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
import ru.kurs.petrovkurs.model.User;

public class UserDao extends BaseDao<User> {

    public UserDao() {
        super(User.class);
    }

    public User findByUsername(String username) {
        try (Session session = getCurrentSession()) {
            Query<User> query = session.createQuery(
                    "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.role WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean authenticate(String username, String password) {
        try (Session session = getCurrentSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.username = :username AND u.password = :password AND u.enabled = true",
                    User.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            User user = query.uniqueResult();

            if (user != null) {
                // Обновляем время последнего входа
                user.setLastLogin(java.time.LocalDateTime.now());
                update(user);
                System.out.println("Пользователь найден: " + user.getUsername() + " роль: " + user.getRole().getName());
                return true;
            } else {
                System.out.println("Пользователь не найден: " + username);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для проверки существования пользователя
    public boolean userExists(String username) {
        try (Session session = getCurrentSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
            query.setParameter("username", username);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}