package it.unipi.lsmsdb.bookadvisor;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.*;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.*;
import it.unipi.lsmsdb.bookadvisor.model.book.Book;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.user.Admin;
import it.unipi.lsmsdb.bookadvisor.model.user.Author;
import it.unipi.lsmsdb.bookadvisor.model.user.Reviewer;
import it.unipi.lsmsdb.bookadvisor.model.user.User;
import it.unipi.lsmsdb.bookadvisor.service.AuthenticationService;
import it.unipi.lsmsdb.bookadvisor.service.BookService;
import it.unipi.lsmsdb.bookadvisor.service.FollowService;
import it.unipi.lsmsdb.bookadvisor.service.ReviewService;
import it.unipi.lsmsdb.bookadvisor.service.UserService;
import it.unipi.lsmsdb.bookadvisor.utils.HashingUtility;
import it.unipi.lsmsdb.bookadvisor.Procedures;
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
import javafx.collections.FXCollections;
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
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
    private ReviewService reviewService;
    private Stage primaryStage;
    private Procedures procedures;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

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
        bookService = new BookService(bookDao, userDao, new BookGraphDAO(neo4jConnector));
        followService = new FollowService(followGraphDAO);
        userService = new UserService(userDao, reviewDao, userGraphDAO);
        reviewService = new ReviewService(reviewDao, new ReviewGraphDAO(neo4jConnector), bookDao);
        procedures = new Procedures(neo4jConnector, connector);

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
    
        ComboBox<String> genderComboBox = new ComboBox<>();
        genderComboBox.setItems(FXCollections.observableArrayList("F", "M", "Other"));
        genderComboBox.setPromptText("Gender");
    
        DatePicker birthdatePicker = new DatePicker();
        birthdatePicker.setPromptText("Birthdate");
    
        ComboBox<String> nationalityComboBox = new ComboBox<>();
        nationalityComboBox.setItems(FXCollections.observableArrayList(
            "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica (the territory South of 60 deg S)", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Bouvet Island (Bouvetoya)", "Brazil", "British Indian Ocean Territory (Chagos Archipelago)", "British Virgin Islands", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "French Guiana", "French Polynesia", "French Southern Territories", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guernsey", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Heard Island and McDonald Islands", "Holy See (Vatican City State)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica", "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea", "Kuwait", "Kyrgyz Republic", "Lao People's Democratic Republic", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macao", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Macedonia", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Palestinian Territory", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda", "Saint Barthelemy", "Saint Helena", "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin", "Saint Pierre and Miquelon", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia (Slovak Republic)", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard & Jan Mayen Islands", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States Minor Outlying Islands", "United States Virgin Islands", "United States of America", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Wallis and Futuna", "Western Sahara", "Yemen", "Zambia", "Zimbabwe"
        ));
        nationalityComboBox.setPromptText("Nationality");

        CheckComboBox<String> favGenresCheckComboBox = new CheckComboBox<>(FXCollections.observableArrayList(
            "biography", "children", "comics", "crime", "fantasy", "fiction", "graphic", "historical fiction", "history", "mystery", "non-fiction", "paranormal", "poetry", "romance", "thriller", "young-adult"
        ));

        CheckComboBox<String> spokenLanguagesCheckComboBox = new CheckComboBox<>(FXCollections.observableArrayList(
            "--", "abk", "ady", "afr", "ale", "amh", "apa", "ara", "arg", "arw", "aus", "ave", "aze", "ben", "bos", "bul", "cat", "cze", "dan", "dum", "egy", "elx", "en", "en-CA", "en-GB", "en-US", "eng", "enm", "epo", "es-MX", "est", "eus", "fil", "fin", "fre", "frm", "fro", "frs", "ger", "gle", "glg", "gmh", "grc", "gre", "guj", "heb", "hin", "hun", "hye", "iba", "ind", "ira", "isl", "ita", "jpn", "kam", "kan", "kat", "kor", "kur", "lat", "lav", "lit", "mal", "mar", "mkd", "mlt", "mon", "msa", "mul", "mus", "myn", "nep", "nl", "nld", "nno", "nob", "nor", "pan", "peo", "per", "pes", "phi", "pol", "por", "pra", "pt-BR", "raj", "rum", "rup", "rus", "sco", "scr", "sin", "slo", "slv", "snd", "spa", "sqi", "srp", "swe", "tam", "tel", "tgl", "tha", "tlh", "tur", "ukr", "und", "urd", "vie", "vls", "wak", "zho"
        ));

        CheckComboBox<String> genresCheckComboBox = new CheckComboBox<>(FXCollections.observableArrayList(
            "biography", "children", "comics", "crime", "fantasy", "fiction", "graphic", "historical fiction", "history", "mystery", "non-fiction", "paranormal", "poetry", "romance", "thriller", "young-adult"
        ));

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> handleRegister(
                usernameField.getText(),
                passwordField.getText(),
                nameField.getText(),
                genderComboBox.getValue(),
                birthdatePicker.getValue(),
                nationalityComboBox.getValue(),
                favGenresCheckComboBox.getCheckModel().getCheckedItems(),
                spokenLanguagesCheckComboBox.getCheckModel().getCheckedItems(),
                genresCheckComboBox.getCheckModel().getCheckedItems()
        ));

        vbox.getChildren().addAll(
                new Label("Username"), usernameField,
                new Label("Password"), passwordField,
                new Label("Name"), nameField,
                new Label("Gender"), genderComboBox,
                new Label("Birthdate"), birthdatePicker,
                new Label("Nationality"), nationalityComboBox,
                new Label("Favorite Genres"), favGenresCheckComboBox,
                new Label("Spoken Languages"), spokenLanguagesCheckComboBox,
                new Label("Genres"), genresCheckComboBox,
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
    
        Button findMostFamousBookButton = new Button("Find most famous book by genre");
        findMostFamousBookButton.setOnAction(e -> openFindMostFamousBookWindow());
    
        searchBox.getChildren().addAll(searchField, searchModeButton, searchButton, findMostFamousBookButton);
    
        vbox.getChildren().addAll(new Label("Search"), searchBox);
    
        // Display popular books
        List<Book> popularBooks = bookService.getPopularBooks(5);
        vbox.getChildren().add(new Label("Popular Books"));
        displayBooks(popularBooks, vbox);
    
        ScrollPane scrollPane = new ScrollPane(vbox);
        tab.setContent(scrollPane);
        return tab;
    }
    
    private void openFindMostFamousBookWindow() {
        Stage genreStage = new Stage();
        VBox genreBox = new VBox(10);
        genreBox.setPadding(new Insets(10));
    
        ComboBox<String> genreComboBox = new ComboBox<>();
        genreComboBox.setItems(FXCollections.observableArrayList(
            "biography", "children", "comics", "crime", "fantasy", "fiction", "graphic", "historical fiction",
            "history", "mystery", "non-fiction", "paranormal", "poetry", "romance", "thriller", "young-adult"
        ));
        genreComboBox.setPromptText("Select Genre");
    
        Button findButton = new Button("Find");
        findButton.setOnAction(e -> {
            String selectedGenre = genreComboBox.getValue();
            if (selectedGenre != null) {
                List<Document> results = procedures.findMostFamousBooks(selectedGenre);
                displayMostFamousBooks(results);
                genreStage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select a genre.");
                alert.showAndWait();
            }
        });
    
        genreBox.getChildren().addAll(new Label("Select a genre to find the most famous book in each country:"), genreComboBox, findButton);
    
        Scene genreScene = new Scene(genreBox, 300, 200);
        genreStage.setScene(genreScene);
        genreStage.setTitle("Find Most Famous Book by Genre");
        genreStage.show();
    }
    
    private void displayMostFamousBooks(List<Document> results) {
        Stage resultsStage = new Stage();
        VBox resultsBox = new VBox(10);
        resultsBox.setPadding(new Insets(10));
    
        for (Document result : results) {
            VBox resultBox = new VBox(5);
            resultBox.getChildren().add(new Label("Country: " + result.getString("country")));
            resultBox.getChildren().add(new Label("Title: " + result.getString("title")));
            resultBox.getChildren().add(new Label("Number of Reviews: " + result.getInteger("numReviews")));
    
            // Add book image
            String imageUrl = result.getString("imageUrl");
            ImageView imageView = new ImageView(new Image(imageUrl));
            imageView.setFitHeight(100);
            imageView.setFitWidth(80);
            resultBox.getChildren().add(imageView);
    
            // Add click event to open book details
            resultBox.setOnMouseClicked(e -> {
                Book book = bookService.getBookById(result.getObjectId("bookId"));
                if (book != null) {
                    displayBookDetails(book, resultsStage);
                }
            });
    
            resultsBox.getChildren().add(resultBox);
            resultsBox.getChildren().add(new Separator()); // Add a line separator between each book's data
        }
    
        Scene resultsScene = new Scene(new ScrollPane(resultsBox), 400, 600);

        resultsStage.setScene(resultsScene);
        resultsStage.setTitle("Most Famous Books by Genre");
        resultsStage.show();
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
    
        TextField nameField = new TextField(currentUser.getName());
        nameField.setPromptText("Name");
    
        DatePicker birthdatePicker = new DatePicker(currentUser.getBirthdate());
    
        Label nicknameLabel = new Label("Nickname: " + currentUser.getNickname());
        Label genderLabel = new Label("Gender: " + currentUser.getGender());
        Label nationalityLabel = new Label("Nationality: " + currentUser.getNationality());
        Label favoriteGenresLabel = new Label("Favorite Genres: " + String.join(", ", currentUser.getFavouriteGenres()));
        Label spokenLanguagesLabel = new Label("Spoken Languages: " + String.join(", ", currentUser.getSpokenLanguages()));
    
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Old Password");
    
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
    
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String newName = nameField.getText();
            LocalDate newBirthdate = birthdatePicker.getValue();
            String newPassword = newPasswordField.getText();
    
            if (newPassword.isEmpty() || userService.changePassword(currentUser.getId(), newPassword)) {
                currentUser.setName(newName);
                currentUser.setBirthdate(newBirthdate);
                boolean success = userService.updateAccountInformation(currentUser.getId(), currentUser);
    
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Profile updated successfully.");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to update profile.");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to change password.");
                alert.showAndWait();
            }
        });
    
        Button showReviewsButton = new Button("Show Reviews");
        showReviewsButton.setOnAction(e -> showUserReviews(currentUser));
    
        Button deleteAccountButton = new Button("Delete Account");
        deleteAccountButton.setOnAction(e -> handleDeleteAccount());
    
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> handleLogout());
    
        vbox.getChildren().addAll(
                new Label("Name"), nameField,
                new Label("Birthdate"), birthdatePicker,
                nicknameLabel,
                genderLabel,
                nationalityLabel,
                favoriteGenresLabel,
                spokenLanguagesLabel,
                new Label("Old Password"), oldPasswordField,
                new Label("New Password"), newPasswordField,
                saveButton,
                showReviewsButton,
                deleteAccountButton,
                logoutButton
        );
    
        if (currentUser instanceof Author) {
            Button browseBooksButton = new Button("Browse Books");
            browseBooksButton.setOnAction(e -> browseAuthorBooks());
            vbox.getChildren().add(browseBooksButton);
        }

        if (currentUser instanceof Author) {
            Button viewStatsButton = new Button("View Stats");
            viewStatsButton.setOnAction(e -> {
                List<Document> stats = procedures.calculateAuthorStats(currentUser.getId());
                Stage statsStage = new Stage();
                VBox statsBox = new VBox(10);
                statsBox.setPadding(new Insets(10));
                
                for (Document stat : stats) {
                    VBox statBox = new VBox(5);
                    statBox.getChildren().add(new Label("Title: " + stat.getString("bookTitle")));
    
                    Number bookRating = stat.get("bookRating", Number.class);
                    statBox.getChildren().add(new Label("Average Rating: " + bookRating.doubleValue()));
    
                    statBox.getChildren().add(new Label("Total Ratings: " + stat.getInteger("bookTotalRatings")));
    
                    List<Document> countryDetails = (List<Document>) stat.get("bookCountryDetails");
                    if (countryDetails != null && !countryDetails.isEmpty()) {
                        for (Document countryDetail : countryDetails) {
                            Number averageRating = countryDetail.get("averageRating", Number.class);
                            statBox.getChildren().add(new Label("Country: " + countryDetail.getString("country") +
                                    ", Average Rating: " + averageRating.doubleValue() +
                                    ", Number of Ratings: " + countryDetail.getInteger("numRatings")));
                        }
                    }
                    statsBox.getChildren().add(statBox);
                    statsBox.getChildren().add(new Separator()); // Add a line separator between each book's data
                }
                Scene statsScene = new Scene(new ScrollPane(statsBox), 400, 600);
                statsStage.setScene(statsScene);
                statsStage.setTitle("Author Stats");
                statsStage.show();
            });
    
            vbox.getChildren().add(viewStatsButton);
        }
    
        tab.setContent(vbox);
        return tab;
    }

    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete your account? This action cannot be undone.");
    
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userService.deleteAccount(currentUser.getId(), currentUser.getId())) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Account Deleted");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Your account has been deleted successfully.");
                successAlert.showAndWait();
    
                handleLogout();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Failed to delete account.");
                errorAlert.showAndWait();
            }
        }
    }
    
    private void handleLogout() {
        currentUser = null;
        TabPane tabPane = new TabPane();
    
        Tab loginTab = createLoginTab();
        Tab registerTab = createRegisterTab();
        Tab homeTab = createHomeTab();
    
        tabPane.getTabs().addAll(loginTab, registerTab, homeTab);
    
        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setScene(scene); 
    }

    private void showUserReviews(Reviewer currentUser) {
        Stage reviewsStage = new Stage();
        VBox allReviewsBox = new VBox(10);
        allReviewsBox.setPadding(new Insets(10));
    
        for (ObjectId reviewId : currentUser.getReviewIds()) {
            Review review = reviewDao.findReviewById(reviewId);
            if (review != null) {
                VBox reviewBox = new VBox(5);
                Label reviewerLabel = new Label("Reviewer: " + review.getNickname());
                Label reviewTextLabel = new Label("Review: " + review.getText());
                Label reviewStarsLabel = new Label("Stars: " + review.getStars());
                Button deleteReviewButton = new Button("Delete Review");
                deleteReviewButton.setOnAction(e -> {
                    if (reviewService.deleteReview(review.getId(), currentUser, review.getBookId())) {
                        currentUser.removeReview(review.getId());
                        userService.updateAccountInformation(currentUser.getId(), currentUser);
                        allReviewsBox.getChildren().remove(reviewBox);
                    }
                });
    
                Button editReviewButton = new Button("Edit");
                editReviewButton.setOnAction(e -> {
                    Stage editStage = new Stage();
                    VBox editBox = new VBox(10);
                    editBox.setPadding(new Insets(10));
    
                    TextArea reviewTextArea = new TextArea(review.getText());
                    ComboBox<Integer> ratingComboBox = new ComboBox<>();
                    ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
                    ratingComboBox.setValue(review.getStars());
    
                    Button submitEditButton = new Button("Submit");
                    submitEditButton.setOnAction(event -> {
                        String newText = reviewTextArea.getText();
                        int newRating = ratingComboBox.getValue();
                        review.setText(newText);
                        review.setStars(newRating);
    
                        if (reviewService.updateReview(review, currentUser)) {
                            reviewTextLabel.setText("Review: " + newText);
                            reviewStarsLabel.setText("Stars: " + newRating);
                            editStage.close();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Failed to update review.");
                            alert.showAndWait();
                        }
                    });
    
                    editBox.getChildren().addAll(new Label("Edit Review"), new Label("Text:"), reviewTextArea, new Label("Rating:"), ratingComboBox, submitEditButton);
                    Scene editScene = new Scene(editBox, 300, 200);
                    editStage.setScene(editScene);
                    editStage.show();
                });
    
                Button goToBookButton = new Button("Go to book");
                goToBookButton.setOnAction(e -> {
                    Book book = bookService.getBookById(review.getBookId());
                    if (book != null) {
                        displayBookDetails(book, reviewsStage);
                    }
                });
    
                reviewBox.getChildren().addAll(reviewerLabel, reviewTextLabel, reviewStarsLabel, deleteReviewButton, editReviewButton, goToBookButton);
                allReviewsBox.getChildren().add(reviewBox);
            }
        }
    
        ScrollPane scrollPane = new ScrollPane(allReviewsBox);
        Scene scene = new Scene(scrollPane, 400, 600);
        reviewsStage.setScene(scene);
        reviewsStage.show();
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
            bookBox.setOnMouseClicked(e -> displayBookDetails(book, (Stage) vbox.getScene().getWindow()));

            vbox.getChildren().add(bookBox);
        }
    }

    private void browseAuthorBooks() {
        Stage booksStage = new Stage();
        VBox booksBox = new VBox(10);
        booksBox.setPadding(new Insets(10));
    
        List<Book> authorBooks = bookService.getBooksByAuthor(currentUser.getId());
        displayBooks(authorBooks, booksBox);
    
        ScrollPane scrollPane = new ScrollPane(booksBox);
        Scene scene = new Scene(scrollPane, 400, 600);
        booksStage.setScene(scene);
        booksStage.show();
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
        reviewsButton.setOnAction(e -> displayUserReviews(reviewer.getReviewIds(), reviewer));
    
        userDetailsBox.getChildren().addAll(
                nameLabel,
                nicknameLabel,
                genderLabel,
                birthdateLabel,
                nationalityLabel,
                favoriteGenresLabel,
                spokenLanguagesLabel,
                reviewsButton
        );
    
        if (currentUser != null) {
            Button followButton = new Button("Follow");
            followButton.setOnAction(e -> followUser(reviewer));
            userDetailsBox.getChildren().add(followButton);
    
            if (currentUser instanceof Admin) {
                Button deleteAccountButton = new Button("Delete Account");
                deleteAccountButton.setOnAction(e -> {
                    if (userService.deleteAccount(currentUser.getId(), reviewer.getId())) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.setContentText("Account deleted successfully.");
                        alert.showAndWait();
                        userStage.close();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to delete account.");
                        alert.showAndWait();
                    }
                });
                userDetailsBox.getChildren().add(deleteAccountButton);
            }
    
            Button viewTop3ReviewsButton = new Button("View top 3 useful reviews");
            viewTop3ReviewsButton.setOnAction(e -> displayTop3UsefulReviews(reviewer));
            userDetailsBox.getChildren().add(viewTop3ReviewsButton);
        }
    
        Scene scene = new Scene(userDetailsBox, 300, 400);
        userStage.setScene(scene);
        userStage.show();
    }
    
    private void displayTop3UsefulReviews(Reviewer reviewer) {
        List<Document> reviews = procedures.findMostUsefulReviews(reviewer.getNickname());
        Stage reviewsStage = new Stage();
        VBox reviewsBox = new VBox(10);
        reviewsBox.setPadding(new Insets(10));
    
        for (Document book : reviews) {
            Label bookTitleLabel = new Label("Book Title: " + book.getString("title"));
            reviewsBox.getChildren().add(bookTitleLabel);
    
            List<Document> usefulReviews = (List<Document>) book.get("most_useful_reviews");
            for (Document review : usefulReviews) {
                VBox reviewBox = new VBox(5);
                Label reviewTextLabel = new Label("Review: " + review.getString("review_text"));
                Label reviewStarsLabel = new Label("Stars: " + review.getInteger("rating"));
                Label reviewUpVotesLabel = new Label("Upvotes: " + review.getInteger("count_up_votes"));
                Label reviewDownVotesLabel = new Label("Downvotes: " + review.getInteger("count_down_votes"));
    
                reviewBox.getChildren().addAll(reviewTextLabel, reviewStarsLabel, reviewUpVotesLabel, reviewDownVotesLabel);
    
                if (currentUser != null) {
                    Button upvoteButton = new Button("Upvote");
                    upvoteButton.setOnAction(e -> handleVote(new Review(review), true));
                    Button downvoteButton = new Button("Downvote");
                    downvoteButton.setOnAction(e -> handleVote(new Review(review), false));
                    reviewBox.getChildren().addAll(upvoteButton, downvoteButton);
                }
    
                reviewsBox.getChildren().add(reviewBox);
                reviewsBox.getChildren().add(new Separator());
            }
        }
    
        Scene scene = new Scene(new ScrollPane(reviewsBox), 400, 600);
        reviewsStage.setScene(scene);
        reviewsStage.setTitle("Top 3 Useful Reviews");
        reviewsStage.show();
    }

    private void displayUserReviews(List<ObjectId> reviewIds, Reviewer reviewer) {
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

                Button goToBookButton = new Button("Go to Book");
                goToBookButton.setOnAction(e -> {
                    Book book = bookService.getBookById(review.getBookId());
                    if (book != null) {
                        displayBookDetails(book, reviewStage);
                    }
                });

                reviewBox.getChildren().addAll(reviewerLabel, reviewTextLabel, reviewStarsLabel, goToBookButton);

                // Show delete button only if the current user is an admin
                if (currentUser instanceof Admin) {
                    Button deleteReviewButton = new Button("Delete Review");
                    deleteReviewButton.setOnAction(e -> {
                        if (reviewService.deleteReview(review.getId(), reviewer, review.getBookId())) {
                            reviewer.removeReview(review.getId());
                            userService.updateAccountInformation(reviewer.getId(), reviewer);
                            reviewsBox.getChildren().remove(reviewBox);
                        }
                    });
                    reviewBox.getChildren().add(deleteReviewButton);
                }

                reviewsBox.getChildren().add(reviewBox);
            }
        }

        ScrollPane scrollPane = new ScrollPane(reviewsBox);
        Scene scene = new Scene(scrollPane, 400, 600);
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

    private void displayBookDetails(Book book, Stage ownerStage) {
        Stage bookStage = new Stage();
        bookStage.initOwner(ownerStage);

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

        Button showAllReviewsButton = new Button("Show all reviews");
        showAllReviewsButton.setOnAction(e -> showAllReviews(book));

        ScrollPane scrollPaneReviews = new ScrollPane(reviewsBox);
        scrollPaneReviews.setFitToWidth(true);
        scrollPaneReviews.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        bookDetailsBox.getChildren().addAll(reviewsLabel, scrollPaneReviews, showAllReviewsButton);

        // Add review section if user is logged in
        if (currentUser != null) {
            Label leaveReviewLabel = new Label("Leave a Review");
            ComboBox<Integer> ratingComboBox = new ComboBox<>();
            ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
            ratingComboBox.setValue(5);

            TextArea reviewTextArea = new TextArea();
            reviewTextArea.setPromptText("Write your review here (optional)");

            Button submitReviewButton = new Button("Submit Review");
            submitReviewButton.setOnAction(e -> {
                int rating = ratingComboBox.getValue();
                String text = reviewTextArea.getText();

                // Create and submit the review
                Review newReview = new Review(new ObjectId(), currentUser.getId(), book.getId(), currentUser.getNickname(), text, ((Reviewer) currentUser).getNationality(), rating, 0, 0);
                if (reviewDao.addReview(newReview)) {

                    // Reload the book from the database
                    Book updatedBook = bookService.getBookById(book.getId());

                    // Update the local book object
                    book.setReviewIds(updatedBook.getReviewIds());
                    book.setMost10UsefulReviews(updatedBook.getMost10UsefulReviews());

                    // Reload the user from the database
                    currentUser = (Reviewer) userService.viewInfoAccount(currentUser.getId().toString());

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Review submitted successfully.");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to submit review.");
                    alert.showAndWait();
                }
            });

            VBox leaveReviewBox = new VBox(5);
            leaveReviewBox.getChildren().addAll(leaveReviewLabel, new Label("Rating:"), ratingComboBox, new Label("Review:"), reviewTextArea, submitReviewButton);
            bookDetailsBox.getChildren().add(leaveReviewBox);
        }

        // Add Edit Book button if current user is the author of the book
        if (currentUser != null && currentUser.getId().equals(book.getAuthors()[0].getId())) {
            Button editBookButton = new Button("Edit Book");
            editBookButton.setOnAction(e -> editBook(book));
            bookDetailsBox.getChildren().add(editBookButton);
        }

        // Add delete book button if the user is an admin or the author of the book
        if (currentUser instanceof Admin || (currentUser instanceof Author && Arrays.stream(book.getAuthors()).anyMatch(a -> a.getId().equals(currentUser.getId())))) {
            Button deleteBookButton = new Button("Delete Book");
            deleteBookButton.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Book");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete this book? This action cannot be undone.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    if (bookService.deleteBook(book, currentUser)) {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Book deleted successfully.");
                        successAlert.showAndWait();
                        bookStage.close();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Failed to delete book.");
                        errorAlert.showAndWait();
                    }
                }
            });
            bookDetailsBox.getChildren().add(deleteBookButton);
        }

        ScrollPane scrollPane = new ScrollPane(bookDetailsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 400, 600);
        bookStage.setScene(scene);
        bookStage.show();
    }

    private void editBook(Book book) {
        Stage editStage = new Stage();
        VBox editBox = new VBox(10);
        editBox.setPadding(new Insets(10));
    
        TextField titleField = new TextField(book.getTitle());
        ComboBox<String> languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll(
            "abk", "ady", "afr", "ale", "amh", "apa", "ara", "arg", "arw", "aus", "ave", "aze", "ben", "bos",
            "bul", "cat", "cze", "dan", "dum", "egy", "elx", "en", "en-CA", "en-GB", "en-US", "eng", "enm",
            "epo", "es-MX", "est", "eus", "fil", "fin", "fre", "frm", "fro", "frs", "ger", "gle", "glg",
            "gmh", "grc", "gre", "guj", "heb", "hin", "hun", "hye", "iba", "ind", "ira", "isl", "ita",
            "jpn", "kam", "kan", "kat", "kor", "kur", "lat", "lav", "lit", "mal", "mar", "mkd", "mlt",
            "mon", "msa", "mul", "mus", "myn", "nep", "nl", "nld", "nno", "nob", "nor", "pan", "peo", "per",
            "pes", "phi", "pol", "por", "pra", "pt-BR", "raj", "rum", "rup", "rus", "sco", "scr", "sin",
            "slo", "slv", "snd", "spa", "sqi", "srp", "swe", "tam", "tel", "tgl", "tha", "tlh", "tur",
            "ukr", "und", "urd", "vie", "vls", "wak", "zho"
        );
        languageComboBox.setValue(book.getLanguage());
    
        TextField numPagesField = new TextField(String.valueOf(book.getNumPages()));
        TextField yearField = new TextField(String.valueOf(book.getYear()));
        CheckComboBox<String> genresCheckComboBox = new CheckComboBox<>(FXCollections.observableArrayList(
            "biography", "children", "comics", "crime", "fantasy", "fiction", "graphic", "historical fiction",
            "history", "mystery", "non-fiction", "paranormal", "poetry", "romance", "thriller", "young-adult"
        ));
        for (String genre : book.getGenre()) {
            genresCheckComboBox.getCheckModel().check(genre);
        }
    
        Button submitEditButton = new Button("Submit");
        submitEditButton.setOnAction(e -> {
            String newTitle = titleField.getText();
            String newLanguage = languageComboBox.getValue();
            int newNumPages = Integer.parseInt(numPagesField.getText());
            int newYear = Integer.parseInt(yearField.getText());
            List<String> newGenres = genresCheckComboBox.getCheckModel().getCheckedItems();
    
            book.setTitle(newTitle);
            book.setLanguage(newLanguage);
            book.setNumPages(newNumPages);
            book.setYear(newYear);
            book.setGenre(newGenres.toArray(new String[0]));
    
            if (bookService.updateBook(book, currentUser)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Book updated successfully.");
                alert.showAndWait();
                editStage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to update book.");
                alert.showAndWait();
            }
        });
    
        editBox.getChildren().addAll(
            new Label("Title"), titleField,
            new Label("Language"), languageComboBox,
            new Label("Number of Pages"), numPagesField,
            new Label("Year"), yearField,
            new Label("Genres"), genresCheckComboBox,
            submitEditButton
        );
    
        ScrollPane scrollPane = new ScrollPane(editBox);
        Scene editScene = new Scene(scrollPane, 400, 600);
        editStage.setScene(editScene);
        editStage.show();
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

            // If the user is an author, add the Upload tab
            if (user instanceof Author) {
                Tab uploadTab = createUploadTab();
                tabPane.getTabs().add(uploadTab);
            }

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

    private void showAllReviews(Book book) {
        Stage reviewsStage = new Stage();
        VBox allReviewsBox = new VBox(10);
        allReviewsBox.setPadding(new Insets(10));

        for (ObjectId reviewId : book.getReviewIds()) {
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
                
                allReviewsBox.getChildren().add(reviewBox);
            }
        }

        ScrollPane scrollPane = new ScrollPane(allReviewsBox);
        Scene scene = new Scene(scrollPane, 400, 600);
        reviewsStage.setScene(scene);
        reviewsStage.show();
    }

    private Tab createUploadTab() {
        Tab tab = new Tab("Upload");
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
    
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
    
        ComboBox<String> languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll(
            "abk", "ady", "afr", "ale", "amh", "apa", "ara", "arg", "arw", "aus", "ave", "aze", "ben", "bos",
            "bul", "cat", "cze", "dan", "dum", "egy", "elx", "en", "en-CA", "en-GB", "en-US", "eng", "enm",
            "epo", "es-MX", "est", "eus", "fil", "fin", "fre", "frm", "fro", "frs", "ger", "gle", "glg",
            "gmh", "grc", "gre", "guj", "heb", "hin", "hun", "hye", "iba", "ind", "ira", "isl", "ita",
            "jpn", "kam", "kan", "kat", "kor", "kur", "lat", "lav", "lit", "mal", "mar", "mkd", "mlt",
            "mon", "msa", "mul", "mus", "myn", "nep", "nl", "nld", "nno", "nob", "nor", "pan", "peo", "per",
            "pes", "phi", "pol", "por", "pra", "pt-BR", "raj", "rum", "rup", "rus", "sco", "scr", "sin",
            "slo", "slv", "snd", "spa", "sqi", "srp", "swe", "tam", "tel", "tgl", "tha", "tlh", "tur",
            "ukr", "und", "urd", "vie", "vls", "wak", "zho"
        );
        languageComboBox.setPromptText("Select Language");
    
        TextField numPagesField = new TextField();
        numPagesField.setPromptText("Number of Pages");
    
        TextField yearField = new TextField();
        yearField.setPromptText("Year");
    
        Label genresLabel = new Label("Genres:");
        CheckComboBox<String> genresCheckComboBox = new CheckComboBox<>(FXCollections.observableArrayList(
            "biography", "children", "comics", "crime", "fantasy", "fiction", "graphic", "historical fiction",
            "history", "mystery", "non-fiction", "paranormal", "poetry", "romance", "thriller", "young-adult"
        ));
    
        Button uploadButton = new Button("Upload");
        uploadButton.setOnAction(e -> handleUploadBook(
            titleField.getText(),
            languageComboBox.getValue(),
            numPagesField.getText(),
            yearField.getText(),
            genresCheckComboBox.getCheckModel().getCheckedItems()
        ));
    
        vbox.getChildren().addAll(
            new Label("Title"), titleField,
            new Label("Language"), languageComboBox,
            new Label("Number of Pages"), numPagesField,
            new Label("Year"), yearField,
            genresLabel, genresCheckComboBox,
            uploadButton
        );
    
        ScrollPane scrollPane = new ScrollPane(vbox);
        tab.setContent(scrollPane);
    
        return tab;
    }
    
    private void handleUploadBook(String title, String language, String numPages, String year, List<String> genres) {
        try {
            int numPagesInt = Integer.parseInt(numPages);
            int yearInt = Integer.parseInt(year);
    
            Book newBook = new Book(
                new ObjectId(), 0, 0, language, title, 
                new Book.Author[]{new Book.Author(currentUser.getId(), currentUser.getName())}, 
                genres.toArray(new String[0]), yearInt, 
                "https://s.gr-assets.com/assets/nophoto/book/111x148-bcc042a9c91a29c1d680899eff700a03.png", 
                numPagesInt, new ArrayList<>(), null, null
            );
    
            if (bookService.addBook(newBook, currentUser)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Book uploaded successfully.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to upload book.");
                alert.showAndWait();
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please enter valid numbers for pages and year.");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
