# Diplom_2
Что сделано:
-Добавлены модели данных: User, Order, Ingredient
-Добавлены классы для работы с API: UserRequests, OrderRequests, RequestSpec
-Добавлен класс с константами: ApiConstants
-Написаны тесты для API
Настроен Maven (pom.xml)
-Добавлены зависимости: JUnit 4, RestAssured, Gson, JavaFaker
-Добавлена библиотека Allure для генерации отчетов
-Настроен плагин maven-surefire-plugin для запуска тестов
Настроен Allure
-Добавлена зависимость allure-junit4
-Добавлена зависимость allure-rest-assured
-Настроен листенер Allure в surefire-plugin
-Добавлены аннотации @DisplayName для понятного отображения тестов в отчете
Написаны параметризованные тесты
-Использован JavaFaker для генерации уникальных тестовых данных
-Настроен @After метод для удаления созданных пользователей после тестов
Сгенерирован отчет Allure
-Отчет создан в папке target/site/allure-maven-plugin/
-В отчете отображаются 33 теста, все успешно пройдены
