import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.util.Collections;
import java.io.File;
import javax.swing.Timer;

class memory_game extends Frame implements ActionListener, Runnable {
    Label welcomeLabel, levelLabel, l1, l2, l3, pointsLabel;
    Button simpleBtn, mediumBtn, hardBtn;
    Panel p1, p5, levelPanel;
    JButton[] button;
    ImageIcon[] images;
    List<ImageIcon> randomizedImages;
    int gridSize, totalCards;
    int firstClicked = -1;
    boolean[] isFlipped;
    int moveCount = 0, mistakeCount = 0, points = 0;
    Thread timerThread, countdownThread;
    boolean running = true, levelSelected = false;
    int timeElapsed = 0;
    String imageFolder = "C:/Users/hp290/OneDrive/Documents/KC SY/JAVA/project";
    JButton backButton; 

    // Store references to final score label and buttons
    Label finalScoreLabel; // Store reference to final score label
    Button replayButton; // Store reference to replay button
    Button quitButton; // Store reference to quit button
    boolean canFlip = true;
    memory_game() {
        setTitle("Memory Game");
        setSize(500, 650);
        setVisible(true);
        setLocation(200, 100);
        setLayout(null);

        // Welcome Message
        showWelcomeMessage();

        // Level Selection UI
        addLevelSelection();
    }

    // Typewriter Effect for Welcome Message
    void showWelcomeMessage() {
        welcomeLabel = new Label(" ");
        welcomeLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        welcomeLabel.setAlignment(Label.CENTER);
        welcomeLabel.setBounds(50, 50, 400, 30);
        add(welcomeLabel);

        String message = "Welcome to the Memory Game!";
        new Thread(() -> {
            for (char c : message.toCharArray()) {
                try {
                    Thread.sleep(100);
                    welcomeLabel.setText(welcomeLabel.getText() + c);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Level Selection UI
    void addLevelSelection() {
        levelLabel = new Label("Choose a Level:");
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        levelLabel.setBounds(150, 150, 200, 30);
        add(levelLabel);

        levelPanel = new Panel(new GridLayout(1, 3, 10, 10));
        levelPanel.setBounds(100, 200, 300, 40);
        add(levelPanel);

        simpleBtn = new Button("Simple");
        mediumBtn = new Button("Medium");
        hardBtn = new Button("Hard");

        simpleBtn.addActionListener(this);
        mediumBtn.addActionListener(this);
        hardBtn.addActionListener(this);

        levelPanel.add(simpleBtn);
        levelPanel.add(mediumBtn);
        levelPanel.add(hardBtn);

        // Initialize and hide the back button
        backButton = new JButton("Back");
        backButton.setBounds(150, 620, 200, 30);
        backButton.setBackground(Color.BLUE);
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> showQuitConfirmationDialog());
        add(backButton);
        backButton.setVisible(false); // Hide the back button initially
    }

    // Set grid size based on user selection
    void setGridSize(String level) {
        if (level.equalsIgnoreCase("Simple")) {
            gridSize = 4;  
            totalCards = 16;  
        } else if (level.equalsIgnoreCase("Medium")) {
            gridSize = 5;  
            totalCards = 20;  
        } else if (level.equalsIgnoreCase("Hard")) {
            gridSize = 6;  
            totalCards = 36;  
        }

        levelPanel.setVisible(false);
        levelLabel.setVisible(false);
        levelSelected = true;
        backButton.setVisible(true); // Show the back button when the game starts
        startCountdown();
    }

    // Countdown before the game starts
    void startCountdown() {
        Label countdownLabel = new Label("3", Label.CENTER);
        countdownLabel.setFont(new Font("Monospaced", Font.BOLD, 50));
        countdownLabel.setBounds(150, 300, 200, 50);
        add(countdownLabel);
        validate();
        repaint();

        countdownThread = new Thread(() -> {
            try {
                for (int i = 3; i > 0; i--) {
                    countdownLabel.setText(String.valueOf(i));
                    Thread.sleep(1000);
                }
                countdownLabel.setText("Game Start!");
                countdownLabel.setFont(new Font("Monospaced", Font.BOLD, 30));
                countdownLabel.setForeground(Color.RED);
                Thread.sleep(1000);
                countdownLabel.setVisible(false);
                initializeGameGrid();  // Show the game grid
                startTimer();  // Start the game timer
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        countdownThread.start();
    }

    // Load and shuffle images
    void loadImages() {
        images = new ImageIcon[totalCards / 2]; // Store all images
        randomizedImages = new ArrayList<>();

        for (int i = 0; i < totalCards / 2; i++) {
            File imageFile = new File(imageFolder + "\\" + (i + 1) + ".png"); 

            if (imageFile.exists()) {
                images[i] = new ImageIcon(new ImageIcon(imageFile.getAbsolutePath()).getImage()
                        .getScaledInstance(80, 80, Image.SCALE_SMOOTH));
                System.out.println("Loaded Image: " + imageFile.getAbsolutePath()); 
            } else {
                System.out.println("Image Not Found: " + imageFile.getAbsolutePath());
            }
        }

        for (int i = 0; i < totalCards / 2; i++) {
            randomizedImages.add(images[i]); // Add first copy
            randomizedImages.add(images[i]); // Add second copy
        }
        Collections.shuffle(randomizedImages);
    }


    // Initialize the game grid
    void initializeGameGrid() {
        loadImages();

        // Labels for Mistakes, Moves, Time, and Points
        l1 = new Label("Mistakes: 0");
        l2 = new Label("Moves: 0");
        l3 = new Label("Time: 00:00");
        pointsLabel = new Label("Points: 0");

        l1.setBackground(Color.blue);
        l2.setBackground(Color.blue);
        l3.setBackground(Color.blue);
        pointsLabel.setBackground(Color.green);
        l1.setForeground(Color.white);
        l2.setForeground(Color.white);
        l3.setForeground(Color.white);
        pointsLabel.setForeground(Color.white);

        // Panel for Labels
        p5 = new Panel(new FlowLayout());
        p5.setBounds(50, 100, 400, 40);
        p5.add(pointsLabel);
        p5.add(l1);
        p5.add(l2);
        p5.add(l3);
        add(p5);

        // Panel for Images (Game Grid)
        p1 = new Panel(new GridLayout(gridSize, gridSize, 5, 5));
        p1.setBounds(50, 200, 400, 400);
        add(p1);

        button = new JButton[totalCards];
        isFlipped = new boolean[totalCards];

        for (int i = 0; i < totalCards; i++) {
            button[i] = new JButton("");
            int index = i;
            button[i].addActionListener(e -> handleClick(index));
            p1.add(button[i]);
        }

        validate();
        repaint();
    }

    // Show quit confirmation dialog
    private void showQuitConfirmationDialog() {
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Quit Game", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            // Return to level selection
            levelPanel.setVisible(true);
            levelLabel.setVisible(true);
            p1.setVisible(false); // Hide the game grid
            p5.setVisible(false); // Hide the score labels
            backButton.setVisible(false); // Hide the back button
        }
    }

    // Handle button click event
    private void handleClick(int index) {
        if (!canFlip ||isFlipped[index]) return; 

        // Flip the selected card (show image)
        button[index].setIcon(randomizedImages.get(index));
        isFlipped[index] = true;

        if (firstClicked == -1) {
            firstClicked = index; // Store first clicked card
        } else {
            moveCount++;
            l2.setText("Moves: " + moveCount);
        canFlip = false;

            // If images match, award 100 points and keep them flipped
            if (randomizedImages.get(firstClicked).equals(randomizedImages.get(index))) {  
                points += 100;
                pointsLabel.setText("Points: " + points);
                isFlipped[firstClicked] = true; // Keep them flipped
                isFlipped[index] = true; // Keep them flipped
                firstClicked = -1; // Reset for next turn
            canFlip = true;
            } else {  
                // Start negative marking after points are gained
                if (points > 0) {
                    points -= 10; // Deduct 10 points for wrong move
                    pointsLabel.setText("Points: " + points);
                }
                
                mistakeCount++;
                l1.setText("Mistakes: " + mistakeCount);

                // Flip them back after 1 second
                Timer timer = new Timer(1000, e -> {
                    button[firstClicked].setIcon(null);
                    button[index].setIcon(null);
                    isFlipped[firstClicked] = false;
                    isFlipped[index] = false;
                    firstClicked = -1;
                 canFlip = true;
                });
                timer.setRepeats(false);
                timer.start();
            }
        }

        // **Check if game is over**
        if (isGameOver()) {
            showFinalScore();
        }
    }

    private boolean isGameOver() {
        for (boolean flipped : isFlipped) {
            if (!flipped) return false; 
        }
        return true;
    }

    private void showFinalScore() {
        // Clear the game grid and labels
        p1.setVisible(false);
        p5.setVisible(false);
        backButton.setVisible(false); 
        
        // Create a label to show the final score
        finalScoreLabel = new Label("Final Score: " + points, Label.CENTER);
        finalScoreLabel.setFont(new Font("Arial", Font.BOLD, 30));
        finalScoreLabel.setBounds(100, 120, 300, 50);
        finalScoreLabel.setBackground(Color.BLUE);
        finalScoreLabel.setForeground(Color.WHITE);
        add(finalScoreLabel);

        // Create Replay button
        replayButton = new Button("Play Again");
        replayButton.setBounds(100, 200, 100, 30);
        replayButton.addActionListener(e -> replayGame());
        add(replayButton);

        // Create Quit button
        quitButton = new Button("Quit");
        quitButton.setBounds(300, 200, 100, 30);
        quitButton.addActionListener(e -> quitGame());
        add(quitButton);

        validate();
        repaint();
    }

    // Method to handle replaying the game
    private void replayGame() {
        // Remove final score label and buttons
        removeAllFinalScoreComponents();
        
        // Reset game state
        resetGameState();
        
        // Show level selection again
        levelPanel.setVisible(true);
        levelLabel.setVisible(true);
        p1.setVisible(false); // Hide the game grid
        p5.setVisible(false); // Hide the score labels
        backButton.setVisible(false); // Hide the back button
    }

    // Method to handle quitting the game
    private void quitGame() {
        System.exit(0); // Exit the application
    }

    // Method to reset game state
    private void resetGameState() {
        // Reset all variables to their initial state
        moveCount = 0;
        mistakeCount = 0;
        points = 0;
        timeElapsed = 0;
        firstClicked = -1;
        Arrays.fill(isFlipped, false);
        // Reset labels
        l1.setText("Mistakes: 0");
        l2.setText("Moves: 0");
        pointsLabel.setText("Points: 0");
        l3.setText("Time: 00:00");
    }

    // Method to remove final score components
    private void removeAllFinalScoreComponents() {
        // Remove final score label and buttons
        if (finalScoreLabel != null) {
            remove(finalScoreLabel);
        }
        if (replayButton != null) {
            remove(replayButton);
        }
        if (quitButton != null) {
            remove(quitButton);
        }
        validate();
        repaint();
    }

    void startTimer() {
        timerThread = new Thread(this);
        timerThread.start();
    }

    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
                timeElapsed++;
                l3.setText("Time: " + (timeElapsed / 60) + ":" + String.format("%02d", timeElapsed % 60));
            } catch (InterruptedException e) {
                System.out.println("Timer interrupted");
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == simpleBtn) {
            setGridSize("Simple");
        } else if (e.getSource() == mediumBtn) {
            setGridSize("Medium");
        } else if (e.getSource() == hardBtn) {
            setGridSize("Hard");
        }
    }

    public static void main(String[] args) {
        memory_game m=new memory_game();
    }
} 