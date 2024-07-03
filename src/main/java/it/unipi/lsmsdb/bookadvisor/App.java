package it.unipi.lsmsdb.bookadvisor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import it.unipi.lsmsdb.bookadvisor.service.AuthenticationService;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.UserGraphDAO;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.MongoDBConnector;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.Neo4jConnector;

public class App extends Application {
    private AuthenticationService authenticationService;

    @Override
    public void start(Stage primaryStage) {
        // Initialize services
        UserDao userDao = new UserDao(new MongoDBConnector());
        UserGraphDAO userGraphDAO = new UserGraphDAO(new Neo4jConnector());
        authenticationService = new AuthenticationService(userDao);

        primaryStage.setTitle("Book Advisor");

        // Create login form
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");

        VBox loginForm = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField, loginButton);
        loginForm.setPrefWidth(300);

        // Create book display area
        ListView<String> bookListView = new ListView<>();
        Button loadBooksButton = new Button("Load Books");

        VBox bookDisplay = new VBox(10, loadBooksButton, bookListView);
        bookDisplay.setPrefWidth(300);

        HBox mainLayout = new HBox(20, loginForm, bookDisplay);
        Scene scene = new Scene(mainLayout, 600, 400);

        // Handle login button click
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (authenticationService.logIn(username, password) != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login successful!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Login failed!");
                alert.showAndWait();
            }
        });

        // Handle load books button click
        loadBooksButton.setOnAction(event -> {
            // Load books from the service and display in the list view
            // This is just a placeholder, replace it with actual data retrieval
            bookListView.getItems().setAll("Book 1", "Book 2", "Book 3");
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
