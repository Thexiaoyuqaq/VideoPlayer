package com.github.NGoedix.videoplayer.network.Payload;

import com.github.NGoedix.videoplayer.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenVideoManagerScreenPayload(
        BlockPos pos,
        String url,
        int volume,
        int tick,
        boolean isPlaying
) implements CustomPacketPayload {

    public static final Type<OpenVideoManagerScreenPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "open_video_manager"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenVideoManagerScreenPayload> STREAM_CODEC = StreamCodec.of(
            OpenVideoManagerScreenPayload::toBytes,
            OpenVideoManagerScreenPayload::fromBytes
    );

    public static OpenVideoManagerScreenPayload fromBytes(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String url = buf.readUtf();
        int volume = buf.readInt();
        int tick = buf.readInt();
        boolean isPlaying = buf.readBoolean();
        return new OpenVideoManagerScreenPayload(pos, url, volume, tick, isPlaying);
    }

    public static void toBytes(RegistryFriendlyByteBuf buf, OpenVideoManagerScreenPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeUtf(payload.url);
        buf.writeInt(payload.volume);
        buf.writeInt(payload.tick);
        buf.writeBoolean(payload.isPlaying);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}