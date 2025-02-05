package net.swedz.extended_industrialization.machines.blockentity;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.diesel_tools.DieselToolItem;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.Tickable;
import com.google.common.collect.Lists;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.swedz.extended_industrialization.EI;
import net.swedz.extended_industrialization.EIFluids;
import net.swedz.extended_industrialization.EIText;
import net.swedz.extended_industrialization.EITooltips;
import net.swedz.extended_industrialization.item.ElectricToolItem;
import net.swedz.extended_industrialization.item.SteamChainsawItem;
import net.swedz.extended_industrialization.machines.component.solar.SolarSunlightComponent;
import net.swedz.extended_industrialization.machines.component.solar.boiler.SolarBoilerCalcificationComponent;
import net.swedz.extended_industrialization.machines.guicomponent.solarefficiency.SolarEfficiencyBar;
import net.swedz.tesseract.neoforge.compat.mi.component.SteamProductionComponent;
import net.swedz.tesseract.neoforge.compat.mi.tooltip.MICompatibleTextLine;

import java.util.List;

public final class SolarBoilerMachineBlockEntity extends MachineBlockEntity implements Tickable
{
	private static final int WATER_SLOT_X = 38;
	private static final int WATER_SLOT_Y = 32;
	
	private static final int OUTPUT_SLOT_X = 122;
	private static final int OUTPUT_SLOT_Y = 32;
	
	private static final int BURNING_PROGRESS_X = 120;
	private static final int BURNING_PROGRESS_Y = 50;
	
	private static final int SOLAR_EFFICIENCY_X = 38;
	private static final int SOLAR_EFFICIENCY_Y = 75;
	
	private final MIInventory inventory;
	
	private final boolean bronze;
	private final long    maxEuProduction;
	
	private final SolarSunlightComponent            sunlight;
	private final SolarBoilerCalcificationComponent calcification;
	
	private final SteamProductionComponent steamProduction;
	
	private final IsActiveComponent isActiveComponent;
	
	public SolarBoilerMachineBlockEntity(BEP bep, boolean bronze)
	{
		super(
				bep,
				new MachineGuiParameters.Builder(EI.id(bronze ? "bronze_solar_boiler" : "steel_solar_boiler"), true).backgroundHeight(180).build(),
				new OrientationComponent.Params(false, false, false)
		);
		
		this.bronze = bronze;
		maxEuProduction = bronze ? 4 : 8;
		
		int capacity = FluidType.BUCKET_VOLUME * (bronze ? 8 : 16);
		
		List<ConfigurableFluidStack> fluidStacks = List.of(
				ConfigurableFluidStack.standardInputSlot(capacity),
				ConfigurableFluidStack.lockedOutputSlot(capacity, MIFluids.STEAM.asFluid())
		);
		SlotPositions fluidPositions = new SlotPositions.Builder().addSlot(WATER_SLOT_X, WATER_SLOT_Y).addSlot(OUTPUT_SLOT_X, OUTPUT_SLOT_Y).build();
		inventory = new MIInventory(List.of(), fluidStacks, new SlotPositions.Builder().build(), fluidPositions);
		
		sunlight = new SolarSunlightComponent(this);
		calcification = new SolarBoilerCalcificationComponent();
		
		steamProduction = new SteamProductionComponent(
				inventory.fluidStorage,
				List.of(Fluids.WATER, EIFluids.DISTILLED_WATER.asFluid()),
				MIFluids.STEAM.variant(),
				() -> (long) (maxEuProduction * this.getEfficiency(true)),
				() -> 16
		);
		
		isActiveComponent = new IsActiveComponent();
		
		this.registerGuiComponent(new ProgressBar.Server(
				new ProgressBar.Parameters(BURNING_PROGRESS_X, BURNING_PROGRESS_Y, "furnace", true),
				() -> this.getEfficiency(false)
		));
		this.registerGuiComponent(SolarEfficiencyBar.Server.calcification(
				new SolarEfficiencyBar.Parameters(SOLAR_EFFICIENCY_X, SOLAR_EFFICIENCY_Y),
				sunlight::canOperate,
				() -> (int) (this.getEfficiency(true) * 100),
				() -> (int) (calcification.getCalcification() * 100)
		));
		
		this.registerComponents(inventory, sunlight, calcification, steamProduction, isActiveComponent);
	}
	
	public float getEfficiency(boolean includeCalficiation)
	{
		return includeCalficiation ? sunlight.getSolarEfficiency() * calcification.getEfficiency() : sunlight.getSolarEfficiency();
	}
	
	@Override
	public MIInventory getInventory()
	{
		return inventory;
	}
	
	@Override
	protected MachineModelClientData getMachineModelData()
	{
		MachineModelClientData data = new MachineModelClientData(bronze ? MachineCasings.BRICKED_BRONZE : MachineCasings.BRICKED_STEEL);
		data.isActive = isActiveComponent.isActive;
		orientation.writeModelData(data);
		return data;
	}
	
	@Override
	public void tick()
	{
		if(level.isClientSide())
		{
			return;
		}
		
		boolean active = true;
		
		if(sunlight.canOperate())
		{
			FluidVariant waterFluid = steamProduction.tryMakeSteam();
			if(waterFluid.isBlank())
			{
				active = false;
			}
			else if(!waterFluid.isOf(EIFluids.DISTILLED_WATER.asFluid()))
			{
				calcification.tick();
			}
		}
		else
		{
			active = false;
		}
		
		for(Direction direction : Direction.values())
		{
			this.getInventory().autoExtractFluids(level, worldPosition, direction);
		}
		
		isActiveComponent.updateActive(active, this);
		
		this.setChanged();
	}
	
	@Override
	protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face)
	{
		ItemInteractionResult result = super.useItemOn(player, hand, face);
		if(!result.consumesAction() && player != null)
		{
			ItemStack stack = player.getItemInHand(hand);
			if(stack.is(ItemTags.AXES))
			{
				boolean canUse;
				
				switch (stack.getItem())
				{
					case SteamChainsawItem steamChainsaw ->
					{
						canUse = steamChainsaw.canUse(stack);
						if(canUse)
						{
							steamChainsaw.useFuel(stack, player);
						}
					}
					case DieselToolItem dieselToolItem ->
					{
						canUse = FluidFuelItemHelper.getAmount(stack) > 0;
						if(canUse)
						{
							FluidFuelItemHelper.decrement(stack);
						}
					}
					case ElectricToolItem electricTool ->
					{
						canUse = electricTool.getStoredEnergy(stack) > 0;
						if(canUse)
						{
							electricTool.tryUseEnergy(stack, ElectricToolItem.ENERGY_COST);
						}
					}
					default ->
					{
						stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
						canUse = true;
					}
				}
				
				if(canUse)
				{
					calcification.reset();
					
					level.playSound(null, worldPosition, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1f, 1f);
					level.levelEvent(null, LevelEvent.PARTICLES_SCRAPE, worldPosition, 0);
					
					result = ItemInteractionResult.CONSUME;
				}
			}
		}
		return result;
	}
	
	@Override
	public List<Component> getTooltips()
	{
		List<Component> tooltips = Lists.newArrayList();
		tooltips.add(
				MICompatibleTextLine.line(MIText.MaxEuProductionSteam)
						.arg(maxEuProduction, MITooltips.EU_PER_TICK_PARSER)
						.arg(MIFluids.STEAM)
		);
		tooltips.add(
				MICompatibleTextLine.line(EIText.SOLAR_BOILER_CALCIFICATION)
						.arg(SolarBoilerCalcificationComponent.MINIMUM_EFFICIENCY, EITooltips.PERCENTAGE_PARSER)
						.arg(EIFluids.DISTILLED_WATER.asFluid())
		);
		return tooltips;
	}
}
