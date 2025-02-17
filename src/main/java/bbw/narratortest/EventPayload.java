package bbw.narratortest;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record EventPayload(String type, String extra, long time)
  implements CustomPayload {
  public static final CustomPayload.Id<EventPayload> ID = new CustomPayload.Id<>(
    NarratorNetworkConstants.EVENT_PACKET_ID
  );
  public static final PacketCodec<RegistryByteBuf, EventPayload> CODEC = PacketCodec.tuple(
    PacketCodecs.STRING,
    EventPayload::type,
    PacketCodecs.STRING,
    EventPayload::extra,
    PacketCodecs.LONG,
    EventPayload::time,
    EventPayload::new
  );

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
