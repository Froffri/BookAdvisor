package it.unipi.lsmsdb.bookadvisor;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.*;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.*;
import it.unipi.lsmsdb.bookadvisor.model.book.Book;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.user.User;
import it.unipi.lsmsdb.bookadvisor.service.AuthenticationService;
import it.unipi.lsmsdb.bookadvisor.service.BookService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class App extends Application {
    private AuthenticationService authenticationService;
    private BookService bookService;
    private User currentUser;

    @Override
    public void start(Stage primaryStage) {
        // Initialize MongoDB connector, UserDao, and BookDao
        MongoDBConnector connector = MongoDBConnector.getInstance();
        Neo4jConnector neo4jConnector = Neo4jConnector.getInstance();
        UserDao userDao = new UserDao(connector);
        UserGraphDAO userGraphDAO = new UserGraphDAO(neo4jConnector);
        BookDao bookDao = new BookDao(connector);

        // Initialize services
        authenticationService = new AuthenticationService(userDao, userGraphDAO);
        bookService = new BookService(bookDao);

        primaryStage.setTitle("Book Advisor");

        // Create the login, registration, and home tabs
        TabPane tabPane = new TabPane();

        Tab loginTab = createLoginTab();
        Tab registerTab = createRegisterTab();
        Tab homeTab = createHomeTab();

        tabPane.getTabs().addAll(loginTab, registerTab, homeTab);

        homeTab.setDisable(true); // Disable the home tab until the user logs in

        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Tab createLoginTab() {
        Tab tab = new Tab("Login");
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText(), (Node) e.getSource()));

        vbox.getChildren().addAll(new Label("Username"), usernameField, new Label("Password"), passwordField, loginButton);
        tab.setContent(vbox);

        return tab;
    }

    private Tab createRegisterTab() {
        Tab tab = new Tab("Register");
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField genderField = new TextField();
        genderField.setPromptText("Gender");

        DatePicker birthdatePicker = new DatePicker();
        birthdatePicker.setPromptText("Birthdate");

        TextField nationalityField = new TextField();
        nationalityField.setPromptText("Nationality");

        TextField favGenresField = new TextField();
        favGenresField.setPromptText("Favorite Genres (comma separated)");

        TextField spokenLanguagesField = new TextField();
        spokenLanguagesField.setPromptText("Spoken Languages (comma separated)");

        TextField genresField = new TextField();
        genresField.setPromptText("Genres (comma separated)");

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> handleRegister(
                usernameField.getText(),
                passwordField.getText(),
                nameField.getText(),
                genderField.getText(),
                birthdatePicker.getValue(),
                nationalityField.getText(),
                Arrays.asList(favGenresField.getText().split(",")),
                Arrays.asList(spokenLanguagesField.getText().split(",")),
                Arrays.asList(genresField.getText().split(","))
        ));

        vbox.getChildren().addAll(
                new Label("Username"), usernameField,
                new Label("Password"), passwordField,
                new Label("Name"), nameField,
                new Label("Gender"), genderField,
                new Label("Birthdate"), birthdatePicker,
                new Label("Nationality"), nationalityField,
                new Label("Favorite Genres"), favGenresField,
                new Label("Spoken Languages"), spokenLanguagesField,
                new Label("Genres"), genresField,
                registerButton
        );
        tab.setContent(vbox);

        return tab;
    }

    private Tab createHomeTab() {
        Tab tab = new Tab("Home");
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        TextField searchField = new TextField();
        searchField.setPromptText("Search for books by title");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> displayBooks(bookService.findBooksByTitle(searchField.getText()), vbox));

        vbox.getChildren().addAll(new Label("Search"), searchField, searchButton);

        // Display popular books
        List<Book> popularBooks = bookService.getPopularBooks(5);
        vbox.getChildren().add(new Label("Popular Books"));
        displayBooks(popularBooks, vbox);

        tab.setContent(vbox);
        return tab;
    }

    private void displayBooks(List<Book> books, VBox vbox) {
        vbox.getChildren().clear();
        for (Book book : books) {
            VBox bookBox = new VBox(5);
            Label titleLabel = new Label("Title: " + book.getTitle());
            ImageView imageView = new ImageView(new Image(book.getImageUrl()));
            imageView.setFitHeight(100);
            imageView.setFitWidth(80);
            Label ratingLabel = new Label(String.format("Rating: %.2f", (double) book.getSumStars() / book.getNumRatings()));

            bookBox.getChildren().addAll(titleLabel, imageView, ratingLabel);
            bookBox.setOnMouseClicked(e -> displayBookDetails(book));

            vbox.getChildren().add(bookBox);
        }
    }

    private void displayBookDetails(Book book) {
        Stage bookStage = new Stage();
        VBox bookDetailsBox = new VBox(10);
        bookDetailsBox.setPadding(new Insets(10));

        ImageView imageView = new ImageView(new Image(book.getImageUrl()));
        imageView.setFitHeight(200);
        imageView.setFitWidth(160);

        Label titleLabel = new Label("Title: " + book.getTitle());
        Label authorLabel = new Label("Authors: " + String.join(", ", Arrays.asList(book.getAuthors()).stream().map(a -> a.getName()).collect(Collectors.toList())));
        Label genreLabel = new Label("Genres: " + String.join(", ", book.getGenre()));
        Label yearLabel = new Label("Year: " + book.getYear());
        Label languageLabel = new Label("Language: " + book.getLanguage());
        Label ratingLabel = new Label(String.format("Rating: %.2f", (double) book.getSumStars() / book.getNumRatings()));

        bookDetailsBox.getChildren().addAll(imageView, titleLabel, authorLabel, genreLabel, yearLabel, languageLabel, ratingLabel);

        // Display most useful reviews
        Label reviewsLabel = new Label("Most Useful Reviews");
        VBox reviewsBox = new VBox(10);
        List<Review> reviews = book.getMost10UsefulReviews();
        for (Review review : reviews) {
            VBox reviewBox = new VBox(5);
            Label reviewerLabel = new Label("Reviewer: " + review.getNickname());
            Label reviewTextLabel = new Label("Review: " + review.getText());
            Label reviewStarsLabel = new Label("Stars: " + review.getStars());
            reviewBox.getChildren().addAll(reviewerLabel, reviewTextLabel, reviewStarsLabel);
            reviewsBox.getChildren().add(reviewBox);
        }
        bookDetailsBox.getChildren().addAll(reviewsLabel, reviewsBox);

        Scene scene = new Scene(bookDetailsBox, 400, 600);
        bookStage.setScene(scene);
        bookStage.show();
    }

    private void handleLogin(String username, String password, Node sourceNode) {
        // Use AuthenticationService to validate the user credentials
        User user = authenticationService.logIn(username, password);
        if (user != null) {
            System.out.println("Login successful for user: " + user.getNickname());
            currentUser = user;
            // Enable and switch to home tab
            TabPane tabPane = (TabPane) sourceNode.getScene().getRoot();
            tabPane.getTabs().get(2).setDisable(false);
            tabPane.getSelectionModel().select(2);
        } else {
            System.out.println("Login failed for user: " + username);
            // Show an error message to the user
            // You can show an alert here
        }
    }

    private void handleRegister(String username, String password, String name, String gender, LocalDate birthdate, String nationality, List<String> favouriteGenres, List<String> spokenLanguages, List<String> genres) {
        // Use AuthenticationService to register a new user
        if (authenticationService.signUp(username, password, name, gender, birthdate, nationality, favouriteGenres, spokenLanguages, genres)) {
            System.out.println("Registration successful for user: " + username);
        } else {
            System.out.println("Registration failed for user: " + username);
            // Show an error message to the user
            // You can show an alert here
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
