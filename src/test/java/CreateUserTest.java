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

public class CreateUserTest {
    private User user;
    private UserRequests userRequests;
    private String accessToken;
    private Faker faker;

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        userRequests = new UserRequests();
        faker = new Faker();
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    public void createUniqueUser() {
        user = new User(
                faker.internet().emailAddress(),
                faker.internet().password(6, 10),
                faker.name().firstName()
        );

        Response responseCreate = userRequests.createUser(user);

        responseCreate.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = responseCreate.jsonPath().getString("accessToken");
    }

    // перенесла тест в CreateDuplicateUserTest


    @Test
    @DisplayName("Создание пользователя без email")
    public void createUserWithoutEmail() {
        user = new User(
                null,
                faker.internet().password(6, 10),
                faker.name().firstName()
        );

        Response responseCreate = userRequests.createUser(user);

        responseCreate.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без password")
    public void createUserWithoutPassword() {
        user = new User(
                faker.internet().emailAddress(),
                null,
                faker.name().firstName()
        );

        Response responseCreate = userRequests.createUser(user);

        responseCreate.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без name")
    public void createUserWithoutName() {
        user = new User(
                faker.internet().emailAddress(),
                faker.internet().password(6, 10),
                null
        );

        Response responseCreate = userRequests.createUser(user);

        responseCreate.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void deleteUser() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userRequests.deleteUser(accessToken);
        }
    }
}