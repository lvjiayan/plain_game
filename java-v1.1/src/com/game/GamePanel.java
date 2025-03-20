package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int DELAY = 16;

    private GameObject player;
    private List<GameObject> aiList;  // 替换原来的单个AI对象
    private List<GameObject> playerBullets;
    private List<GameObject> aiBullets;
    private Timer timer;
    private Random random;
    private int playerHits = 0;
    private int aiDefeated = 0;
    private int collisionHits = 0;  // 新增：与AI碰撞的次数
    private long lastShootTime = 0;
    private boolean up, down, left, right, shooting;
    private long aiLastShot = 0;
    private long lastAISpawnTime = 0;
    private static final int MIN_AI_SPAWN_INTERVAL = 2000; // 最小生成间隔(毫秒)
    private static final int MAX_AI_SPAWN_INTERVAL = 5000; // 最大生成间隔(毫秒)
    private static final int MAX_AI_COUNT = 6; // 最大AI数量
    private int playerHealth = 5;      // 初始血量5格
    private int lives = 3;             // 初始3条命
    private boolean gameOver = false;   // 游戏结束标志
    private GamepadManager gamepad;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        random = new Random();
        initGame();
        gamepad = new GamepadManager();
    }

    private void initGame() {
        player = new GameObject(WIDTH/2, HEIGHT-100, 40, 40, Color.BLUE, 5);
        aiList = new ArrayList<>();
        playerBullets = new ArrayList<>();
        aiBullets = new ArrayList<>();
        timer = new Timer(DELAY, this);
        timer.start();
    }

    private void spawnNewAI() {
        int x = random.nextInt(WIDTH - 40);
        GameObject newAI = new GameObject(x, 50, 40, 40, new Color(205, 133, 63), 2);
        
        // 20%概率生成特殊AI
        if (random.nextInt(100) < 20) {
            newAI.setSpecialAI(true);
            // 特殊AI使用不同的颜色（深红色）
            newAI.color = new Color(139, 0, 0);
        }
        
        aiList.add(newAI);
    }

    private void updateAISpawning() {
        long currentTime = System.currentTimeMillis();
        // 移除所有失活的AI
        aiList.removeIf(ai -> !ai.isActive());
        
        // 只在活跃AI数量小于最大限制时生成新AI
        if (currentTime - lastAISpawnTime > MIN_AI_SPAWN_INTERVAL + random.nextInt(MAX_AI_SPAWN_INTERVAL - MIN_AI_SPAWN_INTERVAL)) {
            if (aiList.size() < MAX_AI_COUNT) {
                spawnNewAI();
                lastAISpawnTime = currentTime;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (gameOver) {
            drawGameOver(g);
            return;
        }

        if (player.isActive()) {
            drawGameObject(g, player);
        }
        
        for (GameObject ai : aiList) {
            if (ai.isActive()) {
                drawGameObject(g, ai);
            }
        }
        
        for (GameObject bullet : playerBullets) {
            drawGameObject(g, bullet);
        }
        
        for (GameObject bullet : aiBullets) {
            drawGameObject(g, bullet);
        }
        
        // 先画生命条，再画血条
        drawLivesBar(g);
        drawHealthBar(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Scores: " + aiDefeated, WIDTH - 150, 30);
    }

    private void drawHealthBar(Graphics g) {
        int barWidth = 15;
        int barHeight = 10;
        int startX = 60;          // 向右移动，为标题腾出空间
        int startY = 25;
        int padding = 2;

        // 绘制标题
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Blood", 10, startY + barHeight - 1);  // -1微调使文字垂直对齐

        // 绘制血条背景
        g.setColor(Color.GRAY);
        for (int i = 0; i < 5; i++) {
            g.fillRect(startX + i * (barWidth + padding), startY, barWidth, barHeight);
        }

        // 绘制当前血量
        g.setColor(Color.RED);
        for (int i = 0; i < playerHealth; i++) {
            g.fillRect(startX + i * (barWidth + padding), startY, barWidth, barHeight);
        }
    }

    private void drawLivesBar(Graphics g) {
        int barWidth = 15;
        int barHeight = 10;
        int startX = 60;          // 与血条对齐
        int startY = 10;
        int padding = 2;

        // 绘制标题
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Life", 10, startY + barHeight - 1);  // -1微调使文字垂直对齐

        // 绘制生命条背景
        g.setColor(Color.GRAY);
        for (int i = 0; i < 3; i++) {
            g.fillRect(startX + i * (barWidth + padding), startY, barWidth, barHeight);
        }

        // 绘制当前生命
        g.setColor(Color.GREEN);
        for (int i = 0; i < lives; i++) {
            g.fillRect(startX + i * (barWidth + padding), startY, barWidth, barHeight);
        }
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String gameOverText = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int textX = (WIDTH - fm.stringWidth(gameOverText)) / 2;
        int textY = HEIGHT / 2;
        g.drawString(gameOverText, textX, textY);
    }

    private void drawGameObject(Graphics g, GameObject obj) {
        g.setColor(obj.getColor());
        g.fillRect(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updateGame();
        }
        repaint();
    }

    private void updateGame() {
        handlePlayerInput();
        updateAIs();
        updateAISpawning();
        updateBullets();
        checkCollisions();
    }

    private void handlePlayerInput() {
        // Handle keyboard input
        if (up && player.getY() > 0) {
            player.move(0, -player.speed);
        }
        if (down && player.getY() < HEIGHT - player.getHeight()) {
            player.move(0, player.speed);
        }
        if (left && player.getX() > 0) {
            player.move(-player.speed, 0);
        }
        if (right && player.getX() < WIDTH - player.getWidth()) {
            player.move(player.speed, 0);
        }
        
        // Handle gamepad input if available
        if (gamepad != null && gamepad.isConnected()) {
            gamepad.poll();
            
            // Handle X axis movement
            float xAxis = gamepad.getXAxis();
            if (Math.abs(xAxis) > 0) {
                int movement = (int)(xAxis * player.speed);
                if (player.getX() + movement >= 0 && player.getX() + movement <= WIDTH - player.getWidth()) {
                    player.move(movement, 0);
                }
            }
            
            // Handle Y axis movement
            float yAxis = gamepad.getYAxis();
            if (Math.abs(yAxis) > 0) {
                int movement = (int)(yAxis * player.speed);
                if (player.getY() + movement >= 0 && player.getY() + movement <= HEIGHT - player.getHeight()) {
                    player.move(0, movement);
                }
            }
            
            if (gamepad.isShootPressed() && System.currentTimeMillis() - lastShootTime > 300) {
                firePlayerBullet();
                lastShootTime = System.currentTimeMillis();
            }
        }

        // Handle keyboard shooting
        if (shooting && System.currentTimeMillis() - lastShootTime > 300) {
            firePlayerBullet();
            lastShootTime = System.currentTimeMillis();
        }
    }

    private void updateAIs() {
        for (GameObject ai : aiList) {
            if (ai.isActive()) {
                // Move AI down slowly
                ai.move(0, 1);
                
                // AI shooting logic
                if (ai.getY() < HEIGHT * 2 / 3) {
                    if (ai.isSpecialAI()) {
                        // 特殊AI的射击判定
                        if (random.nextInt(100) < 2) {
                            fireSpecialAIBullets(ai);
                        }
                    } else {
                        // 普通AI的射击判定
                        if (random.nextInt(100) < 2) {
                            aiShoot(ai);
                        }
                    }
                }
                
                if (ai.getY() > HEIGHT) {
                    ai.setActive(false);
                }
            }
        }
    }

    // 新增：特殊AI的三发子弹发射方法
    private void fireSpecialAIBullets(GameObject ai) {
        int bulletSpeed = 6;
        // 发射三发子弹：左下、正下、右下
        double[] angles = {-45, 0, 45}; // 角度：左下45度，正下，右下45度
        
        for (double angle : angles) {
            // 将角度转换为弧度
            double radian = Math.toRadians(angle);
            // 计算x和y方向的速度分量
            int velocityX = (int)(Math.sin(radian) * bulletSpeed);
            int velocityY = (int)(Math.cos(radian) * bulletSpeed);
            
            GameObject bullet = new GameObject(
                ai.getX() + ai.getWidth()/2 - 2,
                ai.getY() + ai.getHeight(),
                4, 10,
                Color.ORANGE, // 特殊AI的子弹使用橙色
                bulletSpeed
            );
            bullet.setVelocity(velocityX, velocityY);
            aiBullets.add(bullet);
        }
    }

    private void firePlayerBullet() {
        GameObject bullet = new GameObject(
            player.getX() + player.getWidth()/2 - 2,
            player.getY(),
            4, 10,
            Color.CYAN,
            10
        );
        playerBullets.add(bullet);
    }

    private void aiShoot(GameObject ai) {
        // 计算方向向量
        double dx = player.getX() - ai.getX();
        double dy = player.getY() - ai.getY();
        
        // 标准化方向向量
        double distance = Math.sqrt(dx * dx + dy * dy);
        dx = dx / distance;
        dy = dy / distance;
        
        // 设置子弹速度
        int bulletSpeed = 8;
        int velocityX = (int)(dx * bulletSpeed);
        int velocityY = (int)(dy * bulletSpeed);
        
        GameObject bullet = new GameObject(
            ai.getX() + ai.getWidth()/2 - 2,
            ai.getY() + ai.getHeight(),
            4, 10,
            Color.RED,
            bulletSpeed
        );
        // 设置子弹的速度向量
        bullet.setVelocity(velocityX, velocityY);
        aiBullets.add(bullet);
    }

    private void updateBullets() {
        // 更新玩家子弹
        for (GameObject bullet : new ArrayList<>(playerBullets)) {
            bullet.move(0, -bullet.speed);
            if (bullet.getY() + bullet.getHeight() < 0) {
                playerBullets.remove(bullet);
            }
        }

        // 更新AI子弹 - 使用速度向量移动
        for (GameObject bullet : new ArrayList<>(aiBullets)) {
            bullet.move(bullet.getVelocityX(), bullet.getVelocityY());
            if (bullet.getY() > HEIGHT || bullet.getY() < 0 || 
                bullet.getX() > WIDTH || bullet.getX() < 0) {
                aiBullets.remove(bullet);
            }
        }
    }

    private void checkCollisions() {
        // 检查玩家子弹击中AI
        for (GameObject bullet : new ArrayList<>(playerBullets)) {
            for (GameObject ai : aiList) {
                if (ai.isActive() && bullet.getBounds().intersects(ai.getBounds())) {
                    ai.setActive(false);
                    aiDefeated++;
                    playerBullets.remove(bullet);
                    break;
                }
            }
        }

        // 检查AI子弹击中玩家
        for (GameObject bullet : new ArrayList<>(aiBullets)) {
            if (bullet.getBounds().intersects(player.getBounds())) {
                playerHealth--;
                aiBullets.remove(bullet);
                if (playerHealth <= 0) {
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    } else {
                        playerHealth = 5; // 重置血量
                    }
                }
                break;
            }
        }

        // 检查AI与玩家的碰撞
        for (GameObject ai : new ArrayList<>(aiList)) {
            if (ai.isActive() && ai.getBounds().intersects(player.getBounds())) {
                ai.setActive(false);
                playerHealth--;
                collisionHits++;
                if (playerHealth <= 0) {
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    } else {
                        playerHealth = 5; // 重置血量
                    }
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                up = true;
                break;
            case KeyEvent.VK_DOWN:
                down = true;
                break;
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_RIGHT:
                right = true;
                break;
            case KeyEvent.VK_SPACE:
                shooting = true;
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                up = false;
                break;
            case KeyEvent.VK_DOWN:
                down = false;
                break;
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_RIGHT:
                right = false;
                break;
            case KeyEvent.VK_SPACE:
                shooting = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
