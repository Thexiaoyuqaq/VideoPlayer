package com.github.NGoedix.videoplayer.util.displayers;

import com.github.NGoedix.videoplayer.util.math.VideoMathUtil;
import com.github.NGoedix.videoplayer.util.math.geo.Vec3d;
import net.minecraft.client.Minecraft;
import org.watermedia.api.math.MathAPI;
import org.watermedia.api.player.videolan.BasePlayer;
import org.watermedia.api.player.videolan.MusicPlayer;
import org.watermedia.api.player.videolan.VideoPlayer;

import java.awt.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoDisplayer implements IDisplay {

    private static final String VLC_FAILED = "https://i.imgur.com/XCcN2uX.png";

    private static final int ACCEPTABLE_SYNC_TIME = 1500;

    private static final List<VideoDisplayer> OPEN_DISPLAYS = new ArrayList<>();

    private boolean stream = false;

    public static void tick() {
        synchronized (OPEN_DISPLAYS) {
            OPEN_DISPLAYS.forEach(VideoDisplayer::pauseIfNecessary);
        }
    }

    private static void pauseIfNecessary(VideoDisplayer display) {
        try {
            Method isPlayingMethod = display.player.getClass().getMethod("isPlaying");
            Method isLiveMethod = display.player.getClass().getMethod("isLive");
            Method getDurationMethod = display.player.getClass().getMethod("getDuration");
            Method setPauseModeMethod = display.player.getClass().getMethod("setPauseMode", boolean.class);

            boolean isPlaying = (Boolean) isPlayingMethod.invoke(display.player);
            boolean isLive = (Boolean) isLiveMethod.invoke(display.player);
            long duration = (Long) getDurationMethod.invoke(display.player);

            if (Minecraft.getInstance().isPaused() && isPlaying && (isLive || duration > 0)) {
                setPauseModeMethod.invoke(display.player, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unload() {
        synchronized (OPEN_DISPLAYS) {
            OPEN_DISPLAYS.forEach(VideoDisplayer::free);
            OPEN_DISPLAYS.clear();
        }
    }

    public static IDisplay createVideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean playing, boolean isOnlyMusic) {
        VideoDisplayer display = new VideoDisplayer(pos, url, volume, minDistance, maxDistance, loop, isOnlyMusic);
        if (display.player.raw() == null) throw new IllegalStateException("VideoDisplayer uses a broken player");
        OPEN_DISPLAYS.add(display);
        return display;
    }

    public BasePlayer player;

    private final Vec3d pos;
    private String url;
    private float lastSetVolume;
    private long lastCorrectedTime = Long.MIN_VALUE;

    public VideoDisplayer(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean isOnlyMusic) {
        this.pos = pos;
        this.url = url;

        if (!url.isEmpty()) {
            if (isOnlyMusic) {
                player = new MusicPlayer();
            } else {
                player = new VideoPlayer(Minecraft.getInstance());
            }
            adjustVolume(volume, minDistance, maxDistance);
            setRepeatMode(loop);
            URI uri = URI.create(url);
            startPlayer(uri);
        }
    }

    private void setRepeatMode(boolean loop) {
        try {
            Method setRepeatModeMethod = player.getClass().getMethod("setRepeatMode", boolean.class);
            setRepeatModeMethod.invoke(player, loop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPlayer(URI uri) {
        try {
            Method startMethod = player.getClass().getMethod("start", URI.class);
            startMethod.invoke(player, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adjustVolume(float volume, float minDistance, float maxDistance) {
        volume = pos != null ? calculateVolume(volume, minDistance, maxDistance) : volume;
        setPlayerVolume((int) volume);
        lastSetVolume = volume;
    }

    private void setPlayerVolume(int volume) {
        try {
            Method setVolumeMethod = player.getClass().getMethod("setVolume", int.class);
            setVolumeMethod.invoke(player, volume);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int calculateVolume(float volume, float minDistance, float maxDistance) {
        if (player == null) return 0;
        Minecraft mc = Minecraft.getInstance();
        float distance = (float) pos.distance(Objects.requireNonNull(Minecraft.getInstance().player).getPosition(mc.isPaused() ? 1.0F : mc.getFrameTime()));
        volume = VideoMathUtil.calculateVolume(volume, distance, minDistance, maxDistance);
        return (int) volume;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null || url == null)
            return;

        this.url = url;
        volume = pos != null ? calculateVolume(volume, minDistance, maxDistance) : volume;
        if (volume != lastSetVolume) {
            setPlayerVolume((int) volume);
            lastSetVolume = volume;
        }

        try {
            Method isSafeUseMethod = player.getClass().getMethod("isSafeUse");
            Method isValidMethod = player.getClass().getMethod("isValid");
            Method isLiveMethod = player.getClass().getMethod("isLive");
            Method setPauseModeMethod = player.getClass().getMethod("setPauseMode", boolean.class);
            Method isSeekAbleMethod = player.getClass().getMethod("isSeekAble");
            Method getTimeMethod = player.getClass().getMethod("getTime");
            Method getMediaInfoDurationMethod = player.getClass().getMethod("getMediaInfoDuration");
            Method seekToMethod = player.getClass().getMethod("seekTo", long.class);

            boolean isSafeUse = (Boolean) isSafeUseMethod.invoke(player);
            boolean isValid = (Boolean) isValidMethod.invoke(player);

            if (isSafeUse && isValid) {
                boolean isLive = (Boolean) isLiveMethod.invoke(player);
                if (!stream && isLive) stream = true;

                boolean currentPlaying = playing && !Minecraft.getInstance().isPaused();
                setPauseModeMethod.invoke(player, !currentPlaying);

                if (!stream && (Boolean) isSeekAbleMethod.invoke(player)) {
                    long time = MathAPI.msToTick(tick);
                    long playerTime = (Long) getTimeMethod.invoke(player);

                    if (time > playerTime) {
                        long mediaDuration = (Long) getMediaInfoDurationMethod.invoke(player);
                        time = floorMod(time, mediaDuration);
                    }

                    if (Math.abs(time - playerTime) > ACCEPTABLE_SYNC_TIME && Math.abs(time - lastCorrectedTime) > ACCEPTABLE_SYNC_TIME) {
                        lastCorrectedTime = time;
                        seekToMethod.invoke(player, time);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long floorMod(long x, long y) {
        try {
            final long r = x % y;
            if ((x ^ y) < 0 && r != 0)
                return r + y;
            return r;
        } catch (ArithmeticException e) {
            return 0;
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            Method isPlayingMethod = player.getClass().getMethod("isPlaying");
            Method isPausedMethod = player.getClass().getMethod("isPaused");

            boolean isPlaying = (Boolean) isPlayingMethod.invoke(player);
            boolean isPaused = (Boolean) isPausedMethod.invoke(player);

            return isPlaying || isPaused;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isStopped() {
        try {
            Method isStoppedMethod = player.getClass().getMethod("isStopped");
            Method isEndedMethod = player.getClass().getMethod("isEnded");

            boolean isStopped = (Boolean) isStoppedMethod.invoke(player);
            boolean isEnded = (Boolean) isEndedMethod.invoke(player);

            return isStopped || isEnded;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public int maxTick() {
        return IDisplay.super.maxTick();
    }

    @Override
    public int prepare(String url, boolean playing, boolean loop, int tick) {
        if (player == null) return -1;
        this.url = url;

        // Try to get GL texture using reflection
        try {
            Method getGlTextureMethod = player.getClass().getMethod("getGlTexture");
            Object result = getGlTextureMethod.invoke(player);
            return result instanceof Integer ? (Integer) result : 0;
        } catch (Exception e) {
            // Method doesn't exist or failed, return 0
            return 0;
        }
    }

    @Override
    public int getRenderTexture() {
        if (player == null) return 0;

        // Try to get GL texture using reflection
        try {
            Method getGlTextureMethod = player.getClass().getMethod("getGlTexture");
            Object result = getGlTextureMethod.invoke(player);
            return result instanceof Integer ? (Integer) result : 0;
        } catch (Exception e) {
            // Method doesn't exist or failed, return 0
            return 0;
        }
    }

    public void free() {
        if (player != null) {
            try {
                Method releaseMethod = player.getClass().getMethod("release");
                releaseMethod.invoke(player);
            } catch (Exception e) {
                e.printStackTrace();
            }
            player = null;
        }
    }

    @Override
    public void release() {
        free();
        synchronized (OPEN_DISPLAYS) {
            OPEN_DISPLAYS.remove(this);
        }
    }

    @Override
    public void stop() {
        if (player == null) return;
        try {
            Method stopMethod = player.getClass().getMethod("stop");
            stopMethod.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause(int tick) {
        if (player == null) return;
        try {
            if (tick != -1) {
                Method seekToMethod = player.getClass().getMethod("seekTo", int.class);
                seekToMethod.invoke(player, tick);
            }
            Method pauseMethod = player.getClass().getMethod("pause");
            pauseMethod.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resume(int tick) {
        if (player == null) return;
        try {
            if (tick != -1) {
                Method seekToMethod = player.getClass().getMethod("seekTo", int.class);
                seekToMethod.invoke(player, tick);
            }
            Method playMethod = player.getClass().getMethod("play");
            playMethod.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dimension getDimensions() {
        if (player == null) return null;

        // Try to get dimensions using reflection
        try {
            Method getDimensionsMethod = player.getClass().getMethod("getDimensions");
            Object result = getDimensionsMethod.invoke(player);
            return result instanceof Dimension ? (Dimension) result : null;
        } catch (Exception e) {
            // Method doesn't exist or failed, return null
            return null;
        }
    }
}