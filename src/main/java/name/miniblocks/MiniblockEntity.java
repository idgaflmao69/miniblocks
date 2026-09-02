package name.miniblocks;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class MiniblockEntity extends BlockEntity {

    // Start completely empty (0 = empty, 1 = filled)
    public final byte[] subBlocks = new byte[8];
    public static final Identifier SYNC_PACKET_ID = new Identifier("miniblocks", "sync_miniblock");

    public MiniblockEntity(BlockPos pos, BlockState state) {
        super(Miniblocks.MINIBLOCK_ENTITY, pos, state);
    }

    public int getIndex(int x, int y, int z) {
        return x + (y * 2) + (z * 4);
    }

    public void markDirtyAndSync() {
        this.markDirty();
        if (world != null && !world.isClient()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            for (int i = 0; i < 8; i++) {
                buf.writeByte(subBlocks[i]);
            }
            for (ServerPlayerEntity player : ((ServerWorld) world).getPlayers()) {
                if (player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64 * 64) {
                    ServerPlayNetworking.send(player, SYNC_PACKET_ID, buf);
                }
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putByteArray("SubBlocks", subBlocks);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("SubBlocks")) {
            byte[] loaded = nbt.getByteArray("SubBlocks");
            System.arraycopy(loaded, 0, subBlocks, 0, Math.min(loaded.length, subBlocks.length));
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putByteArray("SubBlocks", subBlocks);
        return nbt;
    }
}