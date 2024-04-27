package net.swedz.extended_industrialization.registry.tags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class EITags
{
	public static TagKey<Item> itemForge(String path)
	{
		return TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("forge", path));
	}
	
	public static TagKey<Block> blockForge(String path)
	{
		return TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation("forge", path));
	}
}
