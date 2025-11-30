package com.github.NGoedix.videoplayer.network.Payload;

import com.github.NGoedix.videoplayer.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RadioMessagePayload(
        String url,
        BlockPos pos,
        boolean playing
) implements CustomPacketPayload {

    public static final Type<RadioMessagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "radio"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioMessagePayload> STREAM_CODEC = StreamCodec.of(
            RadioMessagePayload::toBytes,
            RadioMessagePayload::fromBytes
    );

    public static RadioMessagePayload fromBytes(RegistryFriendlyByteBuf buf) {
        String url = buf.readUtf();
        BlockPos pos = buf.readBlockPos();
        boolean playing = buf.readBoolean();
        return new RadioMessagePayload(url, pos, playing);
    }

    public static void toBytes(RegistryFriendlyByteBuf buf, RadioMessagePayload payload) {
        buf.writeUtf(payload.url);
        buf.writeBlockPos(payload.pos);
        buf.writeBoolean(payload.playing);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}