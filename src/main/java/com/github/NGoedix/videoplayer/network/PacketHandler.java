package com.github.NGoedix.videoplayer.network;

import com.github.NGoedix.videoplayer.network.Payload.*;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.videoplayer.block.entity.custom.RadioBlockEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.UUID;

public class PacketHandler {

    public static void registerPayloadTypes() {
        // 注册C2S payload类型
        PayloadTypeRegistry.playC2S().register(VideoUpdatePayload.TYPE, VideoUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RadioUpdatePayload.TYPE, RadioUpdatePayload.STREAM_CODEC);

        // 注册S2C payload类型
        PayloadTypeRegistry.playS2C().register(FrameVideoPayload.TYPE, FrameVideoPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenVideoManagerScreenPayload.TYPE, OpenVideoManagerScreenPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenRadioManagerScreenPayload.TYPE, OpenRadioManagerScreenPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SendVideoPayload.TYPE, SendVideoPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SendCustomVideoPayload.TYPE, SendCustomVideoPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SendMusicPayload.TYPE, SendMusicPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(RadioMessagePayload.TYPE, RadioMessagePayload.STREAM_CODEC);
    }

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(VideoUpdatePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var pos = payload.pos();
                var url = payload.url();
                var volume = payload.volume();
                var tick = payload.tick();
                var isPlaying = payload.isPlaying();
                var stopped = payload.stopped();
                var exit = payload.exit();

                if (player.level().getBlockEntity(pos) instanceof TVBlockEntity tvBlockEntity) {
                    if (exit)
                        tvBlockEntity.setBeingUsed(new UUID(0, 0));
                    else {
                        tvBlockEntity.setUrl(url);
                        tvBlockEntity.setVolume(volume);

                        if (tick != -1)
                            tvBlockEntity.setTick(tick);

                        tvBlockEntity.setPlaying(isPlaying);

                        if (stopped)
                            tvBlockEntity.stop();

                        tvBlockEntity.notifyPlayer();
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RadioUpdatePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var blockPos = payload.pos();
                var url = payload.url();
                var volume = payload.volume();
                var tick = payload.tick();
                var isPlaying = payload.isPlaying();
                var exit = payload.exit();

                if (player.level().getBlockEntity(blockPos) instanceof RadioBlockEntity radioBlockEntity) {
                    if (exit)
                        radioBlockEntity.setBeingUsed(new UUID(0, 0));
                    else {
                        radioBlockEntity.setUrl(url);

                        if (tick != -1)
                            radioBlockEntity.setTick(tick);

                        radioBlockEntity.setVolume(volume);
                        radioBlockEntity.setPlaying(isPlaying);

                        radioBlockEntity.notifyPlayer();
                    }
                }
            });
        });
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(FrameVideoPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                var url = payload.url();
                var pos = payload.pos();
                var playing = payload.playing();
                var tick = payload.tick();

                ClientHandler.manageVideo(client, url, pos, playing, tick);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenVideoManagerScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                var pos = payload.pos();
                var url = payload.url();
                var volume = payload.volume();
                var tick = payload.tick();
                var isPlaying = payload.isPlaying();

                ClientHandler.openVideoGUI(client, pos, url, volume, tick, isPlaying);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenRadioManagerScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                var pos = payload.pos();
                var url = payload.url();
                var volume = payload.volume();
                var isPlaying = payload.isPlaying();

                ClientHandler.openRadioGUI(client, pos, url, volume, isPlaying);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SendVideoPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                var type = payload.messageType();

                if (type == SendVideoPayload.VideoMessageType.START) {
                    var url = payload.url();
                    var volume = payload.volume();
                    var isControlBlocked = payload.controlBlocked();
                    var canSkip = payload.canSkip();

                    ClientHandler.openVideo(client, url, volume, isControlBlocked, canSkip);
                } else if (type == SendVideoPayload.VideoMessageType.STOP) {
                    ClientHandler.stopVideoIfExists(client);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SendCustomVideoPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                var type = payload.messageType();

                if (type == SendCustomVideoPayload.VideoMessageType.START) {
                    var url = payload.url();
                    var volume = payload.volume();
                    var isControlBlocked = payload.controlBlocked();
                    var canSkip = payload.canSkip();
                    var mode = payload.mode();
                    var position = payload.position();
                    var optionInMode = payload.optionInMode();
                    var optionInSecs = payload.optionInSecs();
                    var optionOutMode = payload.optionOutMode();
                    var optionOutSecs = payload.optionOutSecs();

                    if (mode == 0) ClientHandler.openVideo(client, url, volume, isControlBlocked, canSkip, optionInMode, optionInSecs, optionOutMode, optionOutSecs);

                } else if (type == SendCustomVideoPayload.VideoMessageType.STOP) {
                    ClientHandler.stopVideoIfExists(client);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SendMusicPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var type = payload.messageType();
                if (type == SendMusicPayload.MusicMessageType.START) {
                    var url = payload.url();
                    var volume = payload.volume();

                    ClientHandler.playMusic(url, volume);
                } else if (type == SendMusicPayload.MusicMessageType.STOP) {
                    ClientHandler.stopMusicIfPlaying();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RadioMessagePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                var url = payload.url();
                var pos = payload.pos();
                var playing = payload.playing();

                ClientHandler.manageRadio(client, url, pos, playing);
            });
        });
    }

    // SEND MESSAGES S2C (保持不变)
    public static void sendS2CSendVideoStart(ServerPlayer player, String url, int volume, boolean controlBlocked, boolean canSkip) {
        SendVideoPayload payload = new SendVideoPayload(SendVideoPayload.VideoMessageType.START, url, volume, controlBlocked, canSkip);
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendS2CSendVideoStop(ServerPlayer player) {
        SendVideoPayload payload = new SendVideoPayload(SendVideoPayload.VideoMessageType.STOP, "", 0, false, false);
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendS2CSendMusicStart(ServerPlayer player, String url, int volume) {
        SendMusicPayload payload = new SendMusicPayload(SendMusicPayload.MusicMessageType.START, url, volume);
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendS2CSendMusicStop(ServerPlayer player) {
        SendMusicPayload payload = new SendMusicPayload(SendMusicPayload.MusicMessageType.STOP, "", 0);
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendS2CSendVideoStart(ServerPlayer player, String url, int volume, boolean controlBlocked, boolean canSkip, int mode, int position, int optionInMode, int optionInSecs, int optionOutMode, int optionOutSecs) {
        SendCustomVideoPayload payload = new SendCustomVideoPayload(SendCustomVideoPayload.VideoMessageType.START, url, volume, controlBlocked, canSkip, mode, position, optionInMode, optionInSecs, optionOutMode, optionOutSecs);
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendS2COpenVideoManagerScreen(ServerPlayer player, BlockPos pos, String url, int volume, int tick, boolean isPlaying) {
        OpenVideoManagerScreenPayload payload = new OpenVideoManagerScreenPayload(pos, url, volume, tick, isPlaying);
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendS2COpenRadioManagerScreen(ServerPlayer player, BlockPos pos, String url, int volume, boolean isPlaying) {
        OpenRadioManagerScreenPayload payload = new OpenRadioManagerScreenPayload(pos, url, volume, isPlaying);
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendS2CFrameVideoMessage(LevelChunk chunk, String url, BlockPos pos, boolean playing, int tick) {
        FrameVideoPayload payload = new FrameVideoPayload(url, pos, playing, tick);

        for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) chunk.getLevel(), chunk.getPos()))
            ServerPlayNetworking.send(player, payload);
    }

    public static void sendS2CRadioMessage(LevelChunk chunk, String url, BlockPos pos, boolean playing) {
        RadioMessagePayload payload = new RadioMessagePayload(url, pos, playing);

        for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) chunk.getLevel(), chunk.getPos()))
            ServerPlayNetworking.send(player, payload);
    }

    public static void sendC2SVideoUpdateMessage(BlockPos pos, String url, int volume, int tick, boolean isPlaying, boolean stopped, boolean exit) {
        VideoUpdatePayload payload = new VideoUpdatePayload(pos, url, volume, tick, isPlaying, stopped, exit);
        ClientPlayNetworking.send(payload);
    }

    public static void sendC2SRadioUpdateMessage(BlockPos pos, String url, int volume, int tick, boolean isPlaying, boolean exit) {
        RadioUpdatePayload payload = new RadioUpdatePayload(pos, url, volume, tick, isPlaying, exit);
        ClientPlayNetworking.send(payload);
    }
}