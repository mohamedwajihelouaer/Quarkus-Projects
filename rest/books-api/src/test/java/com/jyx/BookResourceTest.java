package com.jyx;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@Testcontainers
class BookResourceTest {

    // Define MongoDB container
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0").withExposedPorts(27017);

    @BeforeAll
    public static void setUpClass() {
        // Set MongoDB connection string for Quarkus
        System.setProperty("mongodb.connection-string", mongoDBContainer.getConnectionString());
    }

    @AfterAll
    public static void tearDownClass() {
        // Clean up system property
        System.clearProperty("mongodb.connection-string");
    }


    String bookJson = "{\"id\":\"64e8b7f2c2a4e12a3c8b4567\",\"title\":\"Test Book\",\"author\":\"Test Author\",\"year\":2023,\"price\":15.99}";

    @BeforeEach
    public void setUp() {
        // Remove all books before each test
        Book.deleteAll();
//        List<Book> fakeBooks = FakeBookGenerator.createFakeBooks(10); // Generate 10 fake books
//        for (Book book : fakeBooks) {
//            book.persist();
//        }
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"The Hobbit\",\"author\":\"J.R.R. Tolkien\",\"year\":1937,\"price\":19.99}")
                .when()
                .post("/books")
                .then()
                .statusCode(201);
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"1984\",\"author\":\"George Orwell\",\"year\":1949,\"price\":14.99}")
                .when()
                .post("/books")
                .then()
                .statusCode(201);
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Dune\",\"author\":\"Frank Herbert\",\"year\":1965,\"price\":24.99}")
                .when()
                .post("/books")
                .then()
                .statusCode(201);
    }

    @AfterEach
    public void tearDown() {
        Book.deleteAll();
    }

    @Test
    public void testGetPaginatedAndSortedBooks() {
        // Test sorting by title in ascending order
        given()
                .queryParam("page", 1)
                .queryParam("size", 2)
                .queryParam("sortBy", "title")
                .queryParam("sortOrder", "asc")
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("books.size()", equalTo(2))
                .body("books[0].title", equalTo("1984"))
                .body("books[1].title", equalTo("Dune"))
                .body("totalItems", equalTo(3))
                .body("totalPages", equalTo(2))
                .body("currentPage", equalTo(1))
                .body("pageSize", equalTo(2));

        // Test sorting by year in descending order
        given()
                .queryParam("page", 1)
                .queryParam("size", 2)
                .queryParam("sortBy", "year")
                .queryParam("sortOrder", "desc")
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("books.size()", equalTo(2))
                .body("books[0].title", equalTo("Dune"))
                .body("books[1].title", equalTo("1984"))
                .body("totalItems", equalTo(3))
                .body("totalPages", equalTo(2))
                .body("currentPage", equalTo(1))
                .body("pageSize", equalTo(2));

        // Test sorting by price in ascending order
        given()
                .queryParam("page", 1)
                .queryParam("size", 2)
                .queryParam("sortBy", "price")
                .queryParam("sortOrder", "asc")
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("books.size()", equalTo(2))
                .body("books[0].title", equalTo("1984"))
                .body("books[1].title", equalTo("The Hobbit"))
                .body("totalItems", equalTo(3))
                .body("totalPages", equalTo(2))
                .body("currentPage", equalTo(1))
                .body("pageSize", equalTo(2));

        // Test invalid sortBy field
        given()
                .queryParam("page", 1)
                .queryParam("size", 2)
                .queryParam("sortBy", "invalidField")
                .queryParam("sortOrder", "asc")
                .when()
                .get("/books")
                .then()
                .statusCode(400);
    }

//    @Test
//    public void testGetPaginatedBooks() {
//        given()
//                .queryParam("page", 1)
//                .queryParam("size", 2)
//                .when()
//                .get("/books")
//                .then()
//                .statusCode(200)
//                .body("books.size()", equalTo(2))
//                .body("books[0].title", equalTo("The Hobbit"))
//                .body("books[1].title", equalTo("1984"))
//                .body("totalItems", equalTo(3))
//                .body("totalPages", equalTo(2))
//                .body("currentPage", equalTo(1))
//                .body("pageSize", equalTo(2));
//
//        given()
//                .queryParam("page", 2)
//                .queryParam("size", 2)
//                .when()
//                .get("/books")
//                .then()
//                .statusCode(200)
//                .body("books.size()", equalTo(1))
//                .body("books[0].title", equalTo("Dune"))
//                .body("totalItems", equalTo(3))
//                .body("totalPages", equalTo(2))
//                .body("currentPage", equalTo(2))
//                .body("pageSize", equalTo(2));
//    }

    @Test
    public void testCreateAndGetBook() {
        // Create a book

        String location = given()
                .body(bookJson)
                .contentType(ContentType.JSON)
                .when()
                .post("/books")
                .then()
                .statusCode(201)
                .body("title", equalTo("Test Book"))
                .extract()
                .header("Location");

        // Get the created book
        given()
                .when()
                .get("/books/64e8b7f2c2a4e12a3c8b4567")
                .then()
                .statusCode(200)
                .body("title", equalTo("Test Book"))
                .body("author", equalTo("Test Author"));
    }

    @Test
    public void testGetAllBooks() {
        given()
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body(is(not(empty())));
    }
}