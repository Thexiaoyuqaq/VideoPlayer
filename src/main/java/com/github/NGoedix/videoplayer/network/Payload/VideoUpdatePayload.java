package com.github.NGoedix.videoplayer.network.Payload;

import com.github.NGoedix.videoplayer.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VideoUpdatePayload(
        BlockPos pos,
        String url,
        int volume,
        int tick,
        boolean isPlaying,
        boolean stopped,
        boolean exit
) implements CustomPacketPayload {

    public static final Type<VideoUpdatePayload> TYPE = new Type<>(new ResourceLocation(Reference.MOD_ID, "update_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VideoUpdatePayload> STREAM_CODEC = StreamCodec.of(
            VideoUpdatePayload::toBytes,
            VideoUpdatePayload::fromBytes
    );

    public static VideoUpdatePayload fromBytes(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String url = buf.readUtf();
        int volume = buf.readInt();
        int tick = buf.readInt();
        boolean isPlaying = buf.readBoolean();
        boolean stopped = buf.readBoolean();
        boolean exit = buf.readBoolean();
        return new VideoUpdatePayload(pos, url, volume, tick, isPlaying, stopped, exit);
    }

    public static void toBytes(RegistryFriendlyByteBuf buf, VideoUpdatePayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeUtf(payload.url);
        buf.writeInt(payload.volume);
        buf.writeInt(payload.tick);
        buf.writeBoolean(payload.isPlaying);
        buf.writeBoolean(payload.stopped);
        buf.writeBoolean(payload.exit);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}