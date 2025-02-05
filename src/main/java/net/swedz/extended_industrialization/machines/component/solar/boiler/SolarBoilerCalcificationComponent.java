package net.swedz.extended_industrialization.machines.component.solar.boiler;

import aztech.modern_industrialization.machines.IComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public final class SolarBoilerCalcificationComponent implements IComponent.ServerOnly
{
	public static final long  START_AFTER_TICKS      = 3 * 60 * 60 * 20;
	public static final long  CALCIFICATION_DURATION = 3 * 60 * 60 * 20;
	public static final float MINIMUM_EFFICIENCY     = 0.33f;
	
	private long ticks;
	
	public void tick()
	{
		ticks = Math.min(++ticks, START_AFTER_TICKS + CALCIFICATION_DURATION);
	}
	
	public void reset()
	{
		ticks = 0;
	}
	
	public float getCalcification()
	{
		if(ticks <= START_AFTER_TICKS)
		{
			return 0f;
		}
		long calcificationTicks = ticks - START_AFTER_TICKS;
		return (float) calcificationTicks / CALCIFICATION_DURATION;
	}
	
	public float getEfficiency()
	{
		if(ticks <= START_AFTER_TICKS)
		{
			return 1f;
		}
		float ratio = 1 - this.getCalcification();
		float efficiencyRange = 1 - MINIMUM_EFFICIENCY;
		return MINIMUM_EFFICIENCY + (efficiencyRange * ratio);
	}
	
	@Override
	public void writeNbt(CompoundTag tag, HolderLookup.Provider registries)
	{
		tag.putLong("calcification_ticks", ticks);
	}
	
	@Override
	public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine)
	{
		ticks = tag.getLong("calcification_ticks");
	}
}
