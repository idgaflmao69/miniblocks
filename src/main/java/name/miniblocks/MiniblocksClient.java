package name.miniblocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;

public class MiniblocksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(Miniblocks.MINIBLOCK_BENCH_SCREEN_HANDLER, MiniblockBenchScreen::new);
        BlockEntityRendererRegistry.register(Miniblocks.MINIBLOCK_ENTITY, MiniblockEntityRenderer::new);
        BuiltinItemRendererRegistry.INSTANCE.register(Miniblocks.MINIBLOCK_ITEM, (stack, mode, matrices, vertexConsumers, light, overlay) -> {
            BlockState state = MiniblockItem.getWrappedBlockState(stack);
            matrices.push();
            matrices.translate(0.5D, 0.5D, 0.5D);
            matrices.scale(0.75F, 0.75F, 0.75F);
            matrices.translate(-0.5D, -0.5D, -0.5D);
            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state, matrices, vertexConsumers, light, overlay);
            matrices.pop();
        });

        ClientPlayNetworking.registerGlobalReceiver(MiniblockEntity.SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            net.minecraft.util.math.BlockPos pos = buf.readBlockPos();
            int[] updatedData = new int[8];
            for (int i = 0; i < 8; i++) {
                updatedData[i] = buf.readVarInt();
            }

            client.execute(() -> {
                if (client.world != null && client.world.getBlockEntity(pos) instanceof MiniblockEntity entity) {
                    System.arraycopy(updatedData, 0, entity.subBlocks, 0, 8);
                }
            });
        });
    }
}
