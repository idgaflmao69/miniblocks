package name.miniblocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class MiniblockBenchScreenHandler extends ScreenHandler {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int PLAYER_INVENTORY_START = 2;

    private final SimpleInventory inventory;
    private final ScreenHandlerContext context;
    private boolean refreshingOutput;

    public MiniblockBenchScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, new SimpleInventory(2), ScreenHandlerContext.create(playerInventory.player.getWorld(), pos));
    }

    private MiniblockBenchScreenHandler(int syncId, PlayerInventory playerInventory, SimpleInventory inventory, ScreenHandlerContext context) {
        super(Miniblocks.MINIBLOCK_BENCH_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.context = context;
        inventory.addListener(sender -> refreshOutput());

        addSlot(new Slot(inventory, INPUT_SLOT, 56, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return isValidInput(stack);
            }
        });
        addSlot(new Slot(inventory, OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeItems(PlayerEntity player) {
                return hasOutput();
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                super.onTakeItem(player, stack);
                consumeInput();
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
        refreshOutput();
    }

    private boolean isValidInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() == Miniblocks.MINIBLOCK_ITEM) return MiniblockItem.hasWrappedBlock(stack);
        return stack.getItem() instanceof BlockItem && stack.getItem() != Miniblocks.MINIBLOCK_BENCH_ITEM;
    }

    private ItemStack getOutput() {
        ItemStack input = inventory.getStack(INPUT_SLOT);
        if (!isValidInput(input)) return ItemStack.EMPTY;
        if (input.getItem() == Miniblocks.MINIBLOCK_ITEM) {
            if (input.getCount() < 8) return ItemStack.EMPTY;
            BlockState state = MiniblockItem.getWrappedBlockState(input);
            return new ItemStack(state.getBlock(), 1);
        }
        BlockItem blockItem = (BlockItem) input.getItem();
        return MiniblockItem.createStack(blockItem.getBlock().getDefaultState(), 8);
    }

    private boolean hasOutput() {
        return !getOutput().isEmpty();
    }

    private void refreshOutput() {
        if (refreshingOutput) return;
        refreshingOutput = true;
        inventory.setStack(OUTPUT_SLOT, getOutput());
        refreshingOutput = false;
        sendContentUpdates();
    }

    private void consumeInput() {
        ItemStack input = inventory.getStack(INPUT_SLOT);
        input.decrement(input.getItem() == Miniblocks.MINIBLOCK_ITEM ? 8 : 1);
        refreshOutput();
    }

    @Override
    public void onContentChanged(net.minecraft.inventory.Inventory inventory) {
        super.onContentChanged(inventory);
        refreshOutput();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, Miniblocks.MINIBLOCK_BENCH);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) return result;
        ItemStack original = slot.getStack();
        result = original.copy();
        if (slotIndex == OUTPUT_SLOT) {
            ItemStack output = original.copy();
            if (!insertItem(output, PLAYER_INVENTORY_START, slots.size(), true) || !output.isEmpty()) return ItemStack.EMPTY;
            slot.onTakeItem(player, original);
        } else if (slotIndex == INPUT_SLOT) {
            if (!insertItem(original, PLAYER_INVENTORY_START, slots.size(), false)) return ItemStack.EMPTY;
        } else if (!insertItem(original, INPUT_SLOT, OUTPUT_SLOT, false)) {
            return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return result;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (!player.getWorld().isClient()) {
            inventory.setStack(OUTPUT_SLOT, ItemStack.EMPTY);
            dropInventory(player, inventory);
        }
    }
}
