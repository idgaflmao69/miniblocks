package name.miniblocks;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

final class MiniblockRenderUtil {
    private MiniblockRenderUtil() {}

    static void renderSubBlock(BlockState state, int x, int y, int z, MatrixStack matrices,
                               VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderManager renderManager = client.getBlockRenderManager();

        if (state.getRenderType() != BlockRenderType.MODEL) {
            renderManager.renderBlockAsEntity(state, matrices, vertexConsumers, light, overlay);
            return;
        }

        BakedModel model = renderManager.getModel(state);
        int color = client.getBlockColors().getColor(state, null, null, 0);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        RenderLayer layer = RenderLayers.getEntityBlockLayer(state, false);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(layer);

        renderManager.getModelRenderer().render(
                matrices.peek(),
                vertexConsumer,
                state,
                new QuadrantModel(model, x, y, z),
                red,
                green,
                blue,
                light,
                overlay
        );
    }

    private static final class QuadrantModel implements BakedModel {
        private final BakedModel delegate;
        private final int x;
        private final int y;
        private final int z;

        private QuadrantModel(BakedModel delegate, int x, int y, int z) {
            this.delegate = delegate;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, Random random) {
            List<BakedQuad> quads = delegate.getQuads(state, side, random);
            if (quads.isEmpty()) {
                return quads;
            }

            List<BakedQuad> adjustedQuads = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                adjustedQuads.add(adjustQuad(quad));
            }
            return adjustedQuads;
        }

        private BakedQuad adjustQuad(BakedQuad quad) {
            int[] vertexData = quad.getVertexData().clone();
            Sprite sprite = quad.getSprite();
            Direction face = quad.getFace();
            float uOffset = getUOffset(face);
            float vOffset = getVOffset(face);
            float uRange = sprite.getMaxU() - sprite.getMinU();
            float vRange = sprite.getMaxV() - sprite.getMinV();

            for (int vertex = 0; vertex < 4; vertex++) {
                // Baked quads use POSITION_COLOR_TEXTURE_LIGHT_NORMAL (8 ints per vertex).
                int uvIndex = vertex * 8 + 4;
                float u = Float.intBitsToFloat(vertexData[uvIndex]);
                float v = Float.intBitsToFloat(vertexData[uvIndex + 1]);
                float normalizedU = (u - sprite.getMinU()) / uRange;
                float normalizedV = (v - sprite.getMinV()) / vRange;
                vertexData[uvIndex] = Float.floatToRawIntBits(
                        sprite.getMinU() + (uOffset + normalizedU * 0.5F) * uRange
                );
                vertexData[uvIndex + 1] = Float.floatToRawIntBits(
                        sprite.getMinV() + (vOffset + normalizedV * 0.5F) * vRange
                );
            }

            return new BakedQuad(vertexData, quad.getColorIndex(), face, sprite, quad.hasShade());
        }

        private float getUOffset(Direction face) {
            return switch (face) {
                case NORTH -> (1 - x) * 0.5F;
                case EAST -> (1 - z) * 0.5F;
                case SOUTH, UP, DOWN -> x * 0.5F;
                case WEST -> z * 0.5F;
            };
        }

        private float getVOffset(Direction face) {
            return switch (face) {
                case UP -> z * 0.5F;
                case DOWN -> (1 - z) * 0.5F;
                case NORTH, SOUTH, WEST, EAST -> (1 - y) * 0.5F;
            };
        }

        @Override
        public boolean useAmbientOcclusion() {
            return delegate.useAmbientOcclusion();
        }

        @Override
        public boolean hasDepth() {
            return delegate.hasDepth();
        }

        @Override
        public boolean isSideLit() {
            return delegate.isSideLit();
        }

        @Override
        public boolean isBuiltin() {
            return delegate.isBuiltin();
        }

        @Override
        public Sprite getParticleSprite() {
            return delegate.getParticleSprite();
        }

        @Override
        public net.minecraft.client.render.model.json.ModelTransformation getTransformation() {
            return delegate.getTransformation();
        }

        @Override
        public net.minecraft.client.render.model.json.ModelOverrideList getOverrides() {
            return delegate.getOverrides();
        }
    }
}
