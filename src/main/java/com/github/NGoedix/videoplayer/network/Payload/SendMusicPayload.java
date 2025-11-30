package com.github.NGoedix.videoplayer.network.Payload;

import com.github.NGoedix.videoplayer.Reference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SendMusicPayload(
        MusicMessageType messageType,
        String url,
        int volume
) implements CustomPacketPayload {

    public static final Type<SendMusicPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "send_music"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SendMusicPayload> STREAM_CODEC = StreamCodec.of(
            SendMusicPayload::toBytes,
            SendMusicPayload::fromBytes
    );

    public enum MusicMessageType {
        START, STOP
    }

    public static SendMusicPayload fromBytes(RegistryFriendlyByteBuf buf) {
        MusicMessageType messageType = buf.readEnum(MusicMessageType.class);
        String url = "";
        int volume = 0;

        if (messageType == MusicMessageType.START) {
            url = buf.readUtf();
            volume = buf.readInt();
        }

        return new SendMusicPayload(messageType, url, volume);
    }

    public static void toBytes(RegistryFriendlyByteBuf buf, SendMusicPayload payload) {
        buf.writeEnum(payload.messageType);
        if (payload.messageType == MusicMessageType.START) {
            buf.writeUtf(payload.url);
            buf.writeInt(payload.volume);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}