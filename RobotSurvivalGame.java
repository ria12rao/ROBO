import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;



public class RobotSurvivalGame extends JPanel implements ActionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private Timer timer;
    private int robotX = 300, robotY = 300, robotSize = 30;
    private boolean up, down, left, right;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<BossRobot> bossRobots = new ArrayList<>();
    private boolean isGameOver = false;
    private int survivalTime = 0;
    private Random random = new Random();

    // Food Item
    private int foodX = 100, foodY = 100, foodSize = 20;
    private boolean foodCollected = false;
    private int foodRespawnTimer = 0;
    private final int FOOD_RESPAWN_TIME = 90; // 3 seconds

    // Level System
    private int currentLevel = 1;
    private int scoreForNextLevel = 300; // 10 seconds for first level
    private final int BOSS_LEVEL = 5; // Changed to 5
    private final int BASE_NEXT_LEVEL_SCORE = 300;
    private boolean showLevelUpMessage = false;
    private int levelUpMessageTimer = 0;
    private final int LEVEL_UP_MESSAGE_DURATION = 60; // 2 seconds
    private boolean isBossLevel = false;

    // Materials collection system
    private HashMap<Integer, Material> materials = new HashMap<>();
    private Material currentMaterial = null;
    private boolean materialCollected = false;
    private ArrayList<Integer> collectedMaterials = new ArrayList<>();

    // Player powerups based on materials
    private boolean hasWeapon = false;
    private int weaponCooldown = 0;
    private final int WEAPON_COOLDOWN_TIME = 30; // 1 second
    private ArrayList<Projectile> projectiles = new ArrayList<>();

    // Player lives
    private int lives = 3; // Start with 3 lives
    private int maxLives = 3; // Maximum lives
    private int invincibilityTimer = 0; // Player is invincible after being hit
    private final int INVINCIBILITY_TIME = 60; // 2 seconds

    // Countdown for game start
    private int countdown = 90; // 3 seconds (30 ticks per second)
    private boolean gameStarted = false;
    private boolean showGoScreen = false;
    private int goScreenCounter = 30; // 1 second

    public RobotSurvivalGame() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.BLACK);

        addKeyListener(this);
        setFocusable(true);

        // Initialize materials for levels 1-4
        initializeMaterials();

        // Initialize enemies and food after the panel is created
        spawnEnemies(getEnemyCountForLevel(currentLevel));
        resetFood();
        spawnMaterial();

        timer = new Timer(30, this);
        timer.start();

    }

    private void initializeMaterials() {
        materials.put(1, new Material("Power Core", new Color(255, 50, 50)));
        materials.put(2, new Material("Shield Module", new Color(50, 50, 255)));
        materials.put(3, new Material("Laser Emitter", new Color(50, 255, 50)));
        materials.put(4, new Material("Turbo Engine", new Color(255, 255, 50)));
    }

    private void spawnMaterial() {
        // Only spawn material if we're not at the boss level and haven't collected this level's material
        if (currentLevel < BOSS_LEVEL && !collectedMaterials.contains(currentLevel)) {
            currentMaterial = materials.get(currentLevel);
            materialCollected = false;

            // Make sure material doesn't spawn too close to the player or food
            int x, y;
            do {
                x = random.nextInt(500) + 50;
                y = random.nextInt(500) + 50;
            } while (isCloseToPlayer(x, y) || isCloseToFood(x, y));

            currentMaterial.x = x;
            currentMaterial.y = y;
        } else {
            currentMaterial = null;
        }
    }

    private boolean isCloseToPlayer(int x, int y) {
        int distance = (int) Math.sqrt(Math.pow(x - robotX, 2) + Math.pow(y - robotY, 2));
        return distance < 100; // Keep material at least 100 pixels from player
    }

    private boolean isCloseToFood(int x, int y) {
        int distance = (int) Math.sqrt(Math.pow(x - foodX, 2) + Math.pow(y - foodY, 2));
        return distance < 80; // Keep material at least 80 pixels from food
    }

    private int getEnemyCountForLevel(int level) {
        if (level == BOSS_LEVEL) {
            return 0; // No regular enemies in boss level
        }
        // Level 1: 5 enemies, level 2: 7 enemies, level 3: 9 enemies, etc.
        return 3 + (level * 2);
    }

    private float getEnemySpeedForLevel(int level) {
        // Base speed increases with level
        return 1.5f + (level * 0.3f) + random.nextFloat();
    }

    private void levelUp() {
        currentLevel++;
        showLevelUpMessage = true;
        levelUpMessageTimer = LEVEL_UP_MESSAGE_DURATION;

        // Check if we've reached the boss level
        if (currentLevel == BOSS_LEVEL) {
            isBossLevel = true;
            // Clear all regular enemies
            enemies.clear();
            // Spawn boss robot
            bossRobots.add(new BossRobot());

            // Enable weapon if player has collected all materials
            hasWeapon = collectedMaterials.size() >= BOSS_LEVEL - 1;
        } else {
            // Add new enemies based on the new level
            if (!isBossLevel) {
                int currentEnemyCount = enemies.size();
                int newEnemyCount = getEnemyCountForLevel(currentLevel);

                // Add new enemies if needed
                for (int i = currentEnemyCount; i < newEnemyCount; i++) {
                    enemies.add(new Enemy());
                }

                // Power up existing enemies
                for (Enemy enemy : enemies) {
                    enemy.speed = getEnemySpeedForLevel(currentLevel);
                }

                // Spawn new material for this level
                spawnMaterial();
            }
        }

        // Calculate score needed for next level (increases with each level)
        scoreForNextLevel = BASE_NEXT_LEVEL_SCORE + (currentLevel * 150);

        // Give player a bonus life when leveling up (if not at max)
        if (lives < maxLives) {
            lives++;
        }
    }

    private void spawnEnemies(int count) {
        enemies.clear();
        for (int i = 0; i < count; i++) {
            enemies.add(new Enemy());
        }
    }

    private void resetFood() {
        // Make sure food doesn't spawn too close to the player
        foodX = random.nextInt(500) + 50;  // Keep away from edges
        foodY = random.nextInt(500) + 50;  // Keep away from edges
        foodCollected = false;
    }

    private void resetGame() {
        robotX = 300;
        robotY = 300;
        lives = 3;
        survivalTime = 0;
        currentLevel = 1;
        scoreForNextLevel = BASE_NEXT_LEVEL_SCORE;
        isGameOver = false;
        foodCollected = false;
        isBossLevel = false;
        hasWeapon = false;
        enemies.clear();
        bossRobots.clear();
        projectiles.clear();
        collectedMaterials.clear();
        resetFood();
        spawnEnemies(getEnemyCountForLevel(currentLevel));
        spawnMaterial();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameStarted) {
            countdown--;
            if (countdown <= 0) {
                gameStarted = true;
                showGoScreen = true;
            }
        } else if (showGoScreen) {
            goScreenCounter--;
            if (goScreenCounter <= 0) {
                showGoScreen = false;
            }
        } else if (!isGameOver) {
            moveRobot();

            // Handle invincibility timer
            if (invincibilityTimer > 0) {
                invincibilityTimer--;
            }

            // Handle weapon cooldown
            if (weaponCooldown > 0) {
                weaponCooldown--;
            }

            // Handle level up message timer
            if (showLevelUpMessage) {
                levelUpMessageTimer--;
                if (levelUpMessageTimer <= 0) {
                    showLevelUpMessage = false;
                }
            }

            // Fire weapon if available (in boss level)
            if (isBossLevel && hasWeapon && weaponCooldown <= 0 && (up || down || left || right)) {
                fireWeapon();
                weaponCooldown = WEAPON_COOLDOWN_TIME;
            }

            // Update projectiles
            updateProjectiles();

            if (isBossLevel) {
                // Boss level logic
                for (int i = bossRobots.size() - 1; i >= 0; i--) {
                    BossRobot boss = bossRobots.get(i);
                    boss.moveTowards(robotX, robotY);

                    if (invincibilityTimer <= 0 && boss.collidesWith(robotX, robotY, robotSize)) {
                        // When player collides with boss robot
                        splitBossRobot(boss, i);
                        loseLife();
                        invincibilityTimer = INVINCIBILITY_TIME;
                        break; // Only process one collision at a time
                    }
                }

                // Check if all boss robots have been defeated to win the game
                if (bossRobots.isEmpty()) {
                    // Game completed
                    isGameOver = true;
                }
            } else {
                // Regular level logic
                for (Enemy enemy : enemies) {
                    enemy.moveTowards(robotX, robotY);
                    if (invincibilityTimer <= 0 && enemy.collidesWith(robotX, robotY, robotSize)) {
                        loseLife();
                        invincibilityTimer = INVINCIBILITY_TIME;
                        break; // Only process one collision at a time
                    }
                }
            }

            // Check if player collects the food
            if (!foodCollected &&
                    robotX < foodX + foodSize && robotX + robotSize > foodX &&
                    robotY < foodY + foodSize && robotY + robotSize > foodY) {
                foodCollected = true;
                restoreLife();
                foodRespawnTimer = FOOD_RESPAWN_TIME;
            }

            // Check if player collects the material
            if (currentMaterial != null && !materialCollected &&
                    robotX < currentMaterial.x + 25 && robotX + robotSize > currentMaterial.x &&
                    robotY < currentMaterial.y + 25 && robotY + robotSize > currentMaterial.y) {
                materialCollected = true;
                collectedMaterials.add(currentLevel);
                currentMaterial = null;
            }

            // Handle food respawn
            if (foodCollected) {
                foodRespawnTimer--;
                if (foodRespawnTimer <= 0) {
                    resetFood();
                }
            }

            survivalTime++;

            // Check for level up - only level up if material is collected for current level
            if (survivalTime % scoreForNextLevel == 0 && survivalTime > 0) {
                if (currentLevel < BOSS_LEVEL) {
                    // Only level up if material is collected
                    if (collectedMaterials.contains(currentLevel)) {
                        levelUp();
                    }
                } else {
                    levelUp();
                }
            }
        }
        repaint();
    }

    private void fireWeapon() {
        // Determine firing direction
        int dirX = 0;
        int dirY = 0;

        if (up) dirY = -1;
        if (down) dirY = 1;
        if (left) dirX = -1;
        if (right) dirX = 1;

        // Don't fire if no direction is pressed
        if (dirX == 0 && dirY == 0) return;

        // Get material-based colors
        Color projectileColor = new Color(255, 0, 0); // Default red

        if (collectedMaterials.contains(1)) {
            projectileColor = materials.get(1).color; // Power Core color
        } else if (collectedMaterials.contains(3)) {
            projectileColor = materials.get(3).color; // Laser Emitter color
        }

        // Create new projectile
        projectiles.add(new Projectile(
                robotX + robotSize/2 - 5,
                robotY + robotSize/2 - 5,
                dirX, dirY,
                projectileColor
        ));
    }

    private void updateProjectiles() {
        // Update and check collisions for projectiles
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update();

            // Remove if out of bounds
            if (p.x < 0 || p.x > getWidth() || p.y < 0 || p.y > getHeight()) {
                projectiles.remove(i);
                continue;
            }

            // Check collision with boss robots
            for (int j = bossRobots.size() - 1; j >= 0; j--) {
                BossRobot boss = bossRobots.get(j);
                if (p.collidesWith(boss.x, boss.y, boss.size)) {
                    // Hit the boss, split it
                    splitBossRobot(boss, j);
                    projectiles.remove(i);
                    break;
                }
            }
        }
    }

    private void splitBossRobot(BossRobot boss, int index) {
        // Remove the original boss
        bossRobots.remove(index);

        // Create two new smaller bosses if the original boss is big enough
        if (boss.size > 30) {
            int newSize = boss.size - 20;

            // Create first split boss
            BossRobot split1 = new BossRobot(boss.x - newSize, boss.y - newSize, newSize);

            // Create second split boss
            BossRobot split2 = new BossRobot(boss.x + newSize, boss.y + newSize, newSize);

            // Add the two new bosses
            bossRobots.add(split1);
            bossRobots.add(split2);
        }
    }

    public void moveRobot() {
        int baseSpeed = 5;
        int speed = baseSpeed;

        // Increase speed if player has the Turbo Engine material in boss level
        if (isBossLevel && collectedMaterials.contains(4)) {
            speed = baseSpeed * 3 / 2; // 50% speed boost
        }

        if (up) robotY -= speed;
        if (down) robotY += speed;
        if (left) robotX -= speed;
        if (right) robotX += speed;

        // Stay inside screen
        robotX = Math.max(0, Math.min(robotX, 570)); // 600 - robotSize
        robotY = Math.max(0, Math.min(robotY, 570)); // 600 - robotSize
    }

    public void loseLife() {
        // If player has Shield Module, have a chance to block damage
        if (isBossLevel && collectedMaterials.contains(2) && random.nextInt(10) < 3) {
            // 30% chance to block damage
            return;
        }

        lives--;
        if (lives <= 0) {
            isGameOver = true;
        }
    }

    public void restoreLife() {
        if (lives < maxLives) {
            lives++;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Background
        g.setColor(new Color(20, 20, 40)); // Darker blue background
        g.fillRect(0, 0, getWidth(), getHeight());

        // Add a grid pattern for visual appeal
        g.setColor(new Color(30, 30, 60));
        for (int i = 0; i < getWidth(); i += 30) {
            g.drawLine(i, 0, i, getHeight());
            g.drawLine(0, i, getWidth(), i);
        }

        if (!gameStarted) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 80));
            int secondsLeft = countdown / 30 + 1;
            g.drawString(String.valueOf(secondsLeft), getWidth()/2 - 20, getHeight()/2);
        } else if (showGoScreen) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("GO!", getWidth()/2 - 70, getHeight()/2);
        } else if (!isGameOver) {
            // Draw projectiles
            for (Projectile p : projectiles) {
                g.setColor(p.color);
                g.fillOval(p.x, p.y, 10, 10);
                // Add glow effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g.fillOval(p.x - 5, p.y - 5, 20, 20);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            // Draw the player robot
            if (invincibilityTimer == 0 || invincibilityTimer % 6 >= 3) {  // Blinking effect when invincible
                // Determine player robot color based on collected materials
                Color robotColor = new Color(0, 150, 255); // Default blue

                if (isBossLevel) {
                    // Create a gradient color based on collected materials
                    if (collectedMaterials.size() >= 2) {
                        robotColor = new Color(50, 200, 255); // Upgraded blue
                    }
                    if (collectedMaterials.size() >= 3) {
                        robotColor = new Color(100, 200, 255); // Even better blue
                    }
                    if (collectedMaterials.size() >= 4) {
                        robotColor = new Color(100, 255, 255); // Cyan for all materials
                    }
                }

                // Draw robot body with shadow
                g.setColor(new Color(0, 0, 0, 128)); // Shadow color
                g.fillRoundRect(robotX + 3, robotY + 3, robotSize, robotSize, 10, 10);

                // Draw robot body
                g.setColor(robotColor);
                g.fillRoundRect(robotX, robotY, robotSize, robotSize, 10, 10);

                // Draw robot face/details
                g.setColor(Color.WHITE);
                // Eyes
                g.fillOval(robotX + 7, robotY + 8, 6, 6);
                g.fillOval(robotX + robotSize - 13, robotY + 8, 6, 6);

                // Draw robot mouth
                g.setColor(Color.BLACK);
                g.drawLine(robotX + 8, robotY + 20, robotX + robotSize - 8, robotY + 20);

                // Draw antennas
                g.setColor(Color.GRAY);
                g.fillRect(robotX + 10, robotY - 5, 2, 5);
                g.fillRect(robotX + robotSize - 12, robotY - 5, 2, 5);
                g.setColor(Color.RED);
                g.fillOval(robotX + 9, robotY - 8, 4, 4);
                g.fillOval(robotX + robotSize - 13, robotY - 8, 4, 4);

                // Draw weapon if in boss level and has weapon
                if (isBossLevel && hasWeapon) {
                    g.setColor(new Color(255, 100, 100));
                    g.fillRect(robotX - 5, robotY + robotSize/2 - 2, 5, 4);
                    g.fillRect(robotX + robotSize, robotY + robotSize/2 - 2, 5, 4);
                }

                // Draw shield effect if has shield module
                if (isBossLevel && collectedMaterials.contains(2)) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g.setColor(new Color(100, 100, 255));
                    g.fillOval(robotX - 5, robotY - 5, robotSize + 10, robotSize + 10);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
            }

            if (isBossLevel) {
                // Draw boss robots
                for (BossRobot boss : bossRobots) {
                    // Boss robot gradients and effects
                    GradientPaint gradient = new GradientPaint(
                            boss.x, boss.y, new Color(120, 0, 0),
                            boss.x + boss.size, boss.y + boss.size, new Color(200, 0, 50)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(boss.x, boss.y, boss.size, boss.size, 15, 15);

                    // Draw metallic details
                    g.setColor(new Color(150, 150, 150));
                    g.fillRect(boss.x + boss.size/4, boss.y - 10, boss.size/2, 10);
                    g.fillRect(boss.x + boss.size/4, boss.y + boss.size, boss.size/2, 10);

                    // Draw eyes
                    g.setColor(new Color(255, 255, 0)); // Yellow eyes
                    g.fillOval(boss.x + boss.size/4, boss.y + boss.size/4, boss.size/6, boss.size/6);
                    g.fillOval(boss.x + boss.size - boss.size/4 - boss.size/6, boss.y + boss.size/4, boss.size/6, boss.size/6);

                    // Add glowing effect around eyes
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g.setColor(new Color(255, 255, 100));
                    g.fillOval(boss.x + boss.size/4 - 2, boss.y + boss.size/4 - 2, boss.size/6 + 4, boss.size/6 + 4);
                    g.fillOval(boss.x + boss.size - boss.size/4 - boss.size/6 - 2, boss.y + boss.size/4 - 2, boss.size/6 + 4, boss.size/6 + 4);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                    // Mouth
                    g.setColor(new Color(50, 50, 50));
                    g.fillRect(boss.x + boss.size/4, boss.y + boss.size/2, boss.size/2, boss.size/8);

                    // Teeth
                    g.setColor(Color.WHITE);
                    for (int i = 0; i < 4; i++) {
                        g.fillRect(boss.x + boss.size/4 + i * (boss.size/2)/4, boss.y + boss.size/2, boss.size/10, boss.size/16);
                    }
                }
            } else {
                // Draw regular enemies
                for (Enemy e : enemies) {
                    // Base robot body with gradient
                    GradientPaint gradient = new GradientPaint(
                            e.x, e.y, new Color(180, 30, 30),
                            e.x + e.size, e.y + e.size, new Color(220, 50, 50)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(e.x, e.y, e.size, e.size, 8, 8);

                    // Robot head/top
                    g.setColor(new Color(100, 100, 100));
                    g.fillRoundRect(e.x + 5, e.y - 10, e.size - 10, 15, 5, 5);

                    // Eyes
                    g.setColor(new Color(255, 255, 0)); // Yellow eyes
                    g.fillOval(e.x + 8, e.y + 8, 6, 6);
                    g.fillOval(e.x + e.size - 14, e.y + 8, 6, 6);

                    // Angry eyebrows
                    g.setColor(Color.BLACK);
                    g.drawLine(e.x + 5, e.y + 6, e.x + 13, e.y + 10);
                    g.drawLine(e.x + e.size - 5, e.y + 6, e.x + e.size - 13, e.y + 10);

                    // Mouth
                    g.drawLine(e.x + 10, e.y + 20, e.x + e.size - 10, e.y + 20);

                    // Mechanical arms
                    g.setColor(new Color(150, 150, 150));
                    g.fillRect(e.x - 5, e.y + e.size/2 - 3, 5, 6);
                    g.fillRect(e.x + e.size, e.y + e.size/2 - 3, 5, 6);
                }
            }

            // Draw food (power cell)
            if (!foodCollected) {
                // Draw power cell base
                g.setColor(new Color(50, 50, 50));
                g.fillRect(foodX, foodY, foodSize, foodSize);

                // Draw inner energy
                g.setColor(new Color(0, 255, 200));
                g.fillRect(foodX + 4, foodY + 4, foodSize - 8, foodSize - 8);

                // Add glow effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g.setColor(new Color(0, 255, 200));
                g.fillOval(foodX - 5, foodY - 5, foodSize + 10, foodSize + 10);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            // Draw the current material
            if (currentMaterial != null && !materialCollected) {
                // Create a metallic-looking material
                g.setColor(new Color(50, 50, 50)); // Dark border
                g.fillRect(currentMaterial.x - 2, currentMaterial.y - 2, 29, 29);

                // Material color
                g.setColor(currentMaterial.color);
                g.fillRect(currentMaterial.x, currentMaterial.y, 25, 25);

                // Add shine effect
                g.setColor(new Color(255, 255, 255, 80));
                g.fillRect(currentMaterial.x + 5, currentMaterial.y + 2, 15, 5);

                // Add glow effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                g.setColor(currentMaterial.color);
                g.fillOval(currentMaterial.x - 5, currentMaterial.y - 5, 35, 35);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            // Draw HUD background
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(5, 5, 250, 100);
            g.fillRect(getWidth() - 155, 5, 150, 70);

            // Draw survival time
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Survival Time: " + survivalTime / 30 + "s", 10, 25);

            // Draw level information
            g.setColor(Color.YELLOW);
            g.drawString("Level: " + currentLevel, 10, 50);

            // Special message for boss level
            if (isBossLevel) {
                g.setColor(Color.RED);
                if (hasWeapon) {
                    g.drawString("BOSS LEVEL - WEAPON ACTIVE!", 10, 75);
                } else {
                    g.drawString("BOSS LEVEL - FIND MATERIALS!", 10, 75);
                }
            } else {
                // Show current material objective
                Material levelMaterial = materials.get(currentLevel);
                if (levelMaterial != null) {
                    if (collectedMaterials.contains(currentLevel)) {
                        g.setColor(Color.GREEN);
                        g.drawString(levelMaterial.name + " - COLLECTED!", 10, 75);
                    } else {
                        g.setColor(Color.ORANGE);
                        g.drawString("Find the " + levelMaterial.name + "!", 10, 75);
                    }
                }
            }

            // Draw progress to next level
            // Draw progress to next level
            int progressToNextLevel = (survivalTime % scoreForNextLevel);
            int progressBarWidth = 100;
            int filledWidth = (int)((float)progressToNextLevel / scoreForNextLevel * progressBarWidth);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(10, 95, progressBarWidth, 10);
            g.setColor(Color.GREEN);
            g.fillRect(10, 95, filledWidth, 10);

            // Draw hearts for lives
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            for (int i = 0; i < maxLives; i++) {
                if (i < lives) {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.GRAY); // Empty hearts
                }
                g.drawString("♥", getWidth() - 40 - (i * 40), 30);
            }

            // Draw collected materials info
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Materials: " + collectedMaterials.size() + "/" + (BOSS_LEVEL - 1), getWidth() - 120, 60);

            // Draw level up message if active
            if (showLevelUpMessage) {
                // Semi-transparent background
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(getWidth()/2 - 200, getHeight()/2 - 100, 400, 200);

                g.setColor(new Color(255, 215, 0)); // Gold
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("LEVEL UP!", getWidth()/2 - 100, getHeight()/2 - 50);
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.drawString("Level " + currentLevel, getWidth()/2 - 40, getHeight()/2 - 20);

                if (isBossLevel) {
                    g.setColor(Color.RED);
                    g.setFont(new Font("Arial", Font.BOLD, 30));
                    g.drawString("BOSS LEVEL!", getWidth()/2 - 100, getHeight()/2 + 10);
                    g.setFont(new Font("Arial", Font.PLAIN, 20));

                    if (hasWeapon) {
                        g.setColor(Color.GREEN);
                        g.drawString("All materials collected!", getWidth()/2 - 100, getHeight()/2 + 40);
                        g.drawString("Use arrows to fire your weapon!", getWidth()/2 - 140, getHeight()/2 + 70);
                    } else {
                        g.setColor(Color.ORANGE);
                        g.drawString("Missing materials to build weapon!", getWidth()/2 - 140, getHeight()/2 + 40);
                        g.drawString("Try to survive the boss!", getWidth()/2 - 100, getHeight()/2 + 70);
                    }
                } else {
                    g.drawString("Enemies are faster!", getWidth()/2 - 80, getHeight()/2 + 10);
                    g.drawString("You got an extra life!", getWidth()/2 - 80, getHeight()/2 + 40);

                    // If we have a new material to find
                    Material levelMaterial = materials.get(currentLevel);
                    if (levelMaterial != null) {
                        g.setColor(levelMaterial.color);
                        g.drawString("Find the " + levelMaterial.name + "!", getWidth()/2 - 100, getHeight()/2 + 70);
                    }
                }
            }

        } else {
            // Game over screen (modified to show victory or defeat)
            if (isBossLevel && bossRobots.isEmpty()) {
                // Victory
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(new Color(50, 200, 50));
                g.setFont(new Font("Arial", Font.BOLD, 50));
                g.drawString("VICTORY!", getWidth()/2 - 130, getHeight()/2 - 50);
                g.setFont(new Font("Arial", Font.PLAIN, 30));
                g.setColor(Color.WHITE);
                g.drawString("You defeated the boss!", getWidth()/2 - 150, getHeight()/2);
                g.drawString("Survival Time: " + survivalTime / 30 + " seconds", getWidth()/2 - 170, getHeight()/2 + 40);
                g.drawString("Materials collected: " + collectedMaterials.size() + "/" + (BOSS_LEVEL - 1), getWidth()/2 - 150, getHeight()/2 + 80);
                g.drawString("Press SPACE to play again", getWidth()/2 - 180, getHeight()/2 + 120);
            } else {
                // Defeat
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 50));
                g.drawString("GAME OVER", getWidth()/2 - 150, getHeight()/2 - 50);
                g.setFont(new Font("Arial", Font.PLAIN, 30));
                g.setColor(Color.WHITE);
                g.drawString("You survived " + survivalTime / 30 + " seconds", getWidth()/2 - 170, getHeight()/2);
                g.drawString("Reached Level " + currentLevel, getWidth()/2 - 110, getHeight()/2 + 40);
                g.drawString("Materials collected: " + collectedMaterials.size() + "/" + (BOSS_LEVEL - 1), getWidth()/2 - 150, getHeight()/2 + 80);
                g.drawString("Press SPACE to play again", getWidth()/2 - 180, getHeight()/2 + 120);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameStarted && !showGoScreen) {
            if (isGameOver && key == KeyEvent.VK_SPACE) {
                resetGame();
            } else if (!isGameOver) {
                if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = true;
                if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = true;
                if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = true;
                if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // New Material class to represent collectible items
    class Material {
        String name;
        Color color;
        int x, y;

        public Material(String name, Color color) {
            this.name = name;
            this.color = color;
            this.x = 0;
            this.y = 0;
        }
    }

    // New Projectile class for boss level weapons
    class Projectile {
        int x, y;
        int dx, dy;
        int speed = 8;
        Color color;

        public Projectile(int x, int y, int dx, int dy, Color color) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
        }

        public void update() {
            x += dx * speed;
            y += dy * speed;
        }

        public boolean collidesWith(int targetX, int targetY, int targetSize) {
            return (x < targetX + targetSize &&
                    x + 10 > targetX &&
                    y < targetY + targetSize &&
                    y + 10 > targetY);
        }
    }

    class Enemy {
        int x, y, size = 30;
        float speed;

        public Enemy() {
            // Initialize with speed based on current level
            speed = getEnemySpeedForLevel(currentLevel);

            // Spawn enemies at the edge of the screen
            if (random.nextBoolean()) {
                // Spawn on left or right edge
                x = random.nextBoolean() ? -size : 600;
                y = random.nextInt(600);
            } else {
                // Spawn on top or bottom edge
                x = random.nextInt(600);
                y = random.nextBoolean() ? -size : 600;
            }
        }

        public void moveTowards(int targetX, int targetY) {
            // Calculate direction vector
            float dx = targetX - x;
            float dy = targetY - y;

            // Normalize the vector
            float length = (float) Math.sqrt(dx*dx + dy*dy);
            if (length > 0) {
                dx /= length;
                dy /= length;
            }

            // Move towards player
            x += dx * speed;
            y += dy * speed;
        }

        public boolean collidesWith(int rx, int ry, int rsize) {
            // More forgiving collision detection (smaller hitbox)
            int shrinkFactor = 6;
            int adjustedPlayerX = rx + shrinkFactor;
            int adjustedPlayerY = ry + shrinkFactor;
            int adjustedPlayerSize = rsize - (shrinkFactor * 2);

            return (x < adjustedPlayerX + adjustedPlayerSize &&
                    x + size > adjustedPlayerX &&
                    y < adjustedPlayerY + adjustedPlayerSize &&
                    y + size > adjustedPlayerY);
        }
    }

    class BossRobot {
        int x, y, size;
        float speed;

        public BossRobot() {
            // Create a large boss robot
            size = 90;
            speed = 1.0f;

            // Place the boss at a random edge
            if (random.nextBoolean()) {
                // Spawn on left or right edge
                x = random.nextBoolean() ? -size : 600;
                y = random.nextInt(600);
            } else {
                // Spawn on top or bottom edge
                x = random.nextInt(600);
                y = random.nextBoolean() ? -size : 600;
            }
        }

        // Constructor for split bosses
        public BossRobot(int newX, int newY, int newSize) {
            this.x = newX;
            this.y = newY;
            this.size = newSize;
            // Split bosses are faster
            this.speed = 1.2f + (90.0f - newSize) / 30.0f;

            // Make sure boss stays within screen bounds
            x = Math.max(-size/2, Math.min(x, 600 - size/2));
            y = Math.max(-size/2, Math.min(y, 600 - size/2));
        }

        public void moveTowards(int targetX, int targetY) {
            // Calculate direction vector
            float dx = targetX - x;
            float dy = targetY - y;

            // Normalize the vector
            float length = (float) Math.sqrt(dx*dx + dy*dy);
            if (length > 0) {
                dx /= length;
                dy /= length;
            }

            // Move towards player
            x += dx * speed;
            y += dy * speed;
        }

        public boolean collidesWith(int rx, int ry, int rsize) {
            // Boss collision detection
            return (x < rx + rsize &&
                    x + size > rx &&
                    y < ry + rsize &&
                    y + size > ry);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Robot Survival Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            RobotSurvivalGame game = new RobotSurvivalGame();
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}