package com.quizapp;

public class QuizResult {
    private int id;
    private int userId;
    private int quizId;
    private String quizTitle;
    private int score;
    private int totalQuestions;
    private double percentage;
    private String dateTaken;

    public QuizResult(int id, int userId, int quizId, String quizTitle,
                      int score, int totalQuestions, double percentage, String dateTaken) {
        this.id = id;
        this.userId = userId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.dateTaken = dateTaken;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getQuizId() { return quizId; }
    public String getQuizTitle() { return quizTitle; }
    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public double getPercentage() { return percentage; }
    public String getDateTaken() { return dateTaken; }
}
