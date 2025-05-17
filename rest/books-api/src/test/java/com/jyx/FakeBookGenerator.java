package com.jyx;

import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.List;

public class FakeBookGenerator {
    private static final Faker faker = new Faker();

    public static Book createFakeBook() {
        return new Book(
                faker.book().title(),
                faker.book().author(),
                faker.number().numberBetween(1900, 2025),
                faker.number().randomDouble(2, 5, 100)
        );
    }

    public static List<Book> createFakeBooks(int count) {
        List<Book> books = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            books.add(createFakeBook());
        }
        return books;
    }
}