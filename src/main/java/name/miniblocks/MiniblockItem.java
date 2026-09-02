package name.miniblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class MiniblockItem extends BlockItem {
    public static final String NBT_KEY = "wrapped_block_id";

    public MiniblockItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static ItemStack createStack(BlockState blockState, int count) {
        ItemStack stack = new ItemStack(Miniblocks.MINIBLOCK_ITEM, count);
        Block block = blockState.getBlock();
        stack.getOrCreateNbt().putString(NBT_KEY, Registries.BLOCK.getId(block).toString());
        return stack;
    }

    public static boolean hasWrappedBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Miniblocks.MINIBLOCK_ITEM && stack.getNbt() != null && stack.getNbt().contains(NBT_KEY);
    }

    public static BlockState getWrappedBlockState(ItemStack stack) {
        if (!hasWrappedBlock(stack)) {
            return Blocks.STONE.getDefaultState();
        }

        String idString = stack.getNbt().getString(NBT_KEY);
        if (idString.isEmpty()) {
            return Blocks.STONE.getDefaultState();
        }

        Identifier id = Identifier.tryParse(idString);
        if (id == null) {
            return Blocks.STONE.getDefaultState();
        }

        Block block = Registries.BLOCK.get(id);
        return block != null ? block.getDefaultState() : Blocks.STONE.getDefaultState();
    }

    private BlockState getHeldBlockState(ItemStack stack, ItemUsageContext context) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }

        Block block = blockItem.getBlock();
        if (block == Miniblocks.MINIBLOCK) {
            return null;
        }

        BlockState placementState = block.getPlacementState(new ItemPlacementContext(context));
        return placementState != null ? placementState : block.getDefaultState();
    }

    private boolean intersectsPlayer(PlayerEntity player, BlockPos pos, int x, int y, int z) {
        if (player == null) {
            return false;
        }

        VoxelShape miniCube = Block.createCuboidShape(
                x * 8, y * 8, z * 8,
                (x + 1) * 8, (y + 1) * 8, (z + 1) * 8
        ).offset(pos.getX(), pos.getY(), pos.getZ());
        VoxelShape playerShape = VoxelShapes.cuboid(player.getBoundingBox());
        return VoxelShapes.matchesAnywhere(miniCube, playerShape, BooleanBiFunction.AND);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        BlockState state = world.getBlockState(pos);
        Direction side = context.getSide();

        if (state.isOf(Miniblocks.MINIBLOCK)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof MiniblockEntity miniblockEntity) {
                Vec3d hitPos = context.getHitPos();

                double relX = hitPos.x - pos.getX();
                double relY = hitPos.y - pos.getY();
                double relZ = hitPos.z - pos.getZ();

                if (side == Direction.WEST) relX -= 0.05;
                if (side == Direction.EAST) relX += 0.05;
                if (side == Direction.DOWN) relY -= 0.05;
                if (side == Direction.UP) relY += 0.05;
                if (side == Direction.NORTH) relZ -= 0.05;
                if (side == Direction.SOUTH) relZ += 0.05;

                int x = Math.max(0, Math.min(1, (int) Math.floor(relX * 2)));
                int y = Math.max(0, Math.min(1, (int) Math.floor(relY * 2)));
                int z = Math.max(0, Math.min(1, (int) Math.floor(relZ * 2)));

                int index = miniblockEntity.getIndex(x, y, z);

                if (miniblockEntity.isSlotEmpty(index)) {
                    BlockState subBlockState = getHeldBlockState(stack, context);
                    if (subBlockState == null && hasWrappedBlock(stack)) {
                        subBlockState = getWrappedBlockState(stack);
                    }
                    if (subBlockState == null) {
                        return ActionResult.PASS;
                    }

                    if (intersectsPlayer(player, pos, x, y, z)) {
                        return ActionResult.PASS;
                    }

                    if (world.isClient()) return ActionResult.SUCCESS;

                    miniblockEntity.setSubBlock(index, subBlockState);
                    miniblockEntity.markDirtyAndSync();

                    if (player == null || !player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    return ActionResult.CONSUME;
                }
            }
        }

        BlockPos targetPos = pos.offset(side);
        BlockState targetState = world.getBlockState(targetPos);

        if (targetState.isAir() || targetState.isReplaceable() || targetState.isOf(Miniblocks.MINIBLOCK)) {
            Vec3d hitPos = context.getHitPos();

            double relX = hitPos.x - pos.getX();
            double relY = hitPos.y - pos.getY();
            double relZ = hitPos.z - pos.getZ();

            int x = Math.max(0, Math.min(1, (int) Math.floor(relX * 2)));
            int y = Math.max(0, Math.min(1, (int) Math.floor(relY * 2)));
            int z = Math.max(0, Math.min(1, (int) Math.floor(relZ * 2)));

            if (side == Direction.UP) y = 0;
            else if (side == Direction.DOWN) y = 1;
            else if (side == Direction.NORTH) z = 1;
            else if (side == Direction.SOUTH) z = 0;
            else if (side == Direction.WEST) x = 1;
            else if (side == Direction.EAST) x = 0;

            if (intersectsPlayer(player, targetPos, x, y, z)) {
                return ActionResult.PASS;
            }

            if (world.isClient()) return ActionResult.SUCCESS;

            if (!targetState.isOf(Miniblocks.MINIBLOCK)) {
                net.minecraft.sound.BlockSoundGroup soundGroup = this.getBlock().getSoundGroup(this.getBlock().getDefaultState());
                world.playSound(null, targetPos, soundGroup.getPlaceSound(), net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 0.5F);

                boolean placed = world.setBlockState(targetPos, Miniblocks.MINIBLOCK.getDefaultState(), Block.NOTIFY_ALL);
                if (!placed) return ActionResult.PASS;
            }

            BlockEntity blockEntity = world.getBlockEntity(targetPos);
            if (blockEntity instanceof MiniblockEntity miniblockEntity) {
                int index = miniblockEntity.getIndex(x, y, z);

                if (miniblockEntity.isSlotEmpty(index)) {
                    BlockState subBlockState = getHeldBlockState(stack, context);
                    if (subBlockState == null && hasWrappedBlock(stack)) {
                        subBlockState = getWrappedBlockState(stack);
                    }
                    if (subBlockState == null) {
                        return ActionResult.PASS;
                    }

                    miniblockEntity.setSubBlock(index, subBlockState);
                    miniblockEntity.markDirtyAndSync();

                    if (player == null || !player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    return ActionResult.CONSUME;
                }
            }
        }

        return super.useOnBlock(context);
    }
}