import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Login;
import methods.UserRequests;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.github.javafaker.Faker;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class LoginNegativeTest {
    private final String email;
    private final String password;
    private final int statusCode;
    private final boolean success;
    private final String message;

    private UserRequests userRequests;
    private Login login;

    public LoginNegativeTest(String email, String password, int statusCode, boolean success, String message) {
        this.email = email;
        this.password = password;
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
    }

    @Parameterized.Parameters
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {null, faker.internet().password(), 401, false, "email or password are incorrect"},
                {null, "!nirvana969", 401, false, "email or password are incorrect"},
                {faker.internet().emailAddress(), null, 401, false, "email or password are incorrect"},
                {faker.internet().emailAddress(), "987283467", 401, false, "email or password are incorrect"}
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BURGERS_URL;
        userRequests = new UserRequests();

        // Для негативных тестов используем переданные параметры
        // (они могут быть null или некорректными)
        login = new Login(email, password);

        System.out.println("Негативный тест с email: " + email + ", password: " + password);
    }

    @Test
    @DisplayName("Негативные сценарии логина")
    public void loginUserNegative() {
        // Выполняем логин с некорректными данными
        Response responseLogin = userRequests.loginUser(login);
        responseLogin.then().log().all()
                .statusCode(statusCode)
                .body("success", equalTo(success))
                .body("message", equalTo(message));

        System.out.println("Негативный тест выполнен");
    }
}