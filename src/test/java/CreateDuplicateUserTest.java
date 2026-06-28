import methods.RequestSpec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import methods.UserRequests;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;

public class CreateDuplicateUserTest {
    private User user;
    private UserRequests userRequests;
    private String accessToken;
    private Faker faker = new Faker();

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        user = new User(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().firstName()
        );
        userRequests = new UserRequests();
    }

    @Test
    @DisplayName("Проверка невозможности создания дубликата существующего пользователя")
    public void createDuplicateUser() {
        // Шаг 1: Создаем пользователя
        Response responseCreate = userRequests.createUser(user);
        responseCreate.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true));

        // Сохраняем токен для последующего удаления
        accessToken = responseCreate.then().extract().path("accessToken");

        // Шаг 2: Пытаемся создать пользователя с теми же данными
        Response responseCreateDouble = userRequests.createUser(user);
        responseCreateDouble.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @After
    public void deleteUser() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userRequests.deleteUser(accessToken);
        }
    }
}