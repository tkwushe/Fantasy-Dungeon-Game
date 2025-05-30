package com.game.puzzle;

import java.io.Serial;
import java.io.Serializable;
import java.util.Random;

/**
 * The Puzzle class represents an interactive puzzle in the game.
 */
public class Puzzle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String question;
    private String answer;
    private String description;
    private boolean solved;

    private static float difficultyModifier = 1.0f;

    public Puzzle(String question, String answer, String description) {
        this.question = question;
        this.answer = answer;
        this.description = description;
        this.solved = false;
    }



    /**
     * Gets the description of the puzzle.
     *
     * @return The description of the puzzle.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if the user's answer is correct.
     *
     * @param userAnswer The user's answer.
     * @return True if the user's answer is correct, false otherwise.
     */
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null) {
            return false;
        }
        return userAnswer.trim().equalsIgnoreCase(answer.trim());
    }

    /**
     * Checks if the puzzle is solved.
     *
     * @return True if the puzzle is solved, false otherwise.
     */
    public boolean isSolved() {
        return solved;
    }

    /**
     * Sets the solved status of the puzzle.
     *
     * @param solved The new solved status.
     */
    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    /**
     * Generates a random puzzle.
     *
     * @return A Puzzle object.
     */
    public static Puzzle generateRandomPuzzle() {
        Random random = new Random();
        int puzzleType = random.nextInt(3);
        String question, answer, description;

        // Adjust difficulty based on modifier
        int difficultyAdjustment = (int)(10 * difficultyModifier);

        switch (puzzleType) {
            case 0:
                int a = random.nextInt(10 + difficultyAdjustment) + 1;
                int b = random.nextInt(10 + difficultyAdjustment) + 1;
                question = "What is " + a + " + " + b + "?";
                answer = String.valueOf(a + b);
                description = "A math puzzle";
                break;
            case 1:
                String[] colors = {"red", "blue", "green", "yellow"};
                String color = colors[random.nextInt(colors.length)];
                question = "What color am I thinking of? It rhymes with: " + generateRhyme(color);
                answer = color;
                description = "A color-guessing puzzle";
                break;
            case 2:
            default:
                int number = random.nextInt(100) + 1;
                question = "I'm thinking of a number between 1 and 100. Is it odd or even?";
                answer = (number % 2 == 0) ? "even" : "odd";
                description = "An odd-or-even guessing puzzle";
        }

        if (question == null || answer == null) {
            throw new IllegalStateException("Failed to generate puzzle");
        }

        return new Puzzle(question, answer, description);
    }

    private static String generateRhyme(String color) {
        switch (color) {
            case "red": return "bed";
            case "blue": return "shoe";
            case "green": return "bean";
            case "yellow": return "mellow";
            default: return "unknown";
        }
    }

    /**
     * Gets the question of the puzzle.
     *
     * @return The puzzle question.
     */
    public String getQuestion() {
        return question;
    }

    /**
     * @param question the question to set
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * @return String return the answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * @param answer the answer to set
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public static void setDifficultyModifier(float modifier) {
        difficultyModifier = modifier;
    }

}
