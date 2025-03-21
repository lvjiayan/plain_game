package com.game;

import java.awt.Rectangle;
import java.awt.Color;
import javax.swing.ImageIcon;
import java.net.URL;
import java.awt.Image;
import java.awt.MediaTracker;

public class GameObject {
    protected int x, y;
    protected int width, height;
    protected Color color;
    protected int speed;
    protected boolean isActive = true;
    private int velocityX = 0;
    private int velocityY = 0;
    private boolean isSpecialAI = false;  // 新增：标记是否为特殊AI
    private boolean isBomb = false;
    private int explosionRadius = 0;
    private long createTime = System.currentTimeMillis();
    private float alpha = 1.0f; // 透明度，1.0表示完全不透明
    private int maxHealth = 1;  // 默认血量为1
    private int currentHealth;   // 当前血量
    private boolean isSuperAI = false;  // 新增：标记是否为超级AI
    private float originalX;     // 记录初始X位置，用于摆动
    private float oscPhase = 0;  // 摆动相位
    private boolean isBulletPack = false;
    private boolean isBombPack = false;
    private ImageIcon imageIcon = null;  // 新增：用于存储GIF图片
    private boolean isExplosion = false; // 新增：标记是否为爆炸效果

    public GameObject(int x, int y, int width, int height, Color color, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.speed = speed;
        this.currentHealth = 1;  // 默认血量初始化为1
        this.originalX = x;      // 记录初始X位置
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public Color getColor() { 
        Color baseColor = this.color;
        if (alpha < 1.0f) {
            return new Color(baseColor.getRed()/255f, 
                           baseColor.getGreen()/255f, 
                           baseColor.getBlue()/255f, 
                           alpha);
        }
        return baseColor;
    }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public void setVelocity(int vx, int vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }

    public int getVelocityX() { return velocityX; }
    public int getVelocityY() { return velocityY; }
    
    // 新增：设置和获取特殊AI标记的方法
    public void setSpecialAI(boolean special) {
        this.isSpecialAI = special;
    }

    public boolean isSpecialAI() {
        return this.isSpecialAI;
    }

    // 新增：炸弹相关方法
    public void setBomb(boolean bomb, int radius) {
        this.isBomb = bomb;
        this.explosionRadius = radius;
    }

    public boolean isBomb() { return isBomb; }
    public int getExplosionRadius() { return explosionRadius; }
    public long getCreateTime() { return createTime; }
    public void setAlpha(float alpha) {
        this.alpha = Math.max(0, Math.min(1, alpha));
    }

    public float getAlpha() {
        return alpha;
    }

    public void setHealth(int health) {
        this.maxHealth = health;
        this.currentHealth = health;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void damage(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        if (currentHealth <= 0) {
            isActive = false;
        }
    }

    public void setSuperAI(boolean superAI) {
        this.isSuperAI = superAI;
    }

    public boolean isSuperAI() {
        return this.isSuperAI;
    }

    public void updateOscillation() {
        if (isSuperAI && isActive) {
            oscPhase += 0.02;  // 控制摆动速度
            x = (int)(originalX + Math.sin(oscPhase) * 50);  // 50是摆动幅度
        }
    }

    public void setBulletPack(boolean isBulletPack) {
        this.isBulletPack = isBulletPack;
    }

    public void setBombPack(boolean isBombPack) {
        this.isBombPack = isBombPack;
    }

    public boolean isBulletPack() {
        return isBulletPack;
    }

    public boolean isBombPack() {
        return isBombPack;
    }

    // 新增：设置和获取图片的方法
    public void setImage(String path) {
        try {
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null) {
                // 每次都创建新的Image实例
                Image image = new ImageIcon(url).getImage();
                // 调整图片大小
                Image scaledImage = image.getScaledInstance(width + 30, height, Image.SCALE_DEFAULT);
                imageIcon = new ImageIcon(scaledImage);
                
                // 等待图片完全加载
                MediaTracker tracker = new MediaTracker(new java.awt.Container());
                tracker.addImage(imageIcon.getImage(), 0);
                tracker.waitForAll();
            } else {
                System.err.println("Could not find image resource: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    public ImageIcon getImage() {
        return imageIcon;
    }

    public void setExplosion(boolean explosion) {
        this.isExplosion = explosion;
    }

    public boolean isExplosion() {
        return isExplosion;
    }
}
