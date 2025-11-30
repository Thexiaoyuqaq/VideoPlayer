package com.github.NGoedix.videoplayer.network.Payload;

import com.github.NGoedix.videoplayer.Reference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SendCustomVideoPayload(
        VideoMessageType messageType,
        String url,
        int volume,
        boolean controlBlocked,
        boolean canSkip,
        int mode,
        int position,
        int optionInMode,
        int optionInSecs,
        int optionOutMode,
        int optionOutSecs
) implements CustomPacketPayload {

    public static final Type<SendCustomVideoPayload> TYPE = new Type<>(new ResourceLocation(Reference.MOD_ID, "send_custom_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SendCustomVideoPayload> STREAM_CODEC = StreamCodec.of(
            SendCustomVideoPayload::toBytes,
            SendCustomVideoPayload::fromBytes
    );

    public enum VideoMessageType {
        START, STOP
    }

    public static SendCustomVideoPayload fromBytes(RegistryFriendlyByteBuf buf) {
        VideoMessageType messageType = buf.readEnum(VideoMessageType.class);
        String url = "";
        int volume = 0;
        boolean controlBlocked = false;
        boolean canSkip = false;
        int mode = 0;
        int position = 0;
        int optionInMode = 0;
        int optionInSecs = 0;
        int optionOutMode = 0;
        int optionOutSecs = 0;

        if (messageType == VideoMessageType.START) {
            url = buf.readUtf();
            volume = buf.readInt();
            controlBlocked = buf.readBoolean();
            canSkip = buf.readBoolean();
            mode = buf.readInt();
            position = buf.readInt();
            optionInMode = buf.readInt();
            optionInSecs = buf.readInt();
            optionOutMode = buf.readInt();
            optionOutSecs = buf.readInt();
        }

        return new SendCustomVideoPayload(messageType, url, volume, controlBlocked, canSkip, mode, position, optionInMode, optionInSecs, optionOutMode, optionOutSecs);
    }

    public static void toBytes(RegistryFriendlyByteBuf buf, SendCustomVideoPayload payload) {
        buf.writeEnum(payload.messageType);
        if (payload.messageType == VideoMessageType.START) {
            buf.writeUtf(payload.url);
            buf.writeInt(payload.volume);
            buf.writeBoolean(payload.controlBlocked);
            buf.writeBoolean(payload.canSkip);
            buf.writeInt(payload.mode);
            buf.writeInt(payload.position);
            buf.writeInt(payload.optionInMode);
            buf.writeInt(payload.optionInSecs);
            buf.writeInt(payload.optionOutMode);
            buf.writeInt(payload.optionOutSecs);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}