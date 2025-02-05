package net.swedz.extended_industrialization.machines.component.farmer.planting;

import net.minecraft.world.item.ItemStack;
import net.swedz.tesseract.neoforge.behavior.BehaviorHolder;

import java.util.List;
import java.util.Optional;

public final class PlantableBehaviorHolder extends BehaviorHolder<FarmerPlantable, PlantingContext>
{
	public PlantableBehaviorHolder(List<FarmerPlantable> handlers)
	{
		super(handlers);
	}
	
	public Optional<FarmerPlantable> behavior(ItemStack stack)
	{
		return this.behavior(new PlantingContext(null, null, stack));
	}
}
