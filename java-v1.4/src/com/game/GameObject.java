package com.game;

import java.awt.Rectangle;
import java.awt.Color;

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
}
