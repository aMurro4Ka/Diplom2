import methods.RequestSpec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import methods.UserRequests;

import java.util.Arrays;
import java.util.Collection;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;


@RunWith(Parameterized.class)
public class CreateUserTest {
    private User user;
    private UserRequests userRequests;
    private String accessToken;
    private Faker faker;

    // Параметры для теста
    private final String email;
    private final String password;
    private final String name;
    private final String testDescription;

    public CreateUserTest(String email, String password, String name, String testDescription) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.testDescription = testDescription;
    }

    @Parameterized.Parameters(name = "{3}")
    public static Collection<Object[]> testData() {
        Faker faker = new Faker();
        return Arrays.asList(new Object[][]{
                {
                        null,  // email = null
                        faker.internet().password(6, 10),
                        faker.name().firstName(),
                        "Создание пользователя без email"
                },
                {
                        faker.internet().emailAddress(),
                        null,  // password = null
                        faker.name().firstName(),
                        "Создание пользователя без password"
                },
                {
                        faker.internet().emailAddress(),
                        faker.internet().password(6, 10),
                        null,  // name = null
                        "Создание пользователя без name"
                }
        });
    }

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

    @Test
    @DisplayName("Создание пользователя без обязательного поля")
    public void createUserWithoutRequiredField() {
        user = new User(email, password, name);

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