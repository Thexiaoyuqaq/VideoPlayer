package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class VideoUpdateMessage {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "update_video");

    public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        BlockPos pos = buf.readBlockPos();
        String url = buf.readUtf();
        int volume = buf.readInt();
        int tick = buf.readInt();
        boolean isPlaying = buf.readBoolean();
        boolean stopped = buf.readBoolean();
        boolean exit = buf.readBoolean();

        server.execute(() -> {
            if (player.level().getBlockEntity(pos) instanceof TVBlockEntity) {
                TVBlockEntity tvBlockEntity = (TVBlockEntity) player.level().getBlockEntity(pos);
                if (tvBlockEntity == null) return;

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
                }}
        });
    }
}
