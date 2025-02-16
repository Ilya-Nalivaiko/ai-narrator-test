package bbw.narratortest;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record TtsPayload(BlockPos pos, byte[] audioData) implements CustomPayload {

    public static final CustomPayload.Id<TtsPayload> ID = new CustomPayload.Id<>(TTSNetworkConstants.TTS_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, TtsPayload> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC,
            TtsPayload::pos, PacketCodecs.BYTE_ARRAY, TtsPayload::audioData, TtsPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
