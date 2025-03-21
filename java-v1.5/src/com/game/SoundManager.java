package com.game;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private Map<String, Clip> clips;
    private boolean soundEnabled = true;
    private float volume = 1.0f;

    public SoundManager() {
        clips = new HashMap<>();
        initializeSounds();
    }

    private void initializeSounds() {
        loadSound("shoot", "resources/sounds/laser2.wav");
        loadSound("bomb", "resources/sounds/bomb2.wav");
        loadSound("explosion", "resources/sounds/explosion2.wav");
    }

    private void loadSound(String name, String path) {
        try {
            URL url = getClass().getClassLoader().getResource(path);
            if (url == null) {
                System.err.println("Could not find sound file: " + path);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            
            // 设置音量控制
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume * gainControl.getMaximum());
            }
            
            clips.put(name, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound " + name + ": " + e.getMessage());
        }
    }

    public void playSound(String name) {
        if (!soundEnabled) return;
        
        Clip clip = clips.get(name);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void playExplosion() {
        // 播放爆炸音效，带有延迟的重叠效果
        playSound("bomb");
        new Thread(() -> {
            try {
                Thread.sleep(100);
                playSound("explosion");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        for (Clip clip : clips.values()) {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
            }
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void cleanup() {
        for (Clip clip : clips.values()) {
            clip.close();
        }
        clips.clear();
    }
}
