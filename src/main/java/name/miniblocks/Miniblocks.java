package name.miniblocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Miniblocks implements ModInitializer {
	public static final String MOD_ID = "miniblocks";

	public static final Block MINIBLOCK = new MiniblockBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).nonOpaque());
	public static final Block URANIUM_ORE = new Block(FabricBlockSettings.create().strength(3.0F));
	public static final MiniblockItem MINIBLOCK_ITEM = new MiniblockItem(MINIBLOCK, new FabricItemSettings());

	public static BlockEntityType<MiniblockEntity> MINIBLOCK_ENTITY;

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "miniblock"), MINIBLOCK);
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "uranium_ore"), URANIUM_ORE);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "miniblock"), MINIBLOCK_ITEM);
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(MOD_ID, "block_to_miniblocks"), MiniblockConversionRecipe.BLOCK_TO_MINIBLOCKS);
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(MOD_ID, "miniblocks_to_block"), MiniblockConversionRecipe.MINIBLOCKS_TO_BLOCK);

		MINIBLOCK_ENTITY = Registry.register(
				Registries.BLOCK_ENTITY_TYPE,
				new Identifier(MOD_ID, "miniblock_entity"),
				FabricBlockEntityTypeBuilder.create(MiniblockEntity::new, MINIBLOCK).build()
		);
	}
}