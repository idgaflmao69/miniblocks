package name.miniblocks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class MiniblockEntityRenderer implements BlockEntityRenderer<MiniblockEntity> {

    public MiniblockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(MiniblockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        boolean hasContent = false;
        for (byte b : entity.subBlocks) {
            if (b != 0) {
                hasContent = true;
                break;
            }
        }
        if (!hasContent) {
            return;
        }

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    byte subBlockId = entity.subBlocks[entity.getIndex(x, y, z)];
                    if (subBlockId != 0) {
                        matrices.push();
                        matrices.translate(x * 0.5D, y * 0.5D, z * 0.5D);
                        matrices.scale(0.5F, 0.5F, 0.5F);

                                MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                                Miniblocks.URANIUM_ORE.getDefaultState(), matrices, vertexConsumers, light, overlay
                        );

                        matrices.pop();
                    }
                }
            }
        }
    }
}