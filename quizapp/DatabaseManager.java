package com.quizapp;

import java.sql.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.Base64;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:quiz_app.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                createTables(conn);
                createDefaultAdmin(conn);
            }
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    private void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                email TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                is_admin BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        stmt.execute(createUsersTable);

        String createQuizzesTable = """
            CREATE TABLE IF NOT EXISTS quizzes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                created_by INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (created_by) REFERENCES users(id)
            )
        """;
        stmt.execute(createQuizzesTable);

        String createQuestionsTable = """
            CREATE TABLE IF NOT EXISTS questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                quiz_id INTEGER NOT NULL,
                question_text TEXT NOT NULL,
                option_a TEXT NOT NULL,
                option_b TEXT NOT NULL,
                option_c TEXT NOT NULL,
                option_d TEXT NOT NULL,
                correct_answer TEXT NOT NULL,
                FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
            )
        """;
        stmt.execute(createQuestionsTable);

        String createQuizResultsTable = """
            CREATE TABLE IF NOT EXISTS quiz_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                quiz_id INTEGER NOT NULL,
                quiz_title TEXT NOT NULL,
                score INTEGER NOT NULL,
                total_questions INTEGER NOT NULL,
                percentage REAL NOT NULL,
                date_taken TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
            )
        """;
        stmt.execute(createQuizResultsTable);
        stmt.close();
    }

    private void createDefaultAdmin(Connection conn) {
        // Check if admin exists
        String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkAdmin)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Admin does not exist, create it
                registerUser("admin", "admin@quizapp.com", "admin123", true);
                System.out.println("Default admin user 'admin' with password 'admin123' created.");
            }
        } catch (SQLException e) {
            System.err.println("Error creating default admin: " + e.getMessage());
        }
    }

    // --- Authentication Methods ---

    public boolean registerUser(String username, String email, String password, boolean isAdmin) {
        String checkUser = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertUser = "INSERT INTO users (username, email, password_hash, salt, is_admin) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUser)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Username already exists
                }
            }

            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);

            try (PreparedStatement insertStmt = conn.prepareStatement(insertUser)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, email);
                insertStmt.setString(3, hashedPassword);
                insertStmt.setString(4, salt);
                insertStmt.setBoolean(5, isAdmin);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("User registration error: " + e.getMessage());
            return false;
        }
    }

    public User authenticateUser(String username, String password) {
        String query = "SELECT id, username, email, password_hash, salt, is_admin FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                String inputHash = hashPassword(password, salt);
                if (storedHash.equals(inputHash)) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getBoolean("is_admin")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // --- Quiz CRUD Methods ---

    public List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT id, title, description, created_by FROM quizzes";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                quizzes.add(new Quiz(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("created_by")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all quizzes: " + e.getMessage());
        }
        return quizzes;
    }

    public boolean addQuiz(Quiz quiz) {
        String sql = "INSERT INTO quizzes(title, description, created_by) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, quiz.getTitle());
            pstmt.setString(2, quiz.getDescription());
            pstmt.setInt(3, quiz.getCreatedBy());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding quiz: " + e.getMessage());
            return false;
        }
    }

    public boolean updateQuiz(Quiz quiz) {
        String sql = "UPDATE quizzes SET title = ?, description = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, quiz.getTitle());
            pstmt.setString(2, quiz.getDescription());
            pstmt.setInt(3, quiz.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating quiz: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteQuiz(int quizId) {
        String sql = "DELETE FROM quizzes WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quizId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting quiz: " + e.getMessage());
            return false;
        }
    }

    // --- Question CRUD Methods ---

    public List<Question> getQuestionsForQuiz(int quizId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE quiz_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quizId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("id"), rs.getInt("quiz_id"), rs.getString("question_text"),
                        rs.getString("option_a"), rs.getString("option_b"),
                        rs.getString("option_c"), rs.getString("option_d"),
                        rs.getString("correct_answer")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching questions: " + e.getMessage());
        }
        return questions;
    }

    public boolean addQuestion(Question q) {
        String sql = "INSERT INTO questions(quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, q.getQuizId());
            pstmt.setString(2, q.getQuestionText());
            pstmt.setString(3, q.getOptionA());
            pstmt.setString(4, q.getOptionB());
            pstmt.setString(5, q.getOptionC());
            pstmt.setString(6, q.getOptionD());
            pstmt.setString(7, q.getCorrectAnswer());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding question: " + e.getMessage());
            return false;
        }
    }

    public boolean updateQuestion(Question q) {
        String sql = "UPDATE questions SET question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_answer=? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, q.getQuestionText());
            pstmt.setString(2, q.getOptionA());
            pstmt.setString(3, q.getOptionB());
            pstmt.setString(4, q.getOptionC());
            pstmt.setString(5, q.getOptionD());
            pstmt.setString(6, q.getCorrectAnswer());
            pstmt.setInt(7, q.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating question: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteQuestion(int questionId) {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting question: " + e.getMessage());
            return false;
        }
    }

    // --- Result & Leaderboard Methods ---

    public boolean saveQuizResult(QuizResult result) {
        String sql = "INSERT INTO quiz_results(user_id, quiz_id, quiz_title, score, total_questions, percentage, date_taken) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, result.getUserId());
            pstmt.setInt(2, result.getQuizId());
            pstmt.setString(3, result.getQuizTitle());
            pstmt.setInt(4, result.getScore());
            pstmt.setInt(5, result.getTotalQuestions());
            pstmt.setDouble(6, result.getPercentage());
            pstmt.setString(7, result.getDateTaken());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving quiz result: " + e.getMessage());
            return false;
        }
    }

    public List<QuizResult> getUserQuizResults(int userId) {
        List<QuizResult> results = new ArrayList<>();
        String sql = "SELECT * FROM quiz_results WHERE user_id = ? ORDER BY date_taken DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(new QuizResult(
                        rs.getInt("id"), rs.getInt("user_id"), rs.getInt("quiz_id"),
                        rs.getString("quiz_title"), rs.getInt("score"),
                        rs.getInt("total_questions"), rs.getDouble("percentage"),
                        rs.getString("date_taken")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user quiz results: " + e.getMessage());
        }
        return results;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = """
            SELECT
                u.username,
                AVG(qr.percentage) as average_score,
                COUNT(qr.id) as total_attempts,
                SUM(qr.score) as total_score
            FROM quiz_results qr
            JOIN users u ON qr.user_id = u.id
            GROUP BY u.username
            ORDER BY average_score DESC
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                entries.add(new LeaderboardEntry(
                        rs.getString("username"),
                        rs.getDouble("average_score"),
                        rs.getInt("total_attempts"),
                        rs.getInt("total_score")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching leaderboard: " + e.getMessage());
        }
        return entries;
    }
}
