package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SendCustomVideoMessage {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "send_custom_video");

    public static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        VideoMessageType type = buffer.readEnum(VideoMessageType.class);

        if (type == VideoMessageType.START) {
            String url = buffer.readUtf();
            int volume = buffer.readInt();
            boolean isControlBlocked = buffer.readBoolean();
            boolean canSkip = buffer.readBoolean();
            int mode = buffer.readInt();
            int position = buffer.readInt();
            int optionInMode = buffer.readInt();
            int optionInSecs = buffer.readInt();
            int optionOutMode = buffer.readInt();
            int optionOutSecs = buffer.readInt();

            if (mode == 0) ClientHandler.openVideo(client, url, volume, isControlBlocked, canSkip, optionInMode, optionInSecs, optionOutMode, optionOutSecs);

        } else if (type == VideoMessageType.STOP) {
            ClientHandler.stopVideoIfExists(client);
        }
    }

    public enum VideoMessageType {
        START,
        STOP
    }
}
