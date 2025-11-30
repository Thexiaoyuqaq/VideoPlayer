package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SendMusicMessage {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "send_video");

    public static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        MusicMessageType type = buffer.readEnum(MusicMessageType.class);
        if (type == MusicMessageType.START) {
            String url = buffer.readUtf();
            int volume = buffer.readInt();

            ClientHandler.playMusic(url, volume);
        } else if (type == MusicMessageType.STOP) {
            ClientHandler.stopMusicIfPlaying();
        }
    }

    public enum MusicMessageType {
        START,
        STOP
    }
}
