import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class Image {
    private JFrame frame;

    public void showOpeningScreen() {
        frame = new JFrame("Opening Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700);

        JLabel label = new JLabel(new ImageIcon("/Users/riarao/IdeaProjects/game/src/opening.png"));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        frame.add(label);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Timer to wait 3 seconds then start the game
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                frame.dispose(); // close the opening image window
                startRobotGame(); // start the real game
            }
        }, 3000); // 3 seconds
    }

    private void startRobotGame() {
        JFrame gameFrame = new JFrame("Robot Survival Game");
        RobotSurvivalGame gamePanel = new RobotSurvivalGame();

        gameFrame.add(gamePanel);
        gameFrame.setSize(600, 400);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);

        gamePanel.requestFocusInWindow(); // So key presses work
    }
}
