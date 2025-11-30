package com.github.NGoedix.videoplayer.network.Payload;

import com.github.NGoedix.videoplayer.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FrameVideoPayload(
        String url,
        BlockPos pos,
        boolean playing,
        int tick
) implements CustomPacketPayload {

    public static final Type<FrameVideoPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "frame_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FrameVideoPayload> STREAM_CODEC = StreamCodec.of(
            FrameVideoPayload::toBytes,
            FrameVideoPayload::fromBytes
    );

    public static FrameVideoPayload fromBytes(RegistryFriendlyByteBuf buf) {
        String url = buf.readUtf();
        BlockPos pos = buf.readBlockPos();
        boolean playing = buf.readBoolean();
        int tick = buf.readInt();
        return new FrameVideoPayload(url, pos, playing, tick);
    }

    public static void toBytes(RegistryFriendlyByteBuf buf, FrameVideoPayload payload) {
        buf.writeUtf(payload.url);
        buf.writeBlockPos(payload.pos);
        buf.writeBoolean(payload.playing);
        buf.writeInt(payload.tick);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}