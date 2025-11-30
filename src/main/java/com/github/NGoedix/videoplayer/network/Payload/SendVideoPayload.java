package com.github.NGoedix.videoplayer.network.Payload;

import com.github.NGoedix.videoplayer.Reference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SendVideoPayload(
        VideoMessageType messageType,
        String url,
        int volume,
        boolean controlBlocked,
        boolean canSkip
) implements CustomPacketPayload {

    public static final Type<SendVideoPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "send_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SendVideoPayload> STREAM_CODEC = StreamCodec.of(
            SendVideoPayload::toBytes,
            SendVideoPayload::fromBytes
    );

    public enum VideoMessageType {
        START, STOP
    }

    public static SendVideoPayload fromBytes(RegistryFriendlyByteBuf buf) {
        VideoMessageType messageType = buf.readEnum(VideoMessageType.class);
        String url = "";
        int volume = 0;
        boolean controlBlocked = false;
        boolean canSkip = false;

        if (messageType == VideoMessageType.START) {
            url = buf.readUtf();
            volume = buf.readInt();
            controlBlocked = buf.readBoolean();
            canSkip = buf.readBoolean();
        }

        return new SendVideoPayload(messageType, url, volume, controlBlocked, canSkip);
    }

    public static void toBytes(RegistryFriendlyByteBuf buf, SendVideoPayload payload) {
        buf.writeEnum(payload.messageType);
        if (payload.messageType == VideoMessageType.START) {
            buf.writeUtf(payload.url);
            buf.writeInt(payload.volume);
            buf.writeBoolean(payload.controlBlocked);
            buf.writeBoolean(payload.canSkip);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}