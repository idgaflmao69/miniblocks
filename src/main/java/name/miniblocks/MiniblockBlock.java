package name.miniblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class MiniblockBlock extends Block implements BlockEntityProvider {

    public MiniblockBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MiniblockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    private VoxelShape getSubBlocksShape(BlockView world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof MiniblockEntity miniblockEntity) {
            VoxelShape shape = VoxelShapes.empty();
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    for (int z = 0; z < 2; z++) {
                        if (!miniblockEntity.isSlotEmpty(miniblockEntity.getIndex(x, y, z))) {
                            VoxelShape miniCube = Block.createCuboidShape(x * 8, y * 8, z * 8, (x + 1) * 8, (y + 1) * 8, (z + 1) * 8);
                            shape = VoxelShapes.union(shape, miniCube);
                        }
                    }
                }
            }
            if (shape == VoxelShapes.empty()) {
                return VoxelShapes.fullCube();
            }
            return shape;
        }
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getSubBlocksShape(world, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getSubBlocksShape(world, pos);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && !player.isCreative()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof MiniblockEntity miniblockEntity) {
                int count = 0;
                for (int i = 0; i < miniblockEntity.subBlocks.length; i++) {
                    if (!miniblockEntity.isSlotEmpty(i)) {
                        count++;
                    }
                }

                if (count > 0) {
                    ItemStack dropStack = new ItemStack(this.asItem(), count);
                    Block.dropStack(world, pos, dropStack);
                }
            }
        }
        super.onBreak(world, pos, state, player);
    }
}