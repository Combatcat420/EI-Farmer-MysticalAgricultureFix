package net.swedz.extended_industrialization.machines.component.farmer;

import aztech.modern_industrialization.inventory.ChangeListener;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.world.item.Item;
import net.swedz.extended_industrialization.machines.component.farmer.planting.FarmerPlantable;

import java.util.Optional;

public final class PlantableConfigurableItemStack extends ChangeListener
{
	private final FarmerComponentPlantableStacks parent;
	private final ConfigurableItemStack          stack;
	
	private Item lastUpdateItem;
	
	private Optional<FarmerPlantable> plantable = Optional.empty();
	
	PlantableConfigurableItemStack(FarmerComponentPlantableStacks parent, ConfigurableItemStack stack)
	{
		this.parent = parent;
		this.stack = stack;
	}
	
	public ConfigurableItemStack getStack()
	{
		return stack;
	}
	
	public ItemVariant getItemVariant()
	{
		return (stack.isPlayerLocked() || stack.isMachineLocked()) && stack.getResource().isBlank() ?
				ItemVariant.of(stack.getLockedInstance()) :
				stack.getResource();
	}
	
	public Item getItem()
	{
		return this.getItemVariant().getItem();
	}
	
	public boolean isPlantable()
	{
		return plantable.isPresent();
	}
	
	public FarmerPlantable asPlantable()
	{
		return plantable.orElseThrow(() -> new IllegalStateException("Tried to get plantable of non-plantable itemStack"));
	}
	
	@Override
	protected void onChange()
	{
		ItemVariant itemVariant = this.getItemVariant();
		Item item = itemVariant.getItem();
		if(lastUpdateItem != item)
		{
			plantable = parent.getFarmer().getPlantableBehaviorHolder().behavior(itemVariant.toStack());
		}
		lastUpdateItem = item;
	}
	
	@Override
	protected boolean isValid(Object token)
	{
		return true;
	}
}
