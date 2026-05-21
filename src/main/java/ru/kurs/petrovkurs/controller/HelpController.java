package ru.kurs.petrovkurs.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HelpController {

    public static void showHelpWindow() {
        Stage helpStage = new Stage();
        helpStage.initModality(Modality.APPLICATION_MODAL);
        helpStage.setTitle("Справка - Система ТО Машин");
        helpStage.setWidth(1000);
        helpStage.setHeight(700);
        helpStage.setMinWidth(900);
        helpStage.setMinHeight(600);

        try {
            Image icon = new Image(HelpController.class.getResource("/images/icon_main.png").toExternalForm());
            helpStage.getIcons().add(icon);
        } catch (Exception e) {}

        // Основной контейнер
        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: #f0f4f8;");

        // Заголовок
        Label titleLabel = new Label("📚 Справочная система");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 15 20; -fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        mainPane.setTop(titleLabel);

        // TabPane с увеличенными размерами
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #f0f4f8; -fx-padding: 10;");

        // Вкладки
        Tab aboutTab = new Tab("📖 О программе");
        aboutTab.setContent(createAboutContent());
        aboutTab.setClosable(false);

        Tab guideTab = new Tab("📚 Руководство");
        guideTab.setContent(createGuideContent());
        guideTab.setClosable(false);

        Tab rolesTab = new Tab("👥 Роли и права");
        rolesTab.setContent(createRolesContent());
        rolesTab.setClosable(false);

        Tab faqTab = new Tab("❓ FAQ");
        faqTab.setContent(createFaqContent());
        faqTab.setClosable(false);

        Tab shortcutsTab = new Tab("⌨️ Горячие клавиши");
        shortcutsTab.setContent(createShortcutsContent());
        shortcutsTab.setClosable(false);

        tabPane.getTabs().addAll(aboutTab, guideTab, rolesTab, faqTab, shortcutsTab);
        mainPane.setCenter(tabPane);

        // Нижняя панель с кнопкой
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(15, 20, 15, 20));
        bottomBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");

        Button closeButton = new Button("✕ Закрыть");
        closeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 14px;");
        closeButton.setOnAction(e -> helpStage.close());
        bottomBox.getChildren().add(closeButton);

        mainPane.setBottom(bottomBox);

        Scene scene = new Scene(mainPane, 1000, 700);
        helpStage.setScene(scene);
        helpStage.show();
    }

    private static VBox createAboutContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");
        content.setFillWidth(true);

        Label title = new Label("Система управления техническим обслуживанием");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label version = new Label("Версия 2.0.0");
        version.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");

        Text description = new Text(
                "Программа предназначена для автоматизации учета и планирования " +
                        "технического обслуживания (ТО) промышленного оборудования.\n\n" +
                        "Основные функции:\n" +
                        "• Учет станков и оборудования\n" +
                        "• Ведение журнала актов ТО\n" +
                        "• Планирование графика ТО\n" +
                        "• Автоматическое уведомление о предстоящих работах\n" +
                        "• Формирование отчетов в PDF\n" +
                        "• Календарь ТО с цветовой индикацией\n" +
                        "• Разграничение прав доступа"
        );
        description.setStyle("-fx-font-size: 14px; -fx-fill: #34495e;");

        TextFlow textFlow = new TextFlow(description);
        textFlow.setStyle("-fx-padding: 10 0;");

        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-background-color: #e8f4f8; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-border-width: 1;");

        Label infoTitle = new Label("💡 Информация");
        infoTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9; -fx-font-size: 14px;");

        Label developer = new Label("Разработчик: Петров Курс");
        developer.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");

        Label support = new Label("Техническая поддержка: support@example.com");
        support.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");

        infoBox.getChildren().addAll(infoTitle, developer, support);

        Label copyright = new Label("© 2025 ТО Машин. Все права защищены.");
        copyright.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-padding: 10 0 0 0;");

        content.getChildren().addAll(title, version, separator, textFlow, infoBox, copyright);
        VBox.setVgrow(content, Priority.ALWAYS);

        return content;
    }

    private static VBox createGuideContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label header = new Label("Руководство пользователя");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");

        VBox guideContent = new VBox(15);
        guideContent.setStyle("-fx-padding: 10;");

        // Создаем секции с видимыми границами
        guideContent.getChildren().addAll(
                createGuideSection("1. Добавление станка",
                        "• Нажмите на раздел 'Станки' в левом меню\n" +
                                "• Нажмите 'Правка → Добавить' или дважды кликните по таблице\n" +
                                "• Заполните все обязательные поля (отмечены звездочкой *)\n" +
                                "• Нажмите 'Сохранить' для добавления станка в систему"),

                createGuideSection("2. Добавление акта ТО",
                        "• Перейдите в раздел 'Акты ТО'\n" +
                                "• Нажмите 'Правка → Добавить'\n" +
                                "• Выберите станок и вид ТО из выпадающих списков\n" +
                                "• Укажите дату выполнения, инженера и замечания\n" +
                                "• Подтвердите выполнение ТО (галочка)\n" +
                                "• Нажмите 'Сохранить'"),

                createGuideSection("3. Планирование ТО",
                        "• Перейдите в раздел 'График ТО'\n" +
                                "• Нажмите 'Правка → Добавить'\n" +
                                "• Выберите станок и вид ТО\n" +
                                "• Укажите дату последнего выполненного ТО\n" +
                                "• Система автоматически рассчитает дату следующего ТО\n" +
                                "• Нажмите 'Сохранить'"),

                createGuideSection("4. Просмотр уведомлений",
                        "• Нажмите на кнопку 'Уведомления' в левом меню\n" +
                                "• Отобразится список предстоящих ТО на ближайшие 3 дня\n" +
                                "• Цветовая индикация показывает срочность:\n" +
                                "   🔴 Красный - просрочено\n" +
                                "   🟡 Желтый - сегодня\n" +
                                "   🔵 Голубой - завтра\n" +
                                "   🟢 Зеленый - послезавтра"),

                createGuideSection("5. Формирование PDF отчета",
                        "• Нажмите на кнопку 'PDF отчет' в левом меню\n" +
                                "• Выберите место сохранения файла\n" +
                                "• Отчет о просроченных ТО будет создан автоматически\n" +
                                "• Отчет можно просмотреть или распечатать"),

                createGuideSection("6. Календарь ТО",
                        "• Нажмите на кнопку 'Календарь' в левом меню\n" +
                                "• Выберите нужный месяц с помощью стрелок\n" +
                                "• Цветные индикаторы показывают наличие ТО\n" +
                                "• Нажмите на день с индикатором для просмотра деталей")
        );

        scrollPane.setContent(guideContent);
        content.getChildren().addAll(header, separator, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return content;
    }

    private static VBox createGuideSection(String title, String contentText) {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 1;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label contentLabel = new Label(contentText);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #495057; -fx-wrap-text: true;");
        contentLabel.setWrapText(true);

        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
    }

    private static VBox createRolesContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label header = new Label("Роли пользователей и права доступа");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");

        VBox rolesContent = new VBox(15);
        rolesContent.setStyle("-fx-padding: 10;");

        // Таблица ролей - используем VBox для лучшего отображения
        VBox adminBox = createRoleCard("👑 Администратор", "#e74c3c",
                "Полный доступ ко всем функциям системы",
                "✓ Просмотр всех данных\n✓ Добавление и редактирование\n✓ Удаление записей\n✓ Управление пользователями\n✓ Формирование отчетов");

        VBox engineerBox = createRoleCard("🔧 Инженер", "#f39c12",
                "Добавление и редактирование данных (без удаления)",
                "✓ Просмотр всех данных\n✓ Добавление актов ТО\n✓ Добавление записей в график\n✓ Редактирование своих записей\n✓ Формирование отчетов\n✗ Удаление записей");

        VBox operatorBox = createRoleCard("👁 Оператор", "#3498db",
                "Только просмотр данных",
                "✓ Просмотр станков\n✓ Просмотр актов ТО\n✓ Просмотр графика ТО\n✓ Просмотр отчетов\n✓ Просмотр календаря\n✗ Редактирование\n✗ Удаление");

        rolesContent.getChildren().addAll(adminBox, engineerBox, operatorBox);
        scrollPane.setContent(rolesContent);

        // Дополнительная информация
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: #e8f4f8; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-border-width: 1;");

        Label infoTitle = new Label("🔐 Важно знать");
        infoTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9; -fx-font-size: 14px;");

        Label infoText = new Label(
                "• Администратор имеет неограниченный доступ ко всем функциям системы\n" +
                        "• Инженер может добавлять и редактировать данные, но не может их удалять\n" +
                        "• Оператор может только просматривать информацию без возможности изменений"
        );
        infoText.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px; -fx-wrap-text: true;");

        infoBox.getChildren().addAll(infoTitle, infoText);

        content.getChildren().addAll(header, separator, scrollPane, infoBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return content;
    }

    private static VBox createRoleCard(String title, String color, String description, String permissions) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 1;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #dee2e6;");

        Label permsLabel = new Label(permissions);
        permsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #495057;");
        permsLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, descLabel, sep, permsLabel);
        return card;
    }

    private static VBox createFaqContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label header = new Label("Часто задаваемые вопросы");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");

        VBox faqContent = new VBox(12);
        faqContent.setStyle("-fx-padding: 10;");

        faqContent.getChildren().addAll(
                createFaqItem("Как добавить новый станок?",
                        "Перейдите в раздел 'Станки' → 'Правка' → 'Добавить'. Заполните все обязательные поля и нажмите 'Сохранить'."),

                createFaqItem("Как отметить выполненное ТО?",
                        "Перейдите в 'Акты ТО' → 'Добавить'. Заполните акт о выполненном ТО. Система автоматически обновит график."),

                createFaqItem("Почему я не могу удалить запись?",
                        "Возможно, у вашей учетной записи недостаточно прав. Только администратор может удалять записи."),

                createFaqItem("Как просмотреть предстоящие ТО?",
                        "Нажмите на кнопку 'Уведомления' в левом меню или откройте 'Календарь'."),

                createFaqItem("Как сформировать отчет о просроченных ТО?",
                        "Нажмите на кнопку 'PDF отчет' в меню. Отчет сохранится в выбранную папку."),

                createFaqItem("Что означают цвета в графике ТО?",
                        "Красный - просрочено, желтый - сегодня, голубой - завтра, зеленый - послезавтра."),

                createFaqItem("Как обновить данные после добавления?",
                        "Данные обновляются автоматически. Просто переключитесь на другую вкладку и вернитесь обратно."),

                createFaqItem("Можно ли редактировать существующие записи?",
                        "Да, дважды кликните по любой записи в таблице или выберите ее и нажмите 'Правка'.")
        );

        scrollPane.setContent(faqContent);
        content.getChildren().addAll(header, separator, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return content;
    }

    private static VBox createFaqItem(String question, String answer) {
        VBox item = new VBox(8);
        item.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 1;");

        Label questionLabel = new Label("❓ " + question);
        questionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

        Label answerLabel = new Label("📌 " + answer);
        answerLabel.setStyle("-fx-text-fill: #495057; -fx-font-size: 13px; -fx-wrap-text: true;");
        answerLabel.setWrapText(true);

        item.getChildren().addAll(questionLabel, answerLabel);
        return item;
    }

    private static VBox createShortcutsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label header = new Label("Горячие клавиши");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");

        VBox shortcutsContent = new VBox(10);
        shortcutsContent.setStyle("-fx-padding: 10;");

        // Создаем сетку с явными границами
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 1;");

        // Заголовки с фоном
        Label keyHeader = new Label(" Комбинация ");
        keyHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 8 15; -fx-background-color: #e9ecef; -fx-background-radius: 5;");

        Label actionHeader = new Label(" Действие ");
        actionHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 8 15; -fx-background-color: #e9ecef; -fx-background-radius: 5;");

        Label descHeader = new Label(" Описание ");
        descHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 8 15; -fx-background-color: #e9ecef; -fx-background-radius: 5;");

        grid.add(keyHeader, 0, 0);
        grid.add(actionHeader, 1, 0);
        grid.add(descHeader, 2, 0);

        String[][] shortcuts = {
                {"Ctrl + N", "Новая запись", "Создание нового документа"},
                {"Ctrl + S", "Сохранить", "Быстрое сохранение"},
                {"Delete", "Удалить", "Удаление (только админ)"},
                {"Ctrl + F", "Поиск", "Фокус на поле поиска"},
                {"Esc", "Закрыть", "Закрытие диалога"},
                {"F1", "Справка", "Вызов этой справки"},
                {"Ctrl + P", "PDF отчет", "Экспорт в PDF"},
                {"Enter", "Подтвердить", "Быстрое подтверждение"},
                {"Tab", "Навигация", "Перемещение между полями"}
        };

        for (int i = 0; i < shortcuts.length; i++) {
            Label keyLabel = new Label(" ⌨️ " + shortcuts[i][0] + " ");
            keyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db; -fx-padding: 8 15; -fx-background-color: #e8f4f8; -fx-background-radius: 5;");

            Label actionLabel = new Label(" " + shortcuts[i][1] + " ");
            actionLabel.setStyle("-fx-text-fill: #2c3e50; -fx-padding: 8 15; -fx-background-color: #ffffff; -fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 5;");

            Label descLabel = new Label(" " + shortcuts[i][2] + " ");
            descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-padding: 8 15;");

            grid.add(keyLabel, 0, i + 1);
            grid.add(actionLabel, 1, i + 1);
            grid.add(descLabel, 2, i + 1);
        }

        shortcutsContent.getChildren().add(grid);

        // Советы
        VBox tipsBox = new VBox(10);
        tipsBox.setStyle("-fx-background-color: #e8f4f8; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-border-width: 1;");

        Label tipsTitle = new Label("💡 Полезные советы");
        tipsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9; -fx-font-size: 14px;");

        Label tipsText = new Label(
                "• Двойной клик по записи в таблице открывает форму редактирования\n" +
                        "• Используйте поиск для быстрого нахождения нужной информации\n" +
                        "• Фильтр по дате поможет найти акты ТО за конкретный день\n" +
                        "• Цветные индикаторы в календаре показывают наличие ТО"
        );
        tipsText.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px; -fx-wrap-text: true;");
        tipsText.setWrapText(true);

        tipsBox.getChildren().addAll(tipsTitle, tipsText);
        shortcutsContent.getChildren().add(tipsBox);

        scrollPane.setContent(shortcutsContent);
        content.getChildren().addAll(header, separator, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return content;
    }
}