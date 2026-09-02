package name.miniblocks;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MiniblockBenchBlock extends Block {
    public MiniblockBenchBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) {
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return Text.translatable("container.miniblocks.miniblock_bench");
                }

                @Override
                public MiniblockBenchScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
                    return new MiniblockBenchScreenHandler(syncId, inventory, pos);
                }

                @Override
                public void writeScreenOpeningData(net.minecraft.server.network.ServerPlayerEntity player, net.minecraft.network.PacketByteBuf buf) {
                    buf.writeBlockPos(pos);
                }
            });
        }
        return ActionResult.SUCCESS;
    }
}
