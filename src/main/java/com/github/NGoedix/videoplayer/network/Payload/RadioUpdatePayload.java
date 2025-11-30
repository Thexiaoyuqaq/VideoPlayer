package com.github.NGoedix.videoplayer.network.Payload;

import com.github.NGoedix.videoplayer.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RadioUpdatePayload(
        BlockPos pos,
        String url,
        int volume,
        int tick,
        boolean isPlaying,
        boolean exit
) implements CustomPacketPayload {

    public static final Type<RadioUpdatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "update_radio"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioUpdatePayload> STREAM_CODEC = StreamCodec.of(
            RadioUpdatePayload::toBytes,
            RadioUpdatePayload::fromBytes
    );

    public static RadioUpdatePayload fromBytes(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String url = buf.readUtf();
        int volume = buf.readInt();
        int tick = buf.readInt();
        boolean isPlaying = buf.readBoolean();
        boolean exit = buf.readBoolean();
        return new RadioUpdatePayload(pos, url, volume, tick, isPlaying, exit);
    }

    public static void toBytes(RegistryFriendlyByteBuf buf, RadioUpdatePayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeUtf(payload.url);
        buf.writeInt(payload.volume);
        buf.writeInt(payload.tick);
        buf.writeBoolean(payload.isPlaying);
        buf.writeBoolean(payload.exit);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}