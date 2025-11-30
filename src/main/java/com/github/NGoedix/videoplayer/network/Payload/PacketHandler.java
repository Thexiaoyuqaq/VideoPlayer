package com.github.NGoedix.videoplayer.network.Payload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

public class PacketHandler {

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(VideoUpdatePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var level = player.serverLevel();
                var blockEntity = level.getBlockEntity(payload.pos());
                if (blockEntity instanceof com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity tvBlockEntity) {
                    if (!payload.exit()) {
                        tvBlockEntity.setUrl(payload.url());
                        tvBlockEntity.setVolume(payload.volume());
                        tvBlockEntity.setTick(payload.tick());
                        tvBlockEntity.setPlaying(payload.isPlaying());
                        if (payload.stopped()) {
                            tvBlockEntity.stop();
                        }
                    }
                    tvBlockEntity.setBeingUsed(payload.exit() ? null : player.getUUID());
                    tvBlockEntity.notifyPlayer();
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RadioUpdatePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var level = player.serverLevel();
                var blockEntity = level.getBlockEntity(payload.pos());
                if (blockEntity instanceof com.github.NGoedix.videoplayer.block.entity.custom.RadioBlockEntity radioBlockEntity) {
                    if (!payload.exit()) {
                        radioBlockEntity.setUrl(payload.url());
                        radioBlockEntity.setVolume(payload.volume());
                        radioBlockEntity.setTick(payload.tick());
                        radioBlockEntity.setPlaying(payload.isPlaying());
                    }
                    radioBlockEntity.setBeingUsed(payload.exit() ? null : player.getUUID());
                    radioBlockEntity.notifyPlayer();
                }
            });
        });
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(FrameVideoPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                if (client.level != null) {
                    var blockEntity = client.level.getBlockEntity(payload.pos());
                    if (blockEntity instanceof com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity tvBlockEntity) {
                        tvBlockEntity.setUrl(payload.url());
                        tvBlockEntity.setPlaying(payload.playing());
                        tvBlockEntity.setTick(payload.tick());
                    }
                }
            });
        });

        // 注册其他S2C消息...
    }

    // SEND MESSAGES S2C
    public static void sendS2CFrameVideoMessage(LevelChunk chunk, String url, BlockPos pos, boolean playing, int tick) {
        FrameVideoPayload payload = new FrameVideoPayload(url, pos, playing, tick);

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