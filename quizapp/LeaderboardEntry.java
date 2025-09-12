package com.quizapp;

public class LeaderboardEntry {
    private String username;
    private double averageScore;
    private int totalAttempts;
    private int totalScore;

    public LeaderboardEntry(String username, double averageScore, int totalAttempts, int totalScore) {
        this.username = username;
        this.averageScore = averageScore;
        this.totalAttempts = totalAttempts;
        this.totalScore = totalScore;
    }

    public String getUsername() { return username; }
    public double getAverageScore() { return averageScore; }
    public int getTotalAttempts() { return totalAttempts; }
    public int getTotalScore() { return totalScore; }
}
