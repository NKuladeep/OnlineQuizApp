package com.quizapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

public class QuizApplication extends Application {
    private Stage primaryStage;
    private User currentUser;
    private DatabaseManager dbManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.dbManager = new DatabaseManager();

        primaryStage.setTitle("Online Quiz Application");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        showLoginScreen();
        primaryStage.show();
    }

    private void showLoginScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label titleLabel = new Label("Quiz Application");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: white;");

        VBox loginBox = createLoginForm();

        root.getChildren().addAll(titleLabel, loginBox);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

    private VBox createLoginForm() {
        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(40));
        loginBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");
        loginBox.setMaxWidth(400);

        Label loginTitle = new Label("Login");
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);

        Button loginButton = new Button("Login");
        loginButton.setPrefHeight(40);
        loginButton.setPrefWidth(150);
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        Button registerButton = new Button("Register");
        registerButton.setPrefHeight(40);
        registerButton.setPrefWidth(150);
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));
        registerButton.setOnAction(e -> showRegistrationScreen());

        HBox buttonBox = new HBox(10, loginButton, registerButton);
        buttonBox.setAlignment(Pos.CENTER);

        loginBox.getChildren().addAll(loginTitle, usernameField, passwordField, buttonBox);
        return loginBox;
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Please fill in all fields.");
            return;
        }

        User user = dbManager.authenticateUser(username, password);
        if (user != null) {
            currentUser = user;
            showMainMenu();
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    private void showRegistrationScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        VBox regBox = new VBox(15);
        regBox.setAlignment(Pos.CENTER);
        regBox.setPadding(new Insets(40));
        regBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");
        regBox.setMaxWidth(400);

        Label regTitle = new Label("Register");
        regTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(40);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setPrefHeight(40);

        CheckBox adminCheckBox = new CheckBox("Register as Administrator");

        Button registerButton = new Button("Register");
        Button backButton = new Button("Back to Login");

        registerButton.setOnAction(e -> handleRegistration(
                usernameField.getText(), emailField.getText(),
                passwordField.getText(), confirmPasswordField.getText(),
                adminCheckBox.isSelected()
        ));
        backButton.setOnAction(e -> showLoginScreen());

        HBox buttonBox = new HBox(10, registerButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        regBox.getChildren().addAll(regTitle, usernameField, emailField, passwordField, confirmPasswordField, adminCheckBox, buttonBox);
        root.getChildren().add(regBox);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

    private void handleRegistration(String username, String email, String password, String confirmPassword, boolean isAdmin) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Registration Error", "Please fill in all fields.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert("Registration Error", "Passwords do not match.");
            return;
        }
        if (dbManager.registerUser(username, email, password, isAdmin)) {
            showAlert("Success", "Registration successful! You can now log in.");
            showLoginScreen();
        } else {
            showAlert("Registration Error", "Username already exists.");
        }
    }

    public void showMainMenu() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        welcomeLabel.setStyle("-fx-text-fill: white;");

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(40));
        menuBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");
        menuBox.setMaxWidth(500);

        Button takeQuizButton = createMenuButton("Take Quiz", "#4CAF50");
        Button viewScoresButton = createMenuButton("View My Scores", "#2196F3");
        Button leaderboardButton = createMenuButton("Leaderboard", "#FF9800");

        takeQuizButton.setOnAction(e -> showQuizSelection());
        viewScoresButton.setOnAction(e -> showUserScores());
        leaderboardButton.setOnAction(e -> showLeaderboard());

        menuBox.getChildren().addAll(takeQuizButton, viewScoresButton, leaderboardButton);

        if (currentUser.isAdmin()) {
            Button manageQuizzesButton = createMenuButton("Manage Quizzes", "#9C27B0");
            manageQuizzesButton.setOnAction(e -> showQuizManagement());
            menuBox.getChildren().add(manageQuizzesButton);
        }

        Button logoutButton = createMenuButton("Logout", "#f44336");
        logoutButton.setOnAction(e -> {
            currentUser = null;
            showLoginScreen();
        });
        menuBox.getChildren().add(logoutButton);

        root.getChildren().addAll(welcomeLabel, menuBox);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

    private void showQuizManagement() {
        new QuizManager(primaryStage, currentUser, dbManager, this).show();
    }

    private void showQuizSelection() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label titleLabel = new Label("Select a Quiz");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: white;");

        List<Quiz> quizzes = dbManager.getAllQuizzes();
        ListView<Quiz> quizListView = new ListView<>();
        quizListView.getItems().addAll(quizzes);
        quizListView.setPrefHeight(300);

        Button startButton = new Button("Start Selected Quiz");
        startButton.setDisable(true); // Disabled until a quiz is selected
        startButton.setOnAction(e -> {
            Quiz selectedQuiz = quizListView.getSelectionModel().getSelectedItem();
            if(selectedQuiz != null) {
                startQuiz(selectedQuiz);
            }
        });

        quizListView.getSelectionModel().selectedItemProperty().addListener((obs, old, aNew) -> {
            startButton.setDisable(aNew == null);
        });

        Button backButton = createMenuButton("Back to Menu", "#f44336");
        backButton.setPrefWidth(200);
        backButton.setOnAction(e -> showMainMenu());

        root.getChildren().addAll(titleLabel, quizListView, startButton, backButton);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    private void startQuiz(Quiz quiz) {
        new QuizTaker(primaryStage, quiz, currentUser, dbManager, this).start();
    }

    private void showUserScores() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f4f9;");

        Label title = new Label("My Scores");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        TableView<QuizResult> table = new TableView<>();
        TableColumn<QuizResult, String> quizCol = new TableColumn<>("Quiz Title");
        quizCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quizTitle"));
        TableColumn<QuizResult, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("score"));
        TableColumn<QuizResult, Integer> totalCol = new TableColumn<>("Total Questions");
        totalCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalQuestions"));
        TableColumn<QuizResult, Double> percCol = new TableColumn<>("Percentage");
        percCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("percentage"));
        TableColumn<QuizResult, String> dateCol = new TableColumn<>("Date Taken");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateTaken"));

        table.getColumns().addAll(quizCol, scoreCol, totalCol, percCol, dateCol);
        table.setItems(javafx.collections.FXCollections.observableArrayList(dbManager.getUserQuizResults(currentUser.getId())));

        root.setCenter(table);

        Button backButton = createMenuButton("Back", "#f44336");
        backButton.setOnAction(e -> showMainMenu());
        root.setBottom(backButton);
        BorderPane.setAlignment(backButton, Pos.CENTER);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    private void showLeaderboard() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f4f9;");

        Label title = new Label("Leaderboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        TableView<LeaderboardEntry> table = new TableView<>();
        TableColumn<LeaderboardEntry, String> rankCol = new TableColumn<>("Rank");
        rankCol.setCellFactory(col -> {
            TableCell<LeaderboardEntry, String> cell = new TableCell<>();
            cell.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
                if (cell.isEmpty()) {
                    return null;
                } else {
                    return Integer.toString(cell.getIndex() + 1);
                }
            }, cell.emptyProperty(), cell.indexProperty()));
            return cell;
        });

        TableColumn<LeaderboardEntry, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("username"));

        TableColumn<LeaderboardEntry, Double> avgScoreCol = new TableColumn<>("Average Score (%)");
        avgScoreCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("averageScore"));

        TableColumn<LeaderboardEntry, Integer> attemptsCol = new TableColumn<>("Total Attempts");
        attemptsCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalAttempts"));

        table.getColumns().addAll(rankCol, userCol, avgScoreCol, attemptsCol);
        table.setItems(javafx.collections.FXCollections.observableArrayList(dbManager.getLeaderboard()));

        root.setCenter(table);

        Button backButton = createMenuButton("Back", "#f44336");
        backButton.setOnAction(e -> showMainMenu());
        root.setBottom(backButton);
        BorderPane.setAlignment(backButton, Pos.CENTER);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    private Button createMenuButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefHeight(50);
        button.setPrefWidth(300);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        return button;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
