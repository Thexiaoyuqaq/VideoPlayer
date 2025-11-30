package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SendVideoMessage {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "send_video");

    public static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        VideoMessageType type = buffer.readEnum(VideoMessageType.class);
        if (type == VideoMessageType.START) {
            String url = buffer.readUtf();
            int volume = buffer.readInt();
            boolean isControlBlocked = buffer.readBoolean();
            boolean canSkip = buffer.readBoolean();

            ClientHandler.openVideo(client, url, volume, isControlBlocked, canSkip);
        } else if (type == VideoMessageType.STOP) {
            ClientHandler.stopVideoIfExists(client);
        }
    }

    public enum VideoMessageType {
        START,
        STOP
    }
}
