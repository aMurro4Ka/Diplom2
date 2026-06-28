import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Login;
import models.User;
import methods.UserRequests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;

public class LoginPositiveTest {
    private UserRequests userRequests;
    private User user;
    private Login login;
    private String accessToken;
    private String validEmail;
    private String validPassword;

    @Before
    public void setUp() {
        RestAssured.baseURI = BURGERS_URL;
        userRequests = new UserRequests();

        // Создаём уникального пользователя
        Faker faker = new Faker();
        validEmail = faker.internet().emailAddress();
        validPassword = faker.internet().password(6, 10);
        String validName = faker.name().firstName();

        user = new User(validEmail, validPassword, validName);

        // Создаём пользователя в системе
        Response responseCreate = userRequests.createUser(user);
        responseCreate.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = responseCreate.path("accessToken");
        System.out.println("Создан пользователь: " + validEmail);

        // Подготавливаем данные для логина
        login = new Login(validEmail, validPassword);
    }

    @Test
    @DisplayName("Успешный логин пользователя")
    public void loginUserSuccess() {
        // Выполняем логин
        Response responseLogin = userRequests.loginUser(login);
        responseLogin.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", org.hamcrest.Matchers.notNullValue())
                .body("refreshToken", org.hamcrest.Matchers.notNullValue())
                .body("user.email", org.hamcrest.Matchers.equalTo(validEmail));

        System.out.println("Логин выполнен успешно");
    }

    @After
    public void deleteUser() {
        if (accessToken != null && !accessToken.isEmpty()) {
            Response responseDelete = userRequests.deleteUser(accessToken);
            responseDelete.then().log().all()
                    .statusCode(202)
                    .body("success", equalTo(true));
            System.out.println("Пользователь удалён");
        } else {
            System.out.println("Токен отсутствует — пропускаем удаление");
        }
    }
}