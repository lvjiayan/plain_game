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
    private static final int MAX_AI_COUNT = 5; // 最大AI数量

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        random = new Random();
        initGame();
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
        aiList.add(new GameObject(x, 50, 40, 40, Color.RED, 2));
    }

    private void updateAISpawning() {
        long currentTime = System.currentTimeMillis();
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
        
        // Draw scores
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Hits taken: " + playerHits, 10, 30);
        g.drawString("Collisions: " + collisionHits, WIDTH/3, 30);  // 新增：显示碰撞次数
        g.drawString("AI defeated: " + aiDefeated, WIDTH - 150, 30);
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
        updatePlayer();
        updateAIs();
        updateAISpawning();
        updateBullets();
        checkCollisions();
    }

    private void updatePlayer() {
        if (up && player.getY() > 0) player.move(0, -player.speed);
        if (down && player.getY() < HEIGHT - player.getHeight()) player.move(0, player.speed);
        if (left && player.getX() > 0) player.move(-player.speed, 0);
        if (right && player.getX() < WIDTH - player.getWidth()) player.move(player.speed, 0);
        
        if (shooting && System.currentTimeMillis() - lastShootTime > 250) {
            playerShoot();
            lastShootTime = System.currentTimeMillis();
        }
    }

    private void updateAIs() {
        for (GameObject ai : aiList) {
            if (ai.isActive()) {
                // Move AI down slowly
                ai.move(0, 1);
                
                // AI shooting logic - 只在高于屏幕1/3高度时发射子弹
                if (ai.getY() < HEIGHT * 2 / 3 && random.nextInt(100) < 2) {
                    aiShoot(ai);
                }
            }
        }
        
        // Remove AIs that have gone off screen
        aiList.removeIf(ai -> ai.getY() > HEIGHT);
    }

    private void playerShoot() {
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
            Color.ORANGE,
            bulletSpeed
        );
        bullet.setVelocity(velocityX, velocityY);  // 这里需要在GameObject类中添加velocity相关代码
        aiBullets.add(bullet);
    }

    private void updateBullets() {
        // Update player bullets
        playerBullets.removeIf(bullet -> {
            bullet.move(0, -bullet.speed);
            return bullet.getY() < -bullet.getHeight();
        });

        // Update AI bullets
        aiBullets.removeIf(bullet -> {
            if (bullet instanceof GameObject) {
                GameObject b = bullet;
                b.move(b.getVelocityX(), b.getVelocityY());
                return b.getY() > HEIGHT || b.getY() < 0 || 
                       b.getX() > WIDTH || b.getX() < 0;
            }
            return true;
        });
    }

    private void checkCollisions() {
        // Check player bullets hitting AIs
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

        // Remove inactive AIs
        aiList.removeIf(ai -> !ai.isActive());

        // Check AI bullets hitting player
        for (GameObject bullet : new ArrayList<>(aiBullets)) {
            if (bullet.getBounds().intersects(player.getBounds())) {
                playerHits++;
                aiBullets.remove(bullet);
            }
        }

        // 新增：检测AI与玩家的直接碰撞
        for (GameObject ai : new ArrayList<>(aiList)) {
            if (ai.isActive() && ai.getBounds().intersects(player.getBounds())) {
                ai.setActive(false);
                collisionHits++;
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
