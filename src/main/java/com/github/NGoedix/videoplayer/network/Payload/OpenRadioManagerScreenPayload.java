package com.github.NGoedix.videoplayer.network.Payload;

import com.github.NGoedix.videoplayer.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenRadioManagerScreenPayload(
        BlockPos pos,
        String url,
        int volume,
        boolean isPlaying
) implements CustomPacketPayload {

    public static final Type<OpenRadioManagerScreenPayload> TYPE = new Type<>(new ResourceLocation(Reference.MOD_ID, "open_radio_manager"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRadioManagerScreenPayload> STREAM_CODEC = StreamCodec.of(
            OpenRadioManagerScreenPayload::toBytes,
            OpenRadioManagerScreenPayload::fromBytes
    );

    public static OpenRadioManagerScreenPayload fromBytes(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String url = buf.readUtf();
        int volume = buf.readInt();
        boolean isPlaying = buf.readBoolean();
        return new OpenRadioManagerScreenPayload(pos, url, volume, isPlaying);
    }

    public static void toBytes(RegistryFriendlyByteBuf buf, OpenRadioManagerScreenPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeUtf(payload.url);
        buf.writeInt(payload.volume);
        buf.writeBoolean(payload.isPlaying);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}