package com.quizapp;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Optional;

public class QuizManager {

    private Stage primaryStage;
    private DatabaseManager dbManager;
    private QuizApplication mainApp;
    private User currentUser;

    private ListView<Quiz> quizListView;
    private TableView<Question> questionTableView;

    public QuizManager(Stage primaryStage, User currentUser, DatabaseManager dbManager, QuizApplication mainApp) {
        this.primaryStage = primaryStage;
        this.currentUser = currentUser;
        this.dbManager = dbManager;
        this.mainApp = mainApp;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f4f9;");

        // Top Title
        Label titleLabel = new Label("Quiz Management");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        root.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        // Left Panel: Quiz List
        VBox quizBox = new VBox(10);
        quizBox.setPadding(new Insets(10));
        Label quizLabel = new Label("Quizzes");
        quizLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        quizListView = new ListView<>();
        loadQuizzes();

        HBox quizButtons = new HBox(10,
                createButton("Add", e -> addQuiz()),
                createButton("Edit", e -> editQuiz()),
                createButton("Delete", e -> deleteQuiz())
        );
        quizBox.getChildren().addAll(quizLabel, quizListView, quizButtons);

        // Center Panel: Question Table
        VBox questionBox = new VBox(10);
        questionBox.setPadding(new Insets(10));
        Label questionLabel = new Label("Questions for Selected Quiz");
        questionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        setupQuestionTable();

        HBox questionButtons = new HBox(10,
                createButton("Add", e -> addQuestion()),
                createButton("Edit", e -> editQuestion()),
                createButton("Delete", e -> deleteQuestion())
        );
        questionBox.getChildren().addAll(questionLabel, questionTableView, questionButtons);

        quizListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadQuestionsForQuiz(newSelection);
                    } else {
                        questionTableView.getItems().clear();
                    }
                });

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> mainApp.showMainMenu());

        root.setLeft(quizBox);
        root.setCenter(questionBox);
        root.setBottom(backButton);
        BorderPane.setAlignment(backButton, Pos.CENTER);
        BorderPane.setMargin(backButton, new Insets(20, 0, 0, 0));

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
    }

    private void setupQuestionTable() {
        questionTableView = new TableView<>();
        TableColumn<Question, String> textCol = new TableColumn<>("Question Text");
        textCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        textCol.setPrefWidth(400);

        TableColumn<Question, String> answerCol = new TableColumn<>("Correct Answer");
        answerCol.setCellValueFactory(new PropertyValueFactory<>("correctAnswer"));

        questionTableView.getColumns().addAll(textCol, answerCol);
    }

    private void loadQuizzes() {
        quizListView.setItems(FXCollections.observableArrayList(dbManager.getAllQuizzes()));
    }

    private void loadQuestionsForQuiz(Quiz quiz) {
        questionTableView.setItems(FXCollections.observableArrayList(dbManager.getQuestionsForQuiz(quiz.getId())));
    }

    private void addQuiz() {
        showQuizDialog(null).ifPresent(quiz -> {
            if (dbManager.addQuiz(quiz)) {
                loadQuizzes();
            } else {
                showAlert("Error", "Failed to add the quiz.");
            }
        });
    }

    private void editQuiz() {
        Quiz selected = quizListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a quiz to edit.");
            return;
        }
        showQuizDialog(selected).ifPresent(quiz -> {
            if (dbManager.updateQuiz(quiz)) {
                loadQuizzes();
            } else {
                showAlert("Error", "Failed to update the quiz.");
            }
        });
    }

    private void deleteQuiz() {
        Quiz selected = quizListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a quiz to delete.");
            return;
        }
        if (showConfirmation("Delete Quiz", "Are you sure you want to delete this quiz and all its questions?")) {
            if (dbManager.deleteQuiz(selected.getId())) {
                loadQuizzes();
                questionTableView.getItems().clear();
            } else {
                showAlert("Error", "Failed to delete the quiz.");
            }
        }
    }

    private void addQuestion() {
        Quiz selectedQuiz = quizListView.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            showAlert("No Quiz Selected", "Please select a quiz to add a question to.");
            return;
        }
        showQuestionDialog(null, selectedQuiz.getId()).ifPresent(question -> {
            if (dbManager.addQuestion(question)) {
                loadQuestionsForQuiz(selectedQuiz);
            } else {
                showAlert("Error", "Failed to add question.");
            }
        });
    }

    private void editQuestion() {
        Question selected = questionTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a question to edit.");
            return;
        }
        showQuestionDialog(selected, selected.getQuizId()).ifPresent(question -> {
            if(dbManager.updateQuestion(question)) {
                loadQuestionsForQuiz(quizListView.getSelectionModel().getSelectedItem());
            } else {
                showAlert("Error", "Failed to update question.");
            }
        });
    }

    private void deleteQuestion() {
        Question selected = questionTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a question to delete.");
            return;
        }
        if (showConfirmation("Delete Question", "Are you sure you want to delete this question?")) {
            if (dbManager.deleteQuestion(selected.getId())) {
                loadQuestionsForQuiz(quizListView.getSelectionModel().getSelectedItem());
            } else {
                showAlert("Error", "Failed to delete question.");
            }
        }
    }

    private Optional<Quiz> showQuizDialog(Quiz quiz) {
        Dialog<Quiz> dialog = new Dialog<>();
        dialog.setTitle(quiz == null ? "Add New Quiz" : "Edit Quiz");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField titleField = new TextField(quiz == null ? "" : quiz.getTitle());
        TextArea descriptionArea = new TextArea(quiz == null ? "" : quiz.getDescription());

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType && !titleField.getText().trim().isEmpty()) {
                if (quiz == null) {
                    return new Quiz(0, titleField.getText(), descriptionArea.getText(), currentUser.getId());
                } else {
                    quiz.setTitle(titleField.getText());
                    quiz.setDescription(descriptionArea.getText());
                    return quiz;
                }
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private Optional<Question> showQuestionDialog(Question question, int quizId) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle(question == null ? "Add Question" : "Edit Question");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea questionText = new TextArea(question == null ? "" : question.getQuestionText());
        questionText.setWrapText(true);
        TextField optA = new TextField(question == null ? "" : question.getOptionA());
        TextField optB = new TextField(question == null ? "" : question.getOptionB());
        TextField optC = new TextField(question == null ? "" : question.getOptionC());
        TextField optD = new TextField(question == null ? "" : question.getOptionD());

        ComboBox<String> correctAnswer = new ComboBox<>(FXCollections.observableArrayList("A", "B", "C", "D"));
        correctAnswer.setPromptText("Select Correct Answer");
        if (question != null) {
            // This logic needs to find which letter corresponds to the saved answer text
            if (question.getCorrectAnswer().equals(question.getOptionA())) correctAnswer.setValue("A");
            else if (question.getCorrectAnswer().equals(question.getOptionB())) correctAnswer.setValue("B");
            else if (question.getCorrectAnswer().equals(question.getOptionC())) correctAnswer.setValue("C");
            else if (question.getCorrectAnswer().equals(question.getOptionD())) correctAnswer.setValue("D");
        }


        grid.add(new Label("Question:"), 0, 0); grid.add(questionText, 1, 0);
        grid.add(new Label("Option A:"), 0, 1); grid.add(optA, 1, 1);
        grid.add(new Label("Option B:"), 0, 2); grid.add(optB, 1, 2);
        grid.add(new Label("Option C:"), 0, 3); grid.add(optC, 1, 3);
        grid.add(new Label("Option D:"), 0, 4); grid.add(optD, 1, 4);
        grid.add(new Label("Correct Answer:"), 0, 5); grid.add(correctAnswer, 1, 5);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Basic validation
                if (questionText.getText().trim().isEmpty() ||
                        optA.getText().trim().isEmpty() || optB.getText().trim().isEmpty() ||
                        optC.getText().trim().isEmpty() || optD.getText().trim().isEmpty() ||
                        correctAnswer.getValue() == null) {

                    showAlert("Validation Error", "All fields must be filled out.");
                    return null; // Prevents dialog from closing
                }

                int id = (question == null) ? 0 : question.getId();
                String correctTextValue;
                switch (correctAnswer.getValue()) {
                    case "A": correctTextValue = optA.getText(); break;
                    case "B": correctTextValue = optB.getText(); break;
                    case "C": correctTextValue = optC.getText(); break;
                    case "D": correctTextValue = optD.getText(); break;
                    default: return null;
                }

                return new Question(id, quizId, questionText.getText(), optA.getText(), optB.getText(),
                        optC.getText(), optD.getText(), correctTextValue);
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setOnAction(handler);
        return btn;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

