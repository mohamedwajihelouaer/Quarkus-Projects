package com.jyx;


import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.util.List;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {


    // Wrapper class for paginated response
    public static class PaginatedBooks {
        public List<Book> books;
        public long totalItems;
        public int totalPages;
        public int currentPage;
        public int pageSize;

        public PaginatedBooks(List<Book> books, long totalItems, int totalPages, int currentPage, int pageSize) {
            this.books = books;
            this.totalItems = totalItems;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
        }
    }

    // GET: Retrieve all books
    @GET
    public PaginatedBooks getBooks(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sortBy") @DefaultValue("title") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("asc") String sortOrder) {
        // Validate page and size
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }

        // Validate sortBy to prevent injection (only allow specific fields)
        if (!List.of("title", "author", "year", "price").contains(sortBy)) {
            throw new WebApplicationException("Invalid sortBy field", Response.Status.BAD_REQUEST);
        }

        // Determine sort direction
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.Descending : Sort.Direction.Ascending;

        // Calculate skip for MongoDB query
        int skip = (page - 1) * size;

        // Fetch paginated and sorted books
        List<Book> books = Book.findAll(Sort.by(sortBy, direction))
                .page(Page.of(page - 1, size))
                .list();

        // Get total count of books
        long totalItems = Book.count();

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalItems / size);

        return new PaginatedBooks(books, totalItems, totalPages, page, size);
    }

    // GET: Retrieve a book by ID
    @GET
    @Path("/{id}")
    public Book getBook(@PathParam("id") String id) {
        Book book = Book.findById(new ObjectId(id));
        if (book == null) {
            throw new WebApplicationException("Book not found", Response.Status.NOT_FOUND);
        }
        return book;
    }

    @POST
    public Response createBook(@Valid Book book) {
        book.persist();
        return Response.status(Response.Status.CREATED)
                .entity(book)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Book updateBook(@PathParam("id") String id, @Valid Book updatedBook) {
        Book book = Book.findById(new ObjectId(id));
        if (book == null) {
            throw new WebApplicationException("Book not found", Response.Status.NOT_FOUND);
        }
        book.title = updatedBook.title;
        book.author = updatedBook.author;
        book.year = updatedBook.year;
        book.price = updatedBook.price;
        book.update();
        return book;
    }

    // DELETE: Delete a book by ID
    @DELETE
    @Path("/{id}")
    public Response deleteBook(@PathParam("id") String id) {
        Book book = Book.findById(new ObjectId(id));
        if (book == null) {
            throw new WebApplicationException("Book not found", Response.Status.NOT_FOUND);
        }
        book.delete();
        return Response.noContent().build();
    }
}
