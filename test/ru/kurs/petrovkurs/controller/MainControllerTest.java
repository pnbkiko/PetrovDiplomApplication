package ru.kurs.petrovkurs.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest {
    // В HelloControllerTest.java
    @Nested
    @DisplayName("Тесты метода createDateRangeForNotifications")
    class CreateDateRangeForNotificationsTests {

        @Test
        @DisplayName("Метод должен возвращать список из 3 элементов")
        void shouldReturnListWithThreeElements() {
            // Arrange
            LocalDate startDate = LocalDate.of(2023, 12, 25);
            MainController mainController = new MainController();

            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Первый элемент должен быть равен стартовой дате")
        void firstElementShouldBeStartDate() {
            // Arrange
            LocalDate startDate = LocalDate.of(2023, 12, 25);
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertEquals(startDate, result.get(0));
        }

        @Test
        @DisplayName("Второй элемент должен быть стартовая дата + 1 день")
        void secondElementShouldBeStartDatePlusOneDay() {
            // Arrange
            LocalDate startDate = LocalDate.of(2023, 12, 25);
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertEquals(startDate.plusDays(1), result.get(1));
        }

        @Test
        @DisplayName("Третий элемент должен быть стартовая дата + 2 дня")
        void thirdElementShouldBeStartDatePlusTwoDays() {
            // Arrange
            LocalDate startDate = LocalDate.of(2023, 12, 25);
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertEquals(startDate.plusDays(2), result.get(2));
        }

        @Test
        @DisplayName("Должен корректно обрабатывать високосный год")
        void shouldHandleLeapYearCorrectly() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 2, 28);
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertEquals(LocalDate.of(2024, 2, 28), result.get(0));
            assertEquals(LocalDate.of(2024, 2, 29), result.get(1));
            assertEquals(LocalDate.of(2024, 3, 1), result.get(2));
        }

        @Test
        @DisplayName("Должен корректно обрабатывать конец месяца")
        void shouldHandleMonthEndCorrectly() {
            // Arrange
            LocalDate startDate = LocalDate.of(2023, 12, 31);
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertEquals(LocalDate.of(2023, 12, 31), result.get(0));
            assertEquals(LocalDate.of(2024, 1, 1), result.get(1));
            assertEquals(LocalDate.of(2024, 1, 2), result.get(2));
        }

        @Test
        @DisplayName("Элементы должны идти в правильном порядке")
        void elementsShouldBeInCorrectOrder() {
            // Arrange
            LocalDate startDate = LocalDate.now();
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertTrue(result.get(0).isBefore(result.get(1)));
        }

        @Test
        @DisplayName("Элементы не должны идти в обратном порядке")
        void elementsShouldNotBeInReverseOrder() {
            // Arrange
            LocalDate startDate = LocalDate.now();
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertFalse(result.get(0).isAfter(result.get(1)));
        }

        @Test
        @DisplayName("Разница между первым и вторым элементом должна быть 1 день")
        void differenceBetweenFirstAndSecondShouldBeOneDay() {
            // Arrange
            LocalDate startDate = LocalDate.of(2023, 12, 25);
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertEquals(1, java.time.temporal.ChronoUnit.DAYS.between(result.get(0), result.get(1)));
        }

        @Test
        @DisplayName("Разница между вторым и третьим элементом должна быть 1 день")
        void differenceBetweenSecondAndThirdShouldBeOneDay() {
            // Arrange
            LocalDate startDate = LocalDate.of(2023, 12, 25);
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertEquals(1, java.time.temporal.ChronoUnit.DAYS.between(result.get(1), result.get(2)));
        }

        @Test
        @DisplayName("Разница между первым и третьим элементом должна быть 2 дня")
        void differenceBetweenFirstAndThirdShouldBeTwoDays() {
            // Arrange
            LocalDate startDate = LocalDate.of(2023, 12, 25);
            MainController mainController = new MainController();
            // Act
            List<LocalDate> result = mainController.createDateRangeForNotifications(startDate);

            // Assert
            assertEquals(2, java.time.temporal.ChronoUnit.DAYS.between(result.get(0), result.get(2)));
        }
    }


}