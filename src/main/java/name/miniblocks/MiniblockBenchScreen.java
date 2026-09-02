package name.miniblocks;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class MiniblockBenchScreen extends HandledScreen<MiniblockBenchScreenHandler> {
    public MiniblockBenchScreen(MiniblockBenchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
        playerInventoryTitleY = 72;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFF373737);
        context.fill(x + 1, y + 1, x + backgroundWidth - 1, y + backgroundHeight - 1, 0xFFC6C6C6);
        context.fill(x + 7, y + 20, x + 169, y + 62, 0xFF8B8B8B);
        context.fill(x + 7, y + 80, x + 169, y + 160, 0xFF8B8B8B);

        drawSlot(context, x + 55, y + 34);
        drawSlot(context, x + 115, y + 34);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(context, x + 7 + column * 18, y + 83 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(context, x + 7 + column * 18, y + 141);
        }
    }

    private void drawSlot(DrawContext context, int x, int y) {
        context.fill(x, y, x + 18, y + 18, 0xFF373737);
        context.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
    }
}
