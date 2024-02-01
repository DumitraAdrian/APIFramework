package Tests;

import RequestObject.RequestAccount;
import RequestObject.RequestAccountToken;
import ResponseObject.ResponseAccountAuthSuccess;
import ResponseObject.ResponseAccountSuccess;
import ResponseObject.ResponseTokenSuccess;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateUserTest {

    public String userID;
    public String username;
    public String password;
    public String token;

    @Test
    public void testMethod(){

        System.out.println("Step 1 : Create User");
        createUser();
        System.out.println("Step 2 : Generate Token");
        generateToken();
        System.out.println("Step 3 : Obtain new user");
        interractNewUser();

    }

    public  void createUser(){

        //Definim caracteristicile clientului

        RequestSpecification requestSpecification = RestAssured.given();
        requestSpecification.baseUri("https://demoqa.com");
        requestSpecification.contentType("application/json");

        //Configuram requestul

        username = "AdrianDanyel" + System.currentTimeMillis();
        password = "Adrian001!";

//        JSONObject requestbody = new JSONObject();
//        requestbody.put("userName", username);
//        requestbody.put("password", "ghemaR1966!");

        RequestAccount requestAccount = new RequestAccount(username, password);
        requestSpecification.body(requestAccount);

        //Accesam response-ul

        Response response = requestSpecification.post("/Account/v1/User");
//        System.out.println(response.body());
        ResponseBody body = response.getBody();
        body.prettyPrint();

        //Validam statusul requestului

//        System.out.println(response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 201); // principala validare

        //Validam response body

        ResponseAccountSuccess responseAccountSuccess = response.body().as(ResponseAccountSuccess.class);
//        System.out.println(responseAccountSuccess.getUserID());

        Assert.assertNotNull(responseAccountSuccess.getUserID()); // verificam ca exista o valoare pt field
        Assert.assertEquals(responseAccountSuccess.getUsername(), username); // verificam ca username are valoarea din request
        Assert.assertNotNull(responseAccountSuccess.getBooks());

        userID = responseAccountSuccess.getUserID();
    }

    //Facem un request care ne genereaza un token - Autentificare si Autorizare

    public void generateToken(){

        RequestSpecification requestSpecification = RestAssured.given();
        requestSpecification.baseUri("https://demoqa.com");
        requestSpecification.contentType("application/json");

        RequestAccountToken requestAccountToken = new RequestAccountToken(username,password);
        requestSpecification.body(requestAccountToken);

        //Accesam response-ul

        Response response = requestSpecification.post("/Account/v1/GenerateToken");
//        System.out.println(response.body());
        ResponseBody body = response.getBody();
        body.prettyPrint();

        Assert.assertEquals(response.getStatusCode(), 200);

        ResponseTokenSuccess responseTokenSuccess = response.body().as(ResponseTokenSuccess.class);

        Assert.assertNotNull(responseTokenSuccess.getToken()); // verificam ca exista o valoare pt field
        Assert.assertNotNull(responseTokenSuccess.getExpires()); // verificam ca username are valoarea din request
        Assert.assertEquals(responseTokenSuccess.getStatus(),"Success");
        Assert.assertEquals(responseTokenSuccess.getResult(),"User authorized successfully.");

        token = responseTokenSuccess.getToken();
    }


    //Facem un get pentru userul creat

    public void interractNewUser(){

        RequestSpecification requestSpecification = RestAssured.given();
        requestSpecification.baseUri("https://demoqa.com");
        requestSpecification.contentType("application/json");
        requestSpecification.header("Authorization","Bearer "+ token); // autorizarea care foloseste token

        Response response = requestSpecification.get("/Account/v1/User/"+ userID); // compunere de endpoint din url+userID
        Assert.assertEquals(response.getStatusCode(), 200);

        ResponseAccountAuthSuccess responseAccountAuthSuccess = response.body().as(ResponseAccountAuthSuccess.class);

        Assert.assertNotNull(responseAccountAuthSuccess.getUserId()); // verificam ca exista o valoare pt field
        Assert.assertEquals(responseAccountAuthSuccess.getUsername(), username); // verificam ca username are valoarea din request
        Assert.assertNotNull(responseAccountAuthSuccess.getBooks());
    }
}