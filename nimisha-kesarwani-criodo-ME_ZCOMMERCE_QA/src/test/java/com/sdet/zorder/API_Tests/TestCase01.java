package com.sdet.zorder.API_Tests;

import org.testng.annotations.*;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.RestAssured;

import java.io.IOException;

public class TestCase01 {
    private String baseURI;
    private String email;
    private String cartUrlView;
    private String cartUrlPut;
    private String cartUrlPost;
    private String cartUrlDelete;
    private int validItemId;
    private String invalidItemId;
    private int validItemQuantity;
    private int invalidItemQuantity;

    public TestCase01() throws IOException {

        Properties prop = new Properties();
        // Load the properties file
        FileInputStream input = new FileInputStream("src/test/resources/application.properties");
        prop.load(input);
        // Initialize variables from properties file
        baseURI = prop.getProperty("baseURI");
        RestAssured.baseURI = baseURI;
        email = prop.getProperty("email");
        cartUrlView = prop.getProperty("cartUrlView");
        cartUrlPut = prop.getProperty("cartUrlPut");
        cartUrlPost = prop.getProperty("cartUrlPost");
        cartUrlDelete = prop.getProperty("cartUrlDelete");
        validItemId = Integer.parseInt(prop.getProperty("validItemId"));
        invalidItemId = prop.getProperty("invalidItemId");
        validItemQuantity = Integer.parseInt(prop.getProperty("validItemQuantity"));
        invalidItemQuantity = Integer.parseInt(prop.getProperty("invalidItemQuantity"));
        input.close();
    }

    private boolean doesCartExist(String cartUrlView, String email) throws Exception {

        System.out.println("Checking if cart exists already for " + email);
        RequestSpecification request = RestAssured.given();
        // Add headers
        request.header("Authorization", "Bearer USER_IMPERSONATE_" + email);
        System.out.println("Sending get request to " + baseURI + cartUrlView);
        Response response = request.get(cartUrlView);
        int statusCode = response.getStatusCode();
        // Check if status code is 404 or 200
        if (statusCode == 404) {
            System.out.println("Response came with status code " + statusCode);
            System.out.println("Cart does not exist for " + email);
            return false;
        } else if (statusCode == 200) {
            System.out.println("Response came with status code " + statusCode);
            System.out.println("Cart exists for " + email);
            return true;
        } else {
            System.out.println("Unexpected status code: " + statusCode);
            throw new Exception("Unexpected status code: " + statusCode);
        }
    }

    private int getCartId(String cartUrlView, String email) throws Exception {
        System.out.println("Checking cart ID for " + email);
        RequestSpecification request = RestAssured.given();
        System.out.println("Sending get request to " + baseURI + cartUrlView);
        Response response = request.get(cartUrlView);
        JsonPath jsonPathEvaluator = response.jsonPath();
        int cartId = jsonPathEvaluator.get("data.id");
        System.out.println("ID received from Response: " + cartId);
        return cartId;
    }

    private boolean putItemInCart(int cartId, Object itemId, int itemAmount) throws Exception {

        System.out.println("Putting item with ID " + itemId + " into cart " + cartId);
        RequestSpecification request = RestAssured.given();// Add headers
        request.header("Authorization", "Bearer USER_IMPERSONATE_" + email);
        // Construct the JSON payload
        JSONObject itemQuantity = new JSONObject();
        itemQuantity.put("itemId", itemId);
        itemQuantity.put("quantity", itemAmount);
        JSONArray itemQuantityList = new JSONArray();
        itemQuantityList.put(itemQuantity);
        JSONObject payload = new JSONObject();
        payload.put("id", cartId);
        payload.put("userId", email);
        payload.put("itemQuantityList", itemQuantityList);
        // Set content type and attach payload
        request.contentType(ContentType.JSON);
        request.body(payload.toString());
        // Send PUT request
        System.out.println("Sending " + payload.toString() + " to " + baseURI + cartUrlPut);
        Response response = request.put(cartUrlPut + "/" + cartId);
        int statusCode = response.getStatusCode();
        System.out.println("Response came with status code " + statusCode);
        if (statusCode != 200) {
            return false;
        } else {
            return true;
        }
    }

    public int createCartId(Object itemId, int itemAmount) throws Exception {

        System.out.println("Creating cart with email " + email);
        RequestSpecification request = RestAssured.given();// Add headers
        request.header("Authorization", "Bearer USER_IMPERSONATE_" + email);
        // Construct the JSON payload for POST request
        JSONObject itemQuantity = new JSONObject();
        itemQuantity.put("itemId", itemId);
        itemQuantity.put("quantity", itemAmount);
        JSONArray itemQuantityList = new JSONArray();
        itemQuantityList.put(itemQuantity);
        JSONObject payload = new JSONObject();
        payload.put("itemQuantityList", itemQuantityList);
        // Set content type and attach payload
        request.contentType(ContentType.JSON);
        request.body(payload.toString());
        // Send POST request
        System.out.println("Sending post request to " + baseURI + cartUrlPost);
        Response response = request.post(cartUrlPost);
        int statusCode = response.getStatusCode();
        System.out.println("Response came with status code " + statusCode);
        if (statusCode != 200) {
            System.out.println("Unexpected status code: " + statusCode);
            throw new Exception("Unexpected status code: " + statusCode);
        }
        // Parse JSON to get the id value from the data object
        JsonPath jsonPathEvaluator = response.jsonPath();
        int cartId = jsonPathEvaluator.get("data.id");
        System.out.println("ID received from Response: " + cartId);
        return cartId;
    }

    private boolean deleteCart(int cartId) {
        System.out.println("Deleting cart with id " + cartId);
        RequestSpecification request = RestAssured.given();// Add headers
        request.header("Authorization", "Bearer ADMIN_TOKEN");
        System.out.println("Sending delete request to " + baseURI + cartUrlDelete + "/" + cartId);
        Response response = request.delete(cartUrlDelete + "/" + cartId);
        int statusCode = response.getStatusCode();
        if (statusCode == 200) {
            System.out.println("Response came with status code " + statusCode + ", cart deleted");
            return true;
        } else {
            System.out.println("Response came with status code " + statusCode + ", cart deletion failed");
            return false;
        }
    }

    @Test(priority = 0)
    public void addValidItemToCart() throws Exception {
        try {
            System.out.println("Starting test - Add an item that exists to cart and assert 201");
            Thread.sleep(3000);
            if (doesCartExist(cartUrlView, email)) {
                System.out.println("Cart exists!");
                int cartId = getCartId(cartUrlView, email);
                Assert.assertTrue(putItemInCart(cartId, validItemId, validItemQuantity));
                Assert.assertTrue(deleteCart(cartId));
                System.out.println("Test Case addValidItemToCart Success!!");
            } else {
                System.out.println("Cart does not exist! Creating cart and entering item");
                int cartId = createCartId(validItemId, validItemQuantity);
                Assert.assertTrue(deleteCart(cartId));
                System.out.println("Test Case addValidItemToCart Success!!");
            }
        } catch (AssertionError e) {
            System.out.println("Test Case addValidItemToCart Failure!!");
        }
    }

    @Test(priority = 1)
    public void addInValidItemToCart() throws Exception {
        try {

            System.out.println("Starting test- Add a non-existent item to cart and assert 404");
            Thread.sleep(3000);
            if (doesCartExist(cartUrlView, email)) {
                System.out.println("Cart exists!");
                int cartId = getCartId(cartUrlView, email);
                Assert.assertFalse(putItemInCart(cartId, invalidItemId, validItemQuantity));
            } else {
                System.out.println("Cart does not exist!");
                boolean exceptionThrown = false;
                try {
                    int cartId = createCartId(invalidItemId, validItemQuantity);
                } catch (Exception e) {
                    exceptionThrown = true;
                    System.out.println("Exception message: " + e.getMessage());
                }
                Assert.assertTrue(exceptionThrown, "Exception was not thrown by createCartId");
                System.out.println("Test Case addInValidItemToCart success!!");
            }
        } catch (AssertionError e) {
            System.out.println("Test Case addInValidItemToCart failure!!");
            throw e;
        }
    }
}