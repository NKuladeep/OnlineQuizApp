# 🎯 Quiz Application (JavaFX + SQLite)

An interactive **Quiz Application** built with **JavaFX**, **SQLite**, and **Maven**.  
It supports multiple-choice quizzes, tracks results, and provides a user-friendly GUI interface.

---

## ✨ Features
- 📋 Display multiple-choice quiz questions
- 🎨 Modern JavaFX GUI interface
- 🗄️ SQLite database for storing questions and results
- ✅ Answer validation with score tracking
- 📊 Final result display at the end of the quiz
- 🔌 Maven-based project (easy to build & run)

---

## 🛠️ Tech Stack
- **Java 17+**
- **JavaFX 21** (Controls, FXML, Graphics)
- **SQLite (JDBC)**
- **Maven**

---

## 📂 Project Structure
```
onlinequizapplication/
├── src/main/java/com/quizapp/
│ ├── QuizApplication.java # Main entry point
│ ├── QuizManager.java # Handles quiz logic
│ ├── DatabaseManager.java # Database connection
│ ├── Question.java # Question model
│ ├── QuestionsDAO.java # DAO for fetching questions
│ ├── ResultsDAO.java # DAO for storing results
│ └── Main.java # (Optional CLI version)
├── src/main/resources/
│ └── quiz.db # SQLite database file
├── pom.xml # Maven dependencies
└── README.md # Project documentation
```
## ⚙️ Setup & Installation

### 1️⃣ Clone the repository
```sh
git clone https://github.com/your-username/quiz-application.git
cd quiz-application
```
2️⃣ Install dependencies

Maven will download all required dependencies automatically:
```sh
mvn clean install
```
3️⃣ Run the application
```sh
mvn clean javafx:run
```
💾 Database

Uses SQLite (quiz.db).

Example Questions Table schema:
```
CREATE TABLE questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id INTEGER,
    question_text TEXT NOT NULL,
    option1 TEXT NOT NULL,
    option2 TEXT NOT NULL,
    option3 TEXT NOT NULL,
    option4 TEXT NOT NULL,
    correct_option INTEGER NOT NULL
);

```
Example Results Table schema:
```
CREATE TABLE results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    quiz_id INTEGER,
    score INTEGER
);
```
📸 Screenshots
Quiz Window

### 🔹 Login Page
![Login Page](https://github.com/NKuladeep/OnlineQuizApp/blob/9b0dc81132a61490f11be148a4d245557f7e0e9d/Templates/Login%20Page.png)

### 🔹 Login with Credentials
![Login with Credentials](https://github.com/NKuladeep/OnlineQuizApp/blob/67bab37729b6029c011e284fe0af202c5dfebc40/Templates/Login%20with%20Credentials.png)

### 🔹 Main Menu
![Main Menu](https://github.com/NKuladeep/OnlineQuizApp/blob/4dae967d00bfe619f23a133a6b62a13f0a5b9378/Templates/Main%20Menu.png)

### 🔹 Quiz Selection
![Quiz Selection](https://github.com/NKuladeep/OnlineQuizApp/blob/4d406fcad55e26124cb6e78553f5b0b150620468/Templates/Quiz%20Selection.png)

### 🔹 Quiz Question
![Quiz Question](https://github.com/NKuladeep/OnlineQuizApp/blob/ec2faed9560ac68e35e2616e4be4797501de655e/Templates/Quiz%20Question.png)

### 🔹 Quiz Completed
![Quiz Completed](https://github.com/NKuladeep/OnlineQuizApp/blob/c7c7ad9e873e0d0fe17ef0a3ad688bf05df40dad/Templates/Quiz%20Completed.png)

🚀 Future Improvements
```
🔑 User login system

📊 Leaderboard & analytics

🌐 Online quiz integration (API-based)

🎨 Improved UI with CSS styling
```
👨‍💻 Author

Developed by Nageti Kuladeep
