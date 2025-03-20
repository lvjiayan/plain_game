package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int DELAY = 16; // 约60FPS
    private static final int AI_MOVE_DELAY = 5;  // 从30减少到20帧，提高更新频率
    private static final int AI_SHOOT_CHANCE = 3;  // 保持3%的射击概率

    private GameObject player;
    private GameObject ai;
    private List<GameObject> playerBullets;
    private List<GameObject> aiBullets;
    private Timer timer;
    private boolean leftPressed, rightPressed, spacePressed;
    private long lastShotTime;
    private int failureCount = 0;
    private int aiHitCount = 0;
    private Random random = new Random();
    private int aiMoveCounter = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        initGame();
    }

    private void initGame() {
        player = new GameObject(WIDTH/2-25, HEIGHT-60, 50, 50, Color.BLUE, 5);
        ai = new GameObject(WIDTH/2-25, 10, 50, 50, Color.RED, 4);
        playerBullets = new ArrayList<>();
        aiBullets = new ArrayList<>();
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGame(g);
    }

    private void drawGame(Graphics g) {
        // 绘制游戏对象
        drawGameObject(g, player);
        drawGameObject(g, ai);
        
        // 绘制子弹
        for (GameObject bullet : playerBullets) {
            drawGameObject(g, bullet);
        }
        for (GameObject bullet : aiBullets) {
            drawGameObject(g, bullet);
        }

        // 绘制分数
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Failures: " + failureCount, 10, 25);
        g.drawString("AI Hits: " + aiHitCount, WIDTH-150, 25);
    }

    private void drawGameObject(Graphics g, GameObject obj) {
        g.setColor(obj.getColor());
        g.fillRect(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        handlePlayerMovement();
        updateAI();
        updateBullets();
        checkCollisions();
    }

    private void handlePlayerMovement() {
        if (leftPressed && player.getX() > 0) {
            player.move(-player.speed, 0);
        }
        if (rightPressed && player.getX() < WIDTH - player.getWidth()) {
            player.move(player.speed, 0);
        }
        if (spacePressed && System.currentTimeMillis() - lastShotTime > 300) {
            firePlayerBullet();
            lastShotTime = System.currentTimeMillis();
        }
    }

    private void updateAI() {
        aiMoveCounter++;
        if (aiMoveCounter >= AI_MOVE_DELAY) {
            int distanceToPlayer = player.getX() - ai.getX();
            // 将除数从40减小到30，增加移动速度
            int aiMoveStep = Math.min(Math.abs(distanceToPlayer) / 10, 8);  // 最大速度从2增加到3
            aiMoveStep = Math.max(1, aiMoveStep);  // 保持最小速度为1

            // 减小移动触发距离，让AI更快响应
            if (Math.abs(distanceToPlayer) > 3) {
                ai.move(Integer.compare(distanceToPlayer, 0) * aiMoveStep, 0);
            }
            aiMoveCounter = 0;
        }

        if (random.nextInt(100) < AI_SHOOT_CHANCE) {
            fireAIBullet();
        }
    }

    private void updateBullets() {
        playerBullets.removeIf(bullet -> {
            bullet.move(0, -10);
            return bullet.getY() < 0;
        });

        aiBullets.removeIf(bullet -> {
            bullet.move(0, 10);
            return bullet.getY() > HEIGHT;
        });
    }

    private void firePlayerBullet() {
        playerBullets.add(new GameObject(
            player.getX() + player.getWidth()/2 - 5,
            player.getY(),
            10, 5,
            Color.BLUE,
            10
        ));
    }

    private void fireAIBullet() {
        aiBullets.add(new GameObject(
            ai.getX() + ai.getWidth()/2 - 5,
            ai.getY() + ai.getHeight(),
            10, 5,
            Color.RED,
            10
        ));
    }

    private void checkCollisions() {
        playerBullets.removeIf(bullet -> {
            if (bullet.getBounds().intersects(ai.getBounds())) {
                aiHitCount++;
                return true;
            }
            return false;
        });

        aiBullets.removeIf(bullet -> {
            if (bullet.getBounds().intersects(player.getBounds())) {
                failureCount++;
                return true;
            }
            return false;
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: leftPressed = true; break;
            case KeyEvent.VK_RIGHT: rightPressed = true; break;
            case KeyEvent.VK_SPACE: spacePressed = true; break;
            case KeyEvent.VK_ESCAPE: System.exit(0); break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: leftPressed = false; break;
            case KeyEvent.VK_RIGHT: rightPressed = false; break;
            case KeyEvent.VK_SPACE: spacePressed = false; break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
