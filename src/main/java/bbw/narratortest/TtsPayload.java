package bbw.narratortest;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record TtsPayload(int entityId, byte[] audioData) implements CustomPayload {

    public static final CustomPayload.Id<TtsPayload> ID = new CustomPayload.Id<>(NarratorNetworkConstants.TTS_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, TtsPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER,
            TtsPayload::entityId, PacketCodecs.BYTE_ARRAY, TtsPayload::audioData, TtsPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
