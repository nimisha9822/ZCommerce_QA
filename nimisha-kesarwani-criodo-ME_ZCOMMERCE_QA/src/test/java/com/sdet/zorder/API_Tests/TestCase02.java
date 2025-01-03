package com.sdet.zorder.API_Tests;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestCase02 {
    private String baseURI;
    private String email;
    private int validItemId;
    private int validItemQuantity;
    private String validPaymentType;
    private String validAddress;
    private String orderUrlPost;
    private String orderUrlAll;
    public TestCase02() throws IOException {
        Properties prop = new Properties();
        // Load the properties file
        FileInputStream input = new FileInputStream("src/test/resources/application.properties");
        prop.load(input);
        baseURI = prop.getProperty("baseURI");
        RestAssured.baseURI = baseURI;
        orderUrlPost = prop.getProperty("orderUrlPost");
        orderUrlAll = prop.getProperty("orderUrlAll");
        email = prop.getProperty("email");
        validItemId = Integer.parseInt(prop.getProperty("validItemId"));
        validItemQuantity = Integer.parseInt(prop.getProperty("validItemQuantity"));
        validPaymentType = prop.getProperty("validPaymentType");
        validAddress = prop.getProperty("validAddress");
    }

    public int createOrder(int itemId) throws Exception {
        System.out.println("Creating an order for email " + email);
        RequestSpecification request = RestAssured.given();// Add headers
        request.header("Authorization", "Bearer USER_IMPERSONATE_" + email);
        JSONObject payload = new JSONObject();
        JSONObject itemDetails = new JSONObject();
        payload.put("userId", email);
        payload.put("paymentType", validPaymentType);
        payload.put("shippingAddress", validAddress);
        payload.put("billingAddress", validAddress);
        itemDetails.put("itemId", itemId);
        itemDetails.put("quantity", validItemQuantity);
        JSONArray list = new JSONArray();
        list.put(itemDetails);
        payload.put("orderItemsDetail", list);
        // Set content type and attach payload
        request.contentType(ContentType.JSON);
        request.body(payload.toString());
        // Send POST request
        System.out.println("Sending post request " + payload.toString() + " to " + baseURI + orderUrlPost);
        Response response = request.post(orderUrlPost);
        int statusCode = response.getStatusCode();

        if (statusCode != 200) {
            System.out.println("Unexpected status code: " + statusCode);
            throw new Exception("Unexpected status code: " + statusCode);
        }
        // Parse JSON to get the id value from the data object
        JsonPath jsonPathEvaluator = response.jsonPath();
        int orderId = jsonPathEvaluator.get("data.id");
        System.out.println("ID received from Response: " + orderId);
        return orderId;
    }

    private List<Integer> getAllOrderIds() {
        System.out.println("Getting a list of all order IDs");
        Response response = RestAssured.given()
                .header("Authorization", "Bearer USER_IMPERSONATE_" + email)
                .get(orderUrlAll);
        // Validate the response status
        if (response.getStatusCode() != 200) {

            System.out.println("Failed to fetch orders. Status code: " + response.getStatusCode());
            return null;
        }
        // Parse the response
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> contentList = jsonPath.getList("content");
        // Extract item IDs
        List<Integer> itemIds = new ArrayList<>();
        for (Map<String, Object> content : contentList) {
            List<Map<String, Object>> itemDetailsQuantity = (List<Map<String, Object>>) content
                    .get("itemDetailsQuantity");
            for (Map<String, Object> itemDetail : itemDetailsQuantity) {
                Map<String, Object> item = (Map<String, Object>) itemDetail.get("item");
                itemIds.add((Integer) item.get("id"));
            }
        }
        return itemIds;
    }

    @Test(priority = 2)
    public void addAnOrder() throws Exception {
        try {
            System.out.println("Starting test- Add an order using post");
            int orderId = createOrder(validItemId);
            System.out.println("Test Case addAnOrder success ... Order created! Id: " + orderId);
        } catch (Exception e) {
            System.out.println("Test Case addAnOrder failure");
        }
    }

    @Test(dependsOnMethods = "addAnOrder")
    public void checkOrder() {
        try {
            System.out.println("Starting test- Assert that you can get the same orderId");
            List<Integer> items = getAllOrderIds();
            System.out.println("List of all ordered items: " + items.toString());
            Assert.assertTrue(items.contains(validItemId));
            System.out.println("Test Case checkOrder success");
        } catch (Exception e) {
            System.out.println("Test Case checkOrder failure - " + e.getMessage());
            throw e;
        }
    }
}