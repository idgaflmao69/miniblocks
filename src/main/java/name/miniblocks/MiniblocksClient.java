package name.miniblocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MiniblocksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(Miniblocks.MINIBLOCK_ENTITY, MiniblockEntityRenderer::new);

        // Receive the sync packet on the client
        ClientPlayNetworking.registerGlobalReceiver(MiniblockEntity.SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            net.minecraft.util.math.BlockPos pos = buf.readBlockPos();
            byte[] updatedData = new byte[8];
            for (int i = 0; i < 8; i++) {
                updatedData[i] = buf.readByte();
            }

            client.execute(() -> {
                if (client.world != null && client.world.getBlockEntity(pos) instanceof MiniblockEntity entity) {
                    System.arraycopy(updatedData, 0, entity.subBlocks, 0, 8);
                }
            });
        });
    }
}