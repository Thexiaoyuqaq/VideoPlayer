package com.github.NGoedix.videoplayer.client;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.VideoPlayerUtils;
import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.block.entity.custom.RadioBlockEntity;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.videoplayer.client.gui.RadioScreen;
import com.github.NGoedix.videoplayer.client.gui.TVVideoScreen;
import com.github.NGoedix.videoplayer.client.gui.VideoScreen;
import com.github.NGoedix.videoplayer.client.render.TVBlockRenderer;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.github.NGoedix.videoplayer.util.RadioStreams;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageRenderer;
import org.watermedia.api.player.videolan.MusicPlayer;
import org.watermedia.core.tools.JarTool;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientHandler implements ClientModInitializer {

    // 图片资源
    @Environment(EnvType.CLIENT)
    private static ImageRenderer IMG_PAUSED;
    @Environment(EnvType.CLIENT)
    private static ImageRenderer IMG_STEP10;
    @Environment(EnvType.CLIENT)
    private static ImageRenderer IMG_STEP5;

    @Environment(EnvType.CLIENT)
    public static ImageRenderer pausedImage() { return IMG_PAUSED; }
    @Environment(EnvType.CLIENT)
    public static ImageRenderer step10Image() { return IMG_STEP10; }
    @Environment(EnvType.CLIENT)
    public static ImageRenderer step5Image() { return IMG_STEP5; }

    // 音乐播放器
    private static final List<MusicPlayer> musicPlayers = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        Reference.LOGGER.info("Initializing Client");

        // 检查不兼容的模组
        if (VideoPlayerUtils.isInstalled("mr_stellarity", "stellarity")) {
            throw new VideoPlayerUtils.UnsupportedModException(
                    "mr_stellarity (Stellarity)",
                    "breaks picture rendering, overwrites Minecraft core shaders and isn't possible work around that"
            );
        }

        // 初始化
        RadioStreams.prepareRadios();
        PacketHandler.registerS2CPackets();
        BlockEntityRendererRegistry.register(ModBlockEntities.TV_BLOCK_ENTITY, TVBlockRenderer::new);

        // 加载图片资源
        IMG_PAUSED = ImageAPI.renderer(JarTool.readImage("/pictures/paused.png"), true);
        IMG_STEP10 = ImageAPI.renderer(JarTool.readImage("/pictures/step10.png"), true);
        IMG_STEP5 = ImageAPI.renderer(JarTool.readImage("/pictures/step5.png"), true);
    }

    /**
     * 打开视频
     */
    public static void openVideo(Minecraft client, String url, int volume, boolean isControlBlocked, boolean canSkip) {
        client.execute(() -> {
           client.setScreen(new VideoScreen(url, volume, isControlBlocked, canSkip, false));
        });
    }

    /**
     * 打开视频（带淡入淡出选项）
     */
    public static void openVideo(Minecraft client, String url, int volume, boolean isControlBlocked,
                                 boolean canSkip, int optionInMode, int optionInSecs,
                                 int optionOutMode, int optionOutSecs) {
        client.execute(() -> {
           client.setScreen(new VideoScreen(url, volume, isControlBlocked, canSkip,
                 optionInMode, optionInSecs, optionOutMode, optionOutSecs));
        });
    }

    /**
     * 停止视频（如果存在）
     */
    public static void stopVideoIfExists(Minecraft client) {
        client.execute(() -> {
            if (client.screen instanceof VideoScreen screen) {
                screen.onClose();
            }
        });
    }

    /**
     * 播放音乐
     */
    public static void playMusic(String url, int volume) {
        // 停止所有正在播放的音乐
        stopAllMusic();

        // 创建新的播放器
        MusicPlayer musicPlayer = new MusicPlayer();
        musicPlayers.add(musicPlayer);
        musicPlayer.setVolume(volume);

        try {
            URI uri = URI.create(url);
            musicPlayer.start(uri);
        } catch (Exception e) {
            Reference.LOGGER.error("Failed to play music: {}", url, e);
            musicPlayer.release();
            musicPlayers.remove(musicPlayer);
        }
    }

    /**
     * 停止所有音乐
     */
    public static void stopMusicIfPlaying() {
        stopAllMusic();
    }

    /**
     * 停止所有音乐（内部方法）
     */
    private static void stopAllMusic() {
        for (MusicPlayer musicPlayer : musicPlayers) {
            try {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.stop();
                }
                musicPlayer.release();
            } catch (Exception e) {
                Reference.LOGGER.error("Error stopping music player", e);
            }
        }
        musicPlayers.clear();
    }

    /**
     * 打开收音机GUI
     */
    public static void openRadioGUI(Minecraft client, BlockPos pos, String url, int volume, boolean isPlaying) {
        client.execute(() -> {
            BlockEntity be = client.level.getBlockEntity(pos);
            if (be instanceof RadioBlockEntity radio) {
                radio.setUrl(url);
                radio.setVolume(volume);
                radio.setPlaying(isPlaying);
                client.setScreen(new RadioScreen(be));
            }
        });
    }

    /**
     * 打开电视GUI
     */
    public static void openVideoGUI(Minecraft client, BlockPos pos, String url, int volume, int tick, boolean isPlaying) {
        client.execute(() -> {
            BlockEntity be = client.level.getBlockEntity(pos);
            if (be instanceof TVBlockEntity tv) {
                tv.setUrl(url);
                tv.setTick(tick);
                tv.setVolume(volume);
                tv.setPlaying(isPlaying);
                client.setScreen(new TVVideoScreen(be));
            }
        });
    }

    /**
     * 管理视频播放
     */
    public static void manageVideo(Minecraft client, String url, BlockPos pos, boolean playing, int tick) {
        client.execute(() -> {
            BlockEntity be = client.level.getBlockEntity(pos);
            if (be instanceof TVBlockEntity tv) {
                tv.setUrl(url);
                tv.setPlaying(playing);

                // 同步时间戳（允许40tick的误差）
                if (Math.abs(tv.getTick() - tick) > 40) {
                    tv.setTick(tick);
                }

                // 控制播放状态
                if (tv.requestDisplay() != null) {
                    if (playing) {
                        tv.requestDisplay().resume(tv.getTick());
                    } else {
                        tv.requestDisplay().pause(tv.getTick());
                    }
                }
            }
        });
    }

    /**
     * 管理收音机播放
     */
    public static void manageRadio(Minecraft client, String url, BlockPos pos, boolean playing) {
        client.execute(() -> {
            BlockEntity be = client.level.getBlockEntity(pos);
            if (be instanceof RadioBlockEntity radio) {
                radio.setUrl(url);
                radio.setPlaying(playing);
                radio.notifyPlayer();
            }
        });
    }
}