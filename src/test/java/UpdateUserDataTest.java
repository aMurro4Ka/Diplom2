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
public class UpdateUserDataTest {
    private final String oldEmail;
    private final String newEmail;
    private final String oldPassword;
    private final String newPassword;
    private final String oldName;
    private final String newName;

    private UserRequests userRequests;
    private String accessToken;
    private String actualOldEmail;
    private String actualOldPassword;
    private String actualOldName;

    public UpdateUserDataTest(String oldEmail, String newEmail, String oldPassword, String newPassword, String oldName, String newName) {
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.oldName = oldName;
        this.newName = newName;
    }

    @Parameterized.Parameters
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {null, faker.internet().emailAddress(), null, faker.internet().password(6, 10), null, faker.name().firstName()},
                {null, faker.internet().emailAddress(), null, null, null, null},
                {null, null, null, faker.internet().password(6, 10), null, null},
                {null, null, null, null, null, faker.name().firstName()}
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BURGERS_URL;
        userRequests = new UserRequests();

        Faker faker = new Faker();
        actualOldEmail = faker.internet().emailAddress();
        actualOldPassword = faker.internet().password(6, 10);
        actualOldName = faker.name().firstName();

        User user = new User(actualOldEmail, actualOldPassword, actualOldName);

        Response responseCreate = userRequests.createUser(user);
        responseCreate.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = responseCreate.path("accessToken");
        System.out.println("Создан пользователь: " + actualOldEmail);
        System.out.println("Токен: " + accessToken);
    }

    @Test
    @DisplayName("Проверка изменения данных авторизованного пользователя")
    public void modifyUserDataTest() {
        if (accessToken == null || accessToken.isEmpty()) {
            System.out.println("Токен отсутствует - пропускаем тест");
            return;
        }

        String emailToUpdate = (newEmail != null) ? newEmail : actualOldEmail;
        String passwordToUpdate = (newPassword != null) ? newPassword : actualOldPassword;
        String nameToUpdate = (newName != null) ? newName : actualOldName;

        Response responseGetUser = userRequests.getUser(accessToken);
        responseGetUser.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(actualOldEmail))
                .body("user.name", equalTo(actualOldName));

        User updatedUser = new User(emailToUpdate, passwordToUpdate, nameToUpdate);
        Response responseUpdate = userRequests.updateUser(updatedUser, accessToken);
        responseUpdate.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(emailToUpdate))
                .body("user.name", equalTo(nameToUpdate));

        Response responseGetUserAfterUpdate = userRequests.getUser(accessToken);
        responseGetUserAfterUpdate.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(emailToUpdate))
                .body("user.name", equalTo(nameToUpdate));

        Login login = new Login(emailToUpdate, passwordToUpdate);
        Response responseLogin = userRequests.loginUser(login);
        responseLogin.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true));

        System.out.println("Данные пользователя обновлены");
    }

    @Test
    @DisplayName("Проверка изменения данных неавторизованного пользователя")
    public void modifyUserDataWithoutAuthorizationTest() {
        Faker faker = new Faker();
        User randomUser = new User(
                faker.internet().emailAddress(),
                faker.internet().password(6, 10),
                faker.name().firstName()
        );

        Response responseUpdateWithoutAuthorization = userRequests.updateUserWithoutAuthorization(randomUser);
        responseUpdateWithoutAuthorization.then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));

        System.out.println("Проверка обновления без авторизации пройдена");
    }

    @After
    public void deleteUser() {
        if (accessToken != null && !accessToken.isEmpty()) {
            Response responseDelete = userRequests.deleteUser(accessToken);
            if (responseDelete != null) {
                responseDelete.then().log().all()
                        .statusCode(202)
                        .body("success", equalTo(true));
            }
            System.out.println("Пользователь удалён");
        } else {
            System.out.println("Токен отсутствует - пропускаем удаление");
        }
    }
}