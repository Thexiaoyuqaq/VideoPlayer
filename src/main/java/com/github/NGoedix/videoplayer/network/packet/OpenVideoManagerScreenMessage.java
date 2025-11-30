package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public class OpenVideoManagerScreenMessage {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "open_video_manager");

    public static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        BlockPos pos = buf.readBlockPos();
        String url = buf.readUtf();
        int volume = buf.readInt();
        int tick = buf.readInt();
        boolean isPlaying = buf.readBoolean();

        ClientHandler.openVideoGUI(client, pos, url, volume, tick, isPlaying);
    }
}
