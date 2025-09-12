package com.quizapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class QuizTaker {
    private Stage primaryStage;
    private Quiz quiz;
    private User currentUser;
    private DatabaseManager dbManager;
    private QuizApplication mainApp;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private String[] userAnswers;

    private Label questionNumberLabel;
    private Label questionTextLabel;
    private RadioButton optionA, optionB, optionC, optionD;
    private ToggleGroup optionsGroup;
    private Button nextButton, prevButton, submitButton;

    public QuizTaker(Stage primaryStage, Quiz quiz, User currentUser, DatabaseManager dbManager, QuizApplication mainApp) {
        this.primaryStage = primaryStage;
        this.quiz = quiz;
        this.currentUser = currentUser;
        this.dbManager = dbManager;
        this.mainApp = mainApp;
    }

    public void start() {
        this.questions = dbManager.getQuestionsForQuiz(quiz.getId());
        if (questions == null || questions.isEmpty()) {
            showAlert("No Questions", "This quiz has no questions yet. Please contact an administrator.");
            mainApp.showMainMenu();
            return;
        }
        this.userAnswers = new String[questions.size()];
        showQuestionScreen();
    }

    private void showQuestionScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f4f4f9;");

        Label quizTitleLabel = new Label(quiz.getTitle());
        quizTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        questionNumberLabel = new Label();
        questionNumberLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));

        questionTextLabel = new Label();
        questionTextLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        questionTextLabel.setWrapText(true);
        questionTextLabel.setMaxWidth(700);

        optionsGroup = new ToggleGroup();
        optionA = createRadioButton();
        optionB = createRadioButton();
        optionC = createRadioButton();
        optionD = createRadioButton();

        VBox optionsBox = new VBox(15, optionA, optionB, optionC, optionD);
        optionsBox.setPadding(new Insets(20, 0, 20, 30));

        prevButton = new Button("Previous");
        nextButton = new Button("Next");
        submitButton = new Button("Submit");

        HBox buttonBar = new HBox(20, prevButton, nextButton, submitButton);
        buttonBar.setAlignment(Pos.CENTER);

        prevButton.setOnAction(e -> navigatePrevious());
        nextButton.setOnAction(e -> navigateNext());
        submitButton.setOnAction(e -> submitQuiz());

        root.getChildren().addAll(quizTitleLabel, questionNumberLabel, new Separator(), questionTextLabel, optionsBox, buttonBar);
        displayQuestion(currentQuestionIndex);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    private RadioButton createRadioButton() {
        RadioButton rb = new RadioButton();
        rb.setToggleGroup(optionsGroup);
        rb.setFont(Font.font("Arial", 16));
        return rb;
    }

    private void displayQuestion(int index) {
        Question q = questions.get(index);
        questionNumberLabel.setText("Question " + (index + 1) + " of " + questions.size());
        questionTextLabel.setText(q.getQuestionText());
        optionA.setText(q.getOptionA());
        optionB.setText(q.getOptionB());
        optionC.setText(q.getOptionC());
        optionD.setText(q.getOptionD());

        optionsGroup.selectToggle(null);
        if (userAnswers[index] != null) {
            if (userAnswers[index].equals(optionA.getText())) optionA.setSelected(true);
            else if (userAnswers[index].equals(optionB.getText())) optionB.setSelected(true);
            else if (userAnswers[index].equals(optionC.getText())) optionC.setSelected(true);
            else if (userAnswers[index].equals(optionD.getText())) optionD.setSelected(true);
        }

        prevButton.setDisable(index == 0);
        nextButton.setVisible(index < questions.size() - 1);
        submitButton.setVisible(index == questions.size() - 1);
    }

    private void saveCurrentAnswer() {
        RadioButton selected = (RadioButton) optionsGroup.getSelectedToggle();
        if (selected != null) {
            userAnswers[currentQuestionIndex] = selected.getText();
        } else {
            userAnswers[currentQuestionIndex] = null; // Save null if no answer is selected
        }
    }

    private void navigateNext() {
        saveCurrentAnswer();
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            displayQuestion(currentQuestionIndex);
        }
    }

    private void navigatePrevious() {
        saveCurrentAnswer();
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayQuestion(currentQuestionIndex);
        }
    }

    private void submitQuiz() {
        saveCurrentAnswer();
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (userAnswers[i] != null && questions.get(i).isCorrectAnswer(userAnswers[i])) {
                score++;
            }
        }

        double percentage = (questions.size() > 0) ? (double) score / questions.size() * 100 : 0;
        String dateTaken = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        QuizResult result = new QuizResult(0, currentUser.getId(), quiz.getId(), quiz.getTitle(), score, questions.size(), percentage, dateTaken);
        dbManager.saveQuizResult(result);

        showResultsScreen(result);
    }

    private void showResultsScreen(QuizResult result) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        Label titleLabel = new Label("Quiz Completed!");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: white;");

        Label scoreLabel = new Label(String.format("Your Score: %d / %d", result.getScore(), result.getTotalQuestions()));
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreLabel.setStyle("-fx-text-fill: white;");

        Label percentageLabel = new Label(String.format("Percentage: %.2f%%", result.getPercentage()));
        percentageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        percentageLabel.setStyle("-fx-text-fill: white;");

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> mainApp.showMainMenu());
        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        root.getChildren().addAll(titleLabel, scoreLabel, percentageLabel, backButton);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
