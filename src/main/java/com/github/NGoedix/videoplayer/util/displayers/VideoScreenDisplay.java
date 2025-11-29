package com.github.NGoedix.videoplayer.util.displayers;

import net.minecraft.client.Minecraft;
import org.watermedia.api.player.videolan.VideoPlayer;
import java.lang.reflect.Method;

public class VideoScreenDisplay {

    private final Object player; // Changed from SyncVideoPlayer to Object for compatibility
    private final String url;
    private final int volume;
    private final int position;
    private final int optionInMode;
    private final int optionInSecs;
    private final int optionOutMode;
    private final int optionOutSecs;

    public VideoScreenDisplay(String url, int volume, int position, int optionInMode, int optionInSecs, int optionOutMode, int optionOutSecs) {
        // Create VideoPlayer instead of SyncVideoPlayer
        player = new VideoPlayer(Minecraft.getInstance());
        this.url = url;
        this.volume = volume;
        this.position = position;
        this.optionInMode = optionInMode;
        this.optionInSecs = optionInSecs;
        this.optionOutMode = optionOutMode;
        this.optionOutSecs = optionOutSecs;
    }

    public String getUrl() {
        return url;
    }

    public int getPosition() {
        return position;
    }

    /**
     * Gets the player instance.
     * Note: Return type changed from SyncVideoPlayer to Object for watermedia 2.1.16 compatibility.
     * Use reflection or cast to appropriate type when accessing player methods.
     */
    public Object getPlayer() {
        return player;
    }

    public int getVolume() {
        return volume;
    }

    public int getOptionInMode() {
        return optionInMode;
    }

    public int getOptionInSecs() {
        return optionInSecs;
    }

    public int getOptionOutMode() {
        return optionOutMode;
    }

    public int getOptionOutSecs() {
        return optionOutSecs;
    }

    // Helper methods for common player operations using reflection

    /**
     * Safely checks if the player is ready
     */
    public boolean isPlayerReady() {
        try {
            Method isReadyMethod = player.getClass().getMethod("isReady");
            return (Boolean) isReadyMethod.invoke(player);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safely checks if the player is playing
     */
    public boolean isPlayerPlaying() {
        try {
            Method isPlayingMethod = player.getClass().getMethod("isPlaying");
            return (Boolean) isPlayingMethod.invoke(player);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safely gets the player duration
     */
    public long getPlayerDuration() {
        try {
            Method getDurationMethod = player.getClass().getMethod("getDuration");
            return (Long) getDurationMethod.invoke(player);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Safely gets the current playback time
     */
    public long getPlayerTime() {
        try {
            Method getTimeMethod = player.getClass().getMethod("getTime");
            return (Long) getTimeMethod.invoke(player);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Safely seeks to a specific time
     */
    public void seekTo(long time) {
        try {
            Method seekToMethod = player.getClass().getMethod("seekTo", long.class);
            seekToMethod.invoke(player, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Safely starts/resumes playback
     */
    public void play() {
        try {
            Method playMethod = player.getClass().getMethod("play");
            playMethod.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Safely pauses playback
     */
    public void pause() {
        try {
            Method pauseMethod = player.getClass().getMethod("pause");
            pauseMethod.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Safely stops playback
     */
    public void stop() {
        try {
            Method stopMethod = player.getClass().getMethod("stop");
            stopMethod.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Safely releases the player resources
     */
    public void release() {
        try {
            Method releaseMethod = player.getClass().getMethod("release");
            releaseMethod.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}