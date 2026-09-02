package name.miniblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MiniblockItem extends BlockItem {

    public MiniblockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        BlockState state = world.getBlockState(pos);
        Direction side = context.getSide();

        // Case 1: Direct interaction with an existing sub-block inside a miniblock container
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

                // Populate empty slot if targeted directly
                if (miniblockEntity.subBlocks[index] == 0) {
                    if (world.isClient()) return ActionResult.SUCCESS;

                    miniblockEntity.subBlocks[index] = 1;
                    miniblockEntity.markDirtyAndSync();

                    if (player == null || !player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    return ActionResult.CONSUME;
                }
            }
        }

        // Case 2: Placing onto a standard block face OR overflowing to the adjacent space
        BlockPos targetPos = pos.offset(side);
        BlockState targetState = world.getBlockState(targetPos);

        if (targetState.isAir() || targetState.isReplaceable() || targetState.isOf(Miniblocks.MINIBLOCK)) {
            if (world.isClient()) return ActionResult.SUCCESS;

            // Ensure miniblock container exists at target position
            if (!targetState.isOf(Miniblocks.MINIBLOCK)) {
                net.minecraft.sound.BlockSoundGroup soundGroup = this.getBlock().getSoundGroup(this.getBlock().getDefaultState());
                world.playSound(null, targetPos, soundGroup.getPlaceSound(), net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 0.5F);

                boolean placed = world.setBlockState(targetPos, Miniblocks.MINIBLOCK.getDefaultState(), Block.NOTIFY_ALL);
                if (!placed) return ActionResult.PASS;
            }

            BlockEntity blockEntity = world.getBlockEntity(targetPos);
            if (blockEntity instanceof MiniblockEntity miniblockEntity) {
                Vec3d hitPos = context.getHitPos();

                // Calculate sub-quadrant relative to clicked block surface
                double relX = hitPos.x - pos.getX();
                double relY = hitPos.y - pos.getY();
                double relZ = hitPos.z - pos.getZ();

                int x = Math.max(0, Math.min(1, (int) Math.floor(relX * 2)));
                int y = Math.max(0, Math.min(1, (int) Math.floor(relY * 2)));
                int z = Math.max(0, Math.min(1, (int) Math.floor(relZ * 2)));

                // Lock only the axis corresponding to the clicked face, preserving cursor positioning on the other two axes
                if (side == Direction.UP) y = 0;
                else if (side == Direction.DOWN) y = 1;
                else if (side == Direction.NORTH) z = 1;
                else if (side == Direction.SOUTH) z = 0;
                else if (side == Direction.WEST) x = 1;
                else if (side == Direction.EAST) x = 0;

                int index = miniblockEntity.getIndex(x, y, z);

                if (miniblockEntity.subBlocks[index] == 0) {
                    miniblockEntity.subBlocks[index] = 1;
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