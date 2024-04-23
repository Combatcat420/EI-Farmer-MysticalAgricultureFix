package net.swedz.miextended.datagen.client.provider.models;

import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.swedz.miextended.MIExtended;
import net.swedz.miextended.registry.items.MIEItems;
import net.swedz.miextended.registry.items.ItemHolder;

public final class ItemModelsDatagenProvider extends ItemModelProvider
{
	public ItemModelsDatagenProvider(GatherDataEvent event)
	{
		super(event.getGenerator().getPackOutput(), MIExtended.ID, event.getExistingFileHelper());
	}
	
	@Override
	protected void registerModels()
	{
		for(ItemHolder item : MIEItems.values())
		{
			ItemModelBuilder itemModelBuilder = this.getBuilder("item/%s".formatted(item.identifier().id()));
			item.modelBuilder().accept(itemModelBuilder);
		}
	}
	
	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}
}
