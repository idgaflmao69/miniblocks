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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

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

    static void renderSubBlock(BlockState state, int x, int y, int z, BlockRenderView world, BlockPos pos,
                               MatrixStack matrices, VertexConsumerProvider vertexConsumers, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderManager renderManager = client.getBlockRenderManager();

        if (state.getRenderType() != BlockRenderType.MODEL) {
            renderManager.renderBlockAsEntity(state, matrices, vertexConsumers, 0, overlay);
            return;
        }

        BakedModel model = renderManager.getModel(state);
        int color = client.getBlockColors().getColor(state, world, pos, 0);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        RenderLayer layer = RenderLayers.getEntityBlockLayer(state, false);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(layer);

        renderManager.getModelRenderer().render(
                world,
                new QuadrantModel(model, x, y, z),
                state,
                pos,
                matrices,
                vertexConsumer,
                false,
                Random.create(),
                42L,
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
            float uRange = sprite.getMaxU() - sprite.getMinU();
            float vRange = sprite.getMaxV() - sprite.getMinV();
            float[] p = new float[4];
            float[] q = new float[4];
            float[] u = new float[4];
            float[] v = new float[4];

            for (int vertex = 0; vertex < 4; vertex++) {
                int positionIndex = vertex * 8;
                int uvIndex = vertex * 8 + 4;
                float px = Float.intBitsToFloat(vertexData[positionIndex]);
                float py = Float.intBitsToFloat(vertexData[positionIndex + 1]);
                float pz = Float.intBitsToFloat(vertexData[positionIndex + 2]);
                p[vertex] = getP(face, px, py, pz);
                q[vertex] = getQ(face, px, py, pz);
                u[vertex] = (Float.intBitsToFloat(vertexData[uvIndex]) - sprite.getMinU()) / uRange;
                v[vertex] = (Float.intBitsToFloat(vertexData[uvIndex + 1]) - sprite.getMinV()) / vRange;
            }

            TextureAxis uAxis = findTextureAxis(p, q, u);
            TextureAxis vAxis = findTextureAxis(p, q, v);
            if (uAxis == null || vAxis == null) {
                return quad;
            }

            float pOffset = getPOffset(face);
            float qOffset = getQOffset(face);
            for (int vertex = 0; vertex < 4; vertex++) {
                int uvIndex = vertex * 8 + 4;
                float uOffset = uAxis.pAxis ? pOffset : qOffset;
                float vOffset = vAxis.pAxis ? pOffset : qOffset;
                if (!uAxis.positive) uOffset = 1.0F - uOffset - 0.5F;
                if (!vAxis.positive) vOffset = 1.0F - vOffset - 0.5F;
                float adjustedU = uOffset + u[vertex] * 0.5F;
                float adjustedV = vOffset + v[vertex] * 0.5F;
                vertexData[uvIndex] = Float.floatToRawIntBits(sprite.getMinU() + adjustedU * uRange);
                vertexData[uvIndex + 1] = Float.floatToRawIntBits(sprite.getMinV() + adjustedV * vRange);
            }

            return new BakedQuad(vertexData, quad.getColorIndex(), face, sprite, quad.hasShade());
        }

        private float getP(Direction face, float px, float py, float pz) {
            return switch (face) {
                case NORTH -> 1.0F - px;
                case EAST -> 1.0F - pz;
                case SOUTH, UP, DOWN -> px;
                case WEST -> pz;
            };
        }

        private float getQ(Direction face, float px, float py, float pz) {
            return switch (face) {
                case UP -> pz;
                case DOWN -> 1.0F - pz;
                case NORTH, SOUTH, WEST, EAST -> 1.0F - py;
            };
        }

        private float getPOffset(Direction face) {
            return switch (face) {
                case NORTH -> (1 - x) * 0.5F;
                case EAST -> (1 - z) * 0.5F;
                case SOUTH, UP, DOWN -> x * 0.5F;
                case WEST -> z * 0.5F;
            };
        }

        private float getQOffset(Direction face) {
            return switch (face) {
                case UP -> z * 0.5F;
                case DOWN -> (1 - z) * 0.5F;
                case NORTH, SOUTH, WEST, EAST -> (1 - y) * 0.5F;
            };
        }

        private TextureAxis findTextureAxis(float[] p, float[] q, float[] texture) {
            float pCorrelation = correlation(p, texture);
            float qCorrelation = correlation(q, texture);
            if (Math.abs(pCorrelation) < 0.001F && Math.abs(qCorrelation) < 0.001F) {
                return null;
            }
            return Math.abs(pCorrelation) >= Math.abs(qCorrelation)
                    ? new TextureAxis(true, pCorrelation >= 0.0F)
                    : new TextureAxis(false, qCorrelation >= 0.0F);
        }

        private float correlation(float[] first, float[] second) {
            float firstAverage = average(first);
            float secondAverage = average(second);
            float result = 0.0F;
            for (int i = 0; i < first.length; i++) {
                result += (first[i] - firstAverage) * (second[i] - secondAverage);
            }
            return result;
        }

        private float average(float[] values) {
            float result = 0.0F;
            for (float value : values) result += value;
            return result / values.length;
        }

        private record TextureAxis(boolean pAxis, boolean positive) {}

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
