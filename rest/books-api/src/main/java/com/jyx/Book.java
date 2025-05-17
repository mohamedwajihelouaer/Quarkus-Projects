package com.jyx;

import com.mongodb.client.model.Indexes;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@MongoEntity(collection = "books")
public class Book extends PanacheMongoEntity {
    @NotBlank(message = "Title must not be empty")
    public String title;

    @NotBlank(message = "Author must not be empty")
    public String author;

    @NotNull(message = "Year must not be null")
    public Integer year;

    @Min(value = 0, message = "Price must be non-negative")
    public Double price;

    // Default constructor
    public Book() {}

    // Constructor for convenience
    public Book(String title, String author, Integer year, Double price) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.price = price;
    }

    // performance ?
    // Define indexes
    public static void createIndexes() {
        mongoCollection().createIndex(Indexes.ascending("title"));
        mongoCollection().createIndex(Indexes.ascending("author"));
        mongoCollection().createIndex(Indexes.ascending("year"));
        mongoCollection().createIndex(Indexes.ascending("price"));
    }
}
