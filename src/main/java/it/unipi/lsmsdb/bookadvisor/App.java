package it.unipi.lsmsdb.bookadvisor;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.MongoDBConnector;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.Neo4jConnector;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.UserGraphDAO;
import it.unipi.lsmsdb.bookadvisor.service.AuthenticationService;
import it.unipi.lsmsdb.bookadvisor.model.user.User;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class App extends Application {
    private AuthenticationService authenticationService;

    @Override
    public void start(Stage primaryStage) {
        // Initialize MongoDB connector and UserDao
        MongoDBConnector connector = MongoDBConnector.getInstance();
        Neo4jConnector neo4jConnector = new Neo4jConnector();
        UserDao userDao = new UserDao(connector);
        UserGraphDAO userGraphDAO = new UserGraphDAO(neo4jConnector);

        // Initialize AuthenticationService
        authenticationService = new AuthenticationService(userDao, userGraphDAO);

        primaryStage.setTitle("Book Advisor");

        // Create the login and registration forms
        TabPane tabPane = new TabPane();

        // Login tab
        Tab loginTab = new Tab("Login", createLoginForm());

        // Registration tab
        Tab registerTab = new Tab("Register", createRegisterForm());

        tabPane.getTabs().addAll(loginTab, registerTab);

        Scene scene = new Scene(tabPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createLoginForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            handleLogin(username, password);
        });

        return grid;
    }

    private GridPane createRegisterForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Label birthdateLabel = new Label("Birthdate (YYYY-MM-DD):");
        TextField birthdateField = new TextField();
        Label genderLabel = new Label("Gender:");
        TextField genderField = new TextField();
        Label nationalityLabel = new Label("Nationality:");
        TextField nationalityField = new TextField();
        Label favGenresLabel = new Label("Favorite Genres (comma separated):");
        TextField favGenresField = new TextField();
        Label spokenLanguagesLabel = new Label("Spoken Languages (comma separated):");
        TextField spokenLanguagesField = new TextField();
        Label genresLabel = new Label("Genres (comma separated, for authors):");
        TextField genresField = new TextField();

        Button registerButton = new Button("Register");

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(birthdateLabel, 0, 3);
        grid.add(birthdateField, 1, 3);
        grid.add(genderLabel, 0, 4);
        grid.add(genderField, 1, 4);
        grid.add(nationalityLabel, 0, 5);
        grid.add(nationalityField, 1, 5);
        grid.add(favGenresLabel, 0, 6);
        grid.add(favGenresField, 1, 6);

        grid.add(spokenLanguagesLabel, 0, 7);
        grid.add(spokenLanguagesField, 1, 7);
        grid.add(genresLabel, 0, 8);
        grid.add(genresField, 1, 8);
        grid.add(registerButton, 1, 9);

        registerButton.setOnAction(e -> {
            String name = nameField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            LocalDate birthdate = LocalDate.parse(birthdateField.getText());
            String gender = genderField.getText();
            String nationality = nationalityField.getText();
            List<String> favGenres = Arrays.asList(favGenresField.getText().split(","));
            List<String> spokenLanguages = Arrays.asList(spokenLanguagesField.getText().split(","));
            List<String> genres = genresField.getText().isEmpty() ? null : Arrays.asList(genresField.getText().split(","));

            handleRegister(username, password, name, gender, birthdate, nationality, favGenres, spokenLanguages, genres);
        });

        return grid;
    }

    private void handleLogin(String username, String password) {
        // Use AuthenticationService to validate the user credentials
        User user = authenticationService.logIn(username, password);
        if (user != null) {
            System.out.println("Login successful for user: " + user.getNickname());
            // Here you can load the next scene or show a success message
        } else {
            System.out.println("Login failed for user: " + username);
            // Show an error message to the user
        }
    }

    private void handleRegister(String username, String password, String name, String gender, LocalDate birthdate, String nationality, List<String> favGenres, List<String> spokenLanguages, List<String> genres) {
        // Use AuthenticationService to register a new user
        boolean success = authenticationService.signUp(username, password, name, gender, birthdate, nationality, favGenres, spokenLanguages, genres);
        if (success) {
            System.out.println("Registration successful for user: " + username);
            // Here you can load the login tab or show a success message
        } else {
            System.out.println("Registration failed for user: " + username);
            // Show an error message to the user
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
