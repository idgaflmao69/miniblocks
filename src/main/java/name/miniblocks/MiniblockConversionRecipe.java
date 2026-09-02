package name.miniblocks;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class MiniblockConversionRecipe implements CraftingRecipe {
    public static final RecipeSerializer<MiniblockConversionRecipe> BLOCK_TO_MINIBLOCKS = new Serializer(true);
    public static final RecipeSerializer<MiniblockConversionRecipe> MINIBLOCKS_TO_BLOCK = new Serializer(false);

    private final Identifier id;
    private final boolean toMiniblocks;

    public MiniblockConversionRecipe(Identifier id, boolean toMiniblocks) {
        this.id = id;
        this.toMiniblocks = toMiniblocks;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        if (toMiniblocks) {
            return matchesBlockToMiniblocks(inventory);
        }
        return matchesMiniblocksToBlock(inventory);
    }

    private boolean matchesBlockToMiniblocks(RecipeInputInventory inventory) {
        ItemStack candidate = null;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (candidate != null) {
                return false;
            }
            if (stack.getItem() == Miniblocks.MINIBLOCK_ITEM || !(stack.getItem() instanceof net.minecraft.item.BlockItem)) {
                return false;
            }
            candidate = stack;
        }
        return candidate != null && candidate.getCount() == 1;
    }

    private boolean matchesMiniblocksToBlock(RecipeInputInventory inventory) {
        BlockState expectedState = null;
        int miniblockCount = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() != Miniblocks.MINIBLOCK_ITEM || !MiniblockItem.hasWrappedBlock(stack)) {
                return false;
            }

            BlockState state = MiniblockItem.getWrappedBlockState(stack);
            if (expectedState == null) {
                expectedState = state;
            } else if (!expectedState.isOf(state.getBlock())) {
                return false;
            }

            miniblockCount += stack.getCount();
        }

        return expectedState != null && miniblockCount == 8;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        if (toMiniblocks) {
            return craftBlockToMiniblocks(inventory);
        }
        return craftMiniblocksToBlock(inventory);
    }

    private ItemStack craftBlockToMiniblocks(RecipeInputInventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof net.minecraft.item.BlockItem blockItem) {
                BlockState state = blockItem.getBlock().getDefaultState();
                return MiniblockItem.createStack(state, 8);
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack craftMiniblocksToBlock(RecipeInputInventory inventory) {
        BlockState state = null;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Miniblocks.MINIBLOCK_ITEM) {
                state = MiniblockItem.getWrappedBlockState(stack);
                break;
            }
        }

        if (state == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(state.getBlock(), 1);
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        if (toMiniblocks) {
            return MiniblockItem.createStack(net.minecraft.block.Blocks.STONE.getDefaultState(), 8);
        }
        return new ItemStack(net.minecraft.block.Blocks.STONE, 1);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return toMiniblocks ? BLOCK_TO_MINIBLOCKS : MINIBLOCKS_TO_BLOCK;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.MISC;
    }

    private static final class Serializer implements RecipeSerializer<MiniblockConversionRecipe> {
        private final boolean toMiniblocks;

        private Serializer(boolean toMiniblocks) {
            this.toMiniblocks = toMiniblocks;
        }

        @Override
        public MiniblockConversionRecipe read(Identifier id, JsonObject json) {
            return new MiniblockConversionRecipe(id, toMiniblocks);
        }

        @Override
        public MiniblockConversionRecipe read(Identifier id, net.minecraft.network.PacketByteBuf buf) {
            return new MiniblockConversionRecipe(id, buf.readBoolean());
        }

        @Override
        public void write(net.minecraft.network.PacketByteBuf buf, MiniblockConversionRecipe recipe) {
            buf.writeBoolean(recipe.toMiniblocks);
        }
    }
}
