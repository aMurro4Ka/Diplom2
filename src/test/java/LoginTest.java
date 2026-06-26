import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Login;
import models.User;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import methods.UserRequests;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class LoginTest {
    private final String email;
    private final String password;
    private final int statusCode;
    private final boolean success;
    private final int createStatusCode;
    private final String message;

    private User user;
    private UserRequests userRequests;
    private Login login;
    private String accessToken;
    private String validEmail;      // Храним email созданного пользователя
    private String validPassword;   // Храним пароль созданного пользователя

    public LoginTest(String email, String password, int statusCode, boolean success, int createStatusCode, String message) {
        this.email = email;
        this.password = password;
        this.statusCode = statusCode;
        this.success = success;
        this.createStatusCode = createStatusCode;
        this.message = message;
    }

    @Parameterized.Parameters
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                // используем данные созданного пользователя
                {null, null, 200, true, 200, null},  // ← Будет подставлен validEmail и validPassword
                {null, faker.internet().password(), 401, false, 200, "email or password are incorrect"},
                {null, "!nirvana969", 401, false, 200, "email or password are incorrect"},
                {faker.internet().emailAddress(), null, 401, false, 200, "email or password are incorrect"},
                {faker.internet().emailAddress(), "987283467", 401, false, 200, "email or password are incorrect"}
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BURGERS_URL;
        userRequests = new UserRequests();

        // Создаём уникального пользователя для каждого теста
        Faker faker = new Faker();
        validEmail = faker.internet().emailAddress();
        validPassword = faker.internet().password(6, 10);
        String validName = faker.name().firstName();

        user = new User(validEmail, validPassword, validName);

        // Создаём пользователя
        Response responseCreate = userRequests.createUser(user);
        responseCreate.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = responseCreate.path("accessToken");
        System.out.println("Создан пользователь: " + validEmail);
        System.out.println("Токен: " + accessToken);

        // Для успешного логина подставляем правильные данные
        String loginEmail = (email == null && statusCode == 200) ? validEmail : email;
        String loginPassword = (password == null && statusCode == 200) ? validPassword : password;

        login = new Login(loginEmail, loginPassword);
    }

    @Test
    @DisplayName("Проверка логина пользователя")
    public void loginUser() {
        // Проверяем, что login не null
        if (login == null) {
            System.out.println("Login объект null, пропускаем тест");
            return;
        }

        Response responseLogin = userRequests.loginUser(login);
        responseLogin.then().log().all()
                .statusCode(statusCode)
                .body("success", equalTo(success));

        if (message != null) {
            responseLogin.then().body("message", equalTo(message));
        }

        System.out.println("Тест выполнен");
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