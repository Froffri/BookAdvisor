package it.unipi.lsmsdb.bookadvisor;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.*;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.*;
import it.unipi.lsmsdb.bookadvisor.model.book.Book;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.user.Reviewer;
import it.unipi.lsmsdb.bookadvisor.model.user.User;
import it.unipi.lsmsdb.bookadvisor.service.AuthenticationService;
import it.unipi.lsmsdb.bookadvisor.service.BookService;
import it.unipi.lsmsdb.bookadvisor.service.FollowService;
import it.unipi.lsmsdb.bookadvisor.service.UserService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

public class App extends Application {
    private AuthenticationService authenticationService;
    private BookService bookService;
    private Reviewer currentUser;
    private UserDao userDao;
    private ReviewDao reviewDao;
    private boolean searchBooks = true;
    private UserService userService;
    private FollowService followService;

    @Override
    public void start(Stage primaryStage) {
        // Initialize MongoDB connector, UserDao, and BookDao
        MongoDBConnector connector = MongoDBConnector.getInstance();
        Neo4jConnector neo4jConnector = Neo4jConnector.getInstance();
        userDao = new UserDao(connector);
        UserGraphDAO userGraphDAO = new UserGraphDAO(neo4jConnector);
        BookDao bookDao = new BookDao(connector);
        FollowGraphDAO followGraphDAO = new FollowGraphDAO(neo4jConnector);
        reviewDao = new ReviewDao(connector);

        // Initialize services
        authenticationService = new AuthenticationService(userDao, userGraphDAO);
        bookService = new BookService(bookDao);
        followService = new FollowService(followGraphDAO);
        userService = new UserService(userDao, reviewDao, userGraphDAO);

        primaryStage.setTitle("Book Advisor");

        // Create the login, registration, and home tabs
        TabPane tabPane = new TabPane();

        Tab loginTab = createLoginTab();
        Tab registerTab = createRegisterTab();
        Tab homeTab = createHomeTab();

        tabPane.getTabs().addAll(loginTab, registerTab, homeTab);

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
        ScrollPane scrollPane = new ScrollPane(vbox);
        tab.setContent(scrollPane);

        return tab;
    }

    private Tab createHomeTab() {
        Tab tab = new Tab("Home");
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search for books by title or users by nickname");
        Button searchModeButton = new Button("Search Mode: Books");
        searchModeButton.setOnAction(e -> {
            searchBooks = !searchBooks;
            searchModeButton.setText(searchBooks ? "Search Mode: Books" : "Search Mode: Users");
        });

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> handleSearch(searchField.getText(), vbox));

        searchBox.getChildren().addAll(searchField, searchModeButton, searchButton);

        vbox.getChildren().addAll(new Label("Search"), searchBox);

        // Display popular books
        List<Book> popularBooks = bookService.getPopularBooks(5);
        vbox.getChildren().add(new Label("Popular Books"));
        displayBooks(popularBooks, vbox);

        ScrollPane scrollPane = new ScrollPane(vbox);
        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createFeedTab() {
        Tab tab = new Tab("Feed");
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        vbox.getChildren().addAll(new Label("Feed"));

        ScrollPane scrollPane = new ScrollPane(vbox);
        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createProfileTab() {
        Tab tab = new Tab("Profile");
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        if (currentUser != null) {
            Label nameLabel = new Label("Name: " + currentUser.getName());
            Label nicknameLabel = new Label("Nickname: " + currentUser.getNickname());
            Label genderLabel = new Label("Gender: " + currentUser.getGender());
            Label birthdateLabel = new Label("Birthdate: " + currentUser.getBirthdate());
            Label nationalityLabel = new Label("Nationality: " + currentUser.getNationality());

            Button modifyButton = new Button("Modify Info");
            modifyButton.setOnAction(e -> modifyUserInfo());

            vbox.getChildren().addAll(nameLabel, nicknameLabel, genderLabel, birthdateLabel, nationalityLabel, modifyButton);
        }

        ScrollPane scrollPane = new ScrollPane(vbox);
        tab.setContent(scrollPane);
        return tab;
    }

    private void handleSearch(String query, VBox vbox) {
        vbox.getChildren().clear();
        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search for books by title or users by nickname");
        Button searchModeButton = new Button(searchBooks ? "Search Mode: Books" : "Search Mode: Users");
        searchModeButton.setOnAction(e -> {
            searchBooks = !searchBooks;
            searchModeButton.setText(searchBooks ? "Search Mode: Books" : "Search Mode: Users");
        });

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> handleSearch(searchField.getText(), vbox));

        searchBox.getChildren().addAll(searchField, searchModeButton, searchButton);
        vbox.getChildren().addAll(new Label("Search"), searchBox);

        if (searchBooks) {
            List<Book> books = bookService.findBooksByTitle(query);
            displayBooks(books, vbox);
        } else {
            List<User> users = userService.searchUsersByUsername(query);
            displayUsers(users, vbox);
        }
    }

    private void displayBooks(List<Book> books, VBox vbox) {
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

    private void displayUsers(List<User> users, VBox vbox) {
        for (User user : users) {
            if (user instanceof Reviewer) {
                Reviewer reviewer = (Reviewer) user;
                VBox userBox = new VBox(5);
                Label nicknameLabel = new Label("Nickname: " + reviewer.getNickname());
                userBox.getChildren().addAll(nicknameLabel);
                userBox.setOnMouseClicked(e -> displayUserDetails(reviewer));
                vbox.getChildren().add(userBox);
            }
        }
    }

    private void displayUserDetails(Reviewer reviewer) {
        Stage userStage = new Stage();
        VBox userDetailsBox = new VBox(10);
        userDetailsBox.setPadding(new Insets(10));

        Label nameLabel = new Label("Name: " + reviewer.getName());
        Label nicknameLabel = new Label("Nickname: " + reviewer.getNickname());
        Label genderLabel = new Label("Gender: " + reviewer.getGender());
        Label birthdateLabel = new Label("Birthdate: " + reviewer.getBirthdate());
        Label nationalityLabel = new Label("Nationality: " + reviewer.getNationality());
        Label favoriteGenresLabel = new Label("Favorite Genres: " + String.join(", ", reviewer.getFavouriteGenres()));
        Label spokenLanguagesLabel = new Label("Spoken Languages: " + String.join(", ", reviewer.getSpokenLanguages()));

        Button reviewsButton = new Button("Reviews");
        reviewsButton.setOnAction(e -> displayUserReviews(reviewer.getReviewIds()));

        Button followButton = new Button("Follow");
        followButton.setOnAction(e -> followUser(reviewer));

        userDetailsBox.getChildren().addAll(
                nameLabel,
                nicknameLabel,
                genderLabel,
                birthdateLabel,
                nationalityLabel,
                favoriteGenresLabel,
                spokenLanguagesLabel,
                reviewsButton,
                followButton
        );

        Scene scene = new Scene(userDetailsBox, 300, 400);
        userStage.setScene(scene);
        userStage.show();
    }

    private void displayUserReviews(List<ObjectId> reviewIds) {
        Stage reviewStage = new Stage();
        VBox reviewsBox = new VBox(10);
        reviewsBox.setPadding(new Insets(10));

        for (ObjectId reviewId : reviewIds) {
            Review review = reviewDao.findReviewById(reviewId);
            if (review != null) {
                VBox reviewBox = new VBox(5);
                Label reviewerLabel = new Label("Reviewer: " + review.getNickname());
                Label reviewTextLabel = new Label("Review: " + review.getText());
                Label reviewStarsLabel = new Label("Stars: " + review.getStars());

                reviewBox.getChildren().addAll(reviewerLabel, reviewTextLabel, reviewStarsLabel);

                if (currentUser != null && review.getText() != null && !review.getText().isEmpty()) {

                    Label reviewUpVotesLabel = new Label("Upvotes: " + review.getCountUpVote());
                    Label reviewDownVotesLabel = new Label("Downvotes: " + review.getCountDownVote());

                    Button upvoteButton = new Button("Upvote");
                    upvoteButton.setOnAction(e -> handleVote(review, true));

                    Button downvoteButton = new Button("Downvote");
                    downvoteButton.setOnAction(e -> handleVote(review, false));

                    reviewBox.getChildren().addAll(reviewUpVotesLabel, reviewDownVotesLabel, upvoteButton, downvoteButton);
                }

                reviewsBox.getChildren().add(reviewBox);
            }
        }

        Scene scene = new Scene(new ScrollPane(reviewsBox), 400, 600);
        reviewStage.setScene(scene);
        reviewStage.show();
    }

    private void handleVote(Review review, boolean isUpvote) {
        if (currentUser != null) {
            if (isUpvote) {
                userService.voteForReview(currentUser.getId().toString(), review.getId(), true);
            } else {
                userService.voteForReview(currentUser.getId().toString(), review.getId(), false);
            }
        } else {
            System.out.println("You must be logged in to vote.");
            // Show an alert to the user
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login Required");
            alert.setHeaderText(null);
            alert.setContentText("You must be logged in to vote.");
            alert.showAndWait();
        }
    }

    private void followUser(Reviewer reviewer) {
        followService.followUser(currentUser.getId(), reviewer.getId());
        System.out.println("Followed user: " + reviewer.getNickname());
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

        if (reviews == null || reviews.isEmpty()) {
            Label noReviewsLabel = new Label("No reviews found");
            reviewsBox.getChildren().add(noReviewsLabel);
        } else {
            for (Review review : reviews) {
                VBox reviewBox = new VBox(5);
                Label reviewerLabel = new Label("Reviewer: " + review.getNickname());
                Label reviewTextLabel = new Label("Review: " + review.getText());
                Label reviewStarsLabel = new Label("Stars: " + review.getStars());
                reviewBox.getChildren().addAll(reviewerLabel, reviewTextLabel, reviewStarsLabel);
                reviewsBox.getChildren().add(reviewBox);
            }
        }

        bookDetailsBox.getChildren().addAll(reviewsLabel, reviewsBox);

        ScrollPane scrollPane = new ScrollPane(bookDetailsBox);
        Scene scene = new Scene(scrollPane, 400, 600);
        bookStage.setScene(scene);
        bookStage.show();
    }

    private void handleLogin(String username, String password, Node sourceNode) {
        // Use AuthenticationService to validate the user credentials
        Reviewer user = (Reviewer) authenticationService.logIn(username, password);
        if (user != null) {
            System.out.println("Login successful for user: " + user.getNickname());
            currentUser = user;
            // Enable and switch to home tab
            TabPane tabPane = (TabPane) sourceNode.getScene().getRoot();

            // Remove login and register tabs
            tabPane.getTabs().removeIf(tab -> tab.getText().equals("Login") || tab.getText().equals("Register"));

            // Add Feed and Profile tabs
            Tab feedTab = createFeedTab();
            Tab profileTab = createProfileTab();
            tabPane.getTabs().addAll(feedTab, profileTab);

            tabPane.getSelectionModel().select(2); // Switch to home tab
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

    private void modifyUserInfo() {
        Stage modifyStage = new Stage();
        VBox modifyBox = new VBox(10);
        modifyBox.setPadding(new Insets(10));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setOnAction(e -> {
            if (userService.changePassword(currentUser.getId(), newPasswordField.getText())) {
                System.out.println("Password changed successfully.");
            } else {
                System.out.println("Failed to change password.");
            }
            modifyStage.close();
        });

        modifyBox.getChildren().addAll(new Label("New Password"), newPasswordField, changePasswordButton);

        Scene scene = new Scene(modifyBox, 300, 200);
        modifyStage.setScene(scene);
        modifyStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
