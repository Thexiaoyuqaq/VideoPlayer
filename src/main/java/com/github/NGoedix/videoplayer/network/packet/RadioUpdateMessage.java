package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.block.entity.custom.RadioBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.UUID;

public class RadioUpdateMessage {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "update_radio");

    public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        BlockPos blockPos = buf.readBlockPos();
        String url = buf.readUtf();
        int volume = buf.readInt();
        int tick = buf.readInt();
        boolean isPlaying = buf.readBoolean();
        boolean exit = buf.readBoolean();

        server.execute(() -> {
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
    }
}
