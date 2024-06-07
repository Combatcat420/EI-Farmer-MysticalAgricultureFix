package net.swedz.extended_industrialization;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.OverclockComponent;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.RecipeEfficiencyBar;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import com.google.common.collect.Maps;
import net.neoforged.neoforge.fluids.FluidType;
import net.swedz.extended_industrialization.machines.blockentities.MachineChainerMachineBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.SolarBoilerMachineBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.SolarPanelMachineBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.UniversalTransformerMachineBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.WirelessChargerMachineBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.brewery.ElectricBreweryMachineBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.brewery.SteamBreweryMachineBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.fluidharvesting.ElectricFluidHarvestingMachineBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.fluidharvesting.SteamFluidHarvestingMachineBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.multiblock.LargeElectricFurnaceBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.multiblock.ProcessingArrayBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.multiblock.farmer.ElectricFarmerBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.multiblock.farmer.SteamFarmerBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.multiblock.multiplied.ElectricMultipliedCraftingMultiblockBlockEntity;
import net.swedz.extended_industrialization.machines.blockentities.multiblock.multiplied.SteamMultipliedCraftingMultiblockBlockEntity;
import net.swedz.extended_industrialization.machines.components.craft.multiplied.EuCostTransformers;
import net.swedz.extended_industrialization.machines.components.fluidharvesting.honeyextractor.HoneyExtractorBehavior;
import net.swedz.extended_industrialization.machines.components.fluidharvesting.wastecollector.WasteCollectorBehavior;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.BlastFurnaceTiersMIHookContext;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.MachineCasingsMIHookContext;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.MachineRecipeTypesMIHookContext;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.MultiblockMachinesMIHookContext;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.SingleBlockCraftingMachinesMIHookContext;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.SingleBlockSpecialMachinesMIHookContext;

import java.util.Map;

import static aztech.modern_industrialization.machines.init.SingleBlockCraftingMachines.*;
import static aztech.modern_industrialization.machines.models.MachineCasings.*;

public final class EIMachines
{
	public static void blastFurnaceTiers(BlastFurnaceTiersMIHookContext hook)
	{
	}
	
	public static final class Casings
	{
		public static MachineCasing
				BRONZE_PIPE,
				STEEL_PIPE,
				STEEL_PLATED_BRICKS;
	}
	
	public static void casings(MachineCasingsMIHookContext hook)
	{
		Casings.BRONZE_PIPE = hook.register("bronze_pipe");
		Casings.STEEL_PIPE = hook.register("steel_pipe");
		Casings.STEEL_PLATED_BRICKS = hook.register("steel_plated_bricks");
	}
	
	public static final class RecipeTypes
	{
		public static MachineRecipeType
				BENDING_MACHINE,
				ALLOY_SMELTER,
				CANNING_MACHINE,
				COMPOSTER;
		
		private static final Map<MachineRecipeType, String> RECIPE_TYPE_NAMES = Maps.newHashMap();
		
		public static Map<MachineRecipeType, String> getNames()
		{
			return RECIPE_TYPE_NAMES;
		}
		
		private static MachineRecipeType create(MachineRecipeTypesMIHookContext hook, String englishName, String id)
		{
			MachineRecipeType recipeType = hook.create(id);
			RECIPE_TYPE_NAMES.put(recipeType, englishName);
			return recipeType;
		}
	}
	
	public static void recipeTypes(MachineRecipeTypesMIHookContext hook)
	{
		RecipeTypes.BENDING_MACHINE = RecipeTypes.create(hook, "Bending Machine", "bending_machine").withItemInputs().withItemOutputs();
		RecipeTypes.ALLOY_SMELTER = RecipeTypes.create(hook, "Alloy Smelter", "alloy_smelter").withItemInputs().withItemOutputs();
		RecipeTypes.CANNING_MACHINE = RecipeTypes.create(hook, "Canning Machine", "canning_machine").withItemInputs().withFluidInputs().withItemOutputs().withFluidOutputs();
		RecipeTypes.COMPOSTER = RecipeTypes.create(hook, "Composter", "composter").withItemInputs().withFluidInputs().withItemOutputs().withFluidOutputs();
	}
	
	public static void multiblocks(MultiblockMachinesMIHookContext hook)
	{
		hook.register(
				"Steam Farmer", "steam_farmer", "farmer",
				BRONZE_PLATED_BRICKS, true, true, false,
				SteamFarmerBlockEntity::new,
				(__) -> SteamFarmerBlockEntity.registerReiShapes()
		);
		
		hook.register(
				"Electric Farmer", "electric_farmer", "farmer",
				STEEL, true, true, false,
				ElectricFarmerBlockEntity::new,
				(__) -> ElectricFarmerBlockEntity.registerReiShapes()
		);
		
		hook.register(
				"Processing Array", "processing_array", "processing_array",
				CLEAN_STAINLESS_STEEL, true, false, false,
				ProcessingArrayBlockEntity::new,
				(__) -> ProcessingArrayBlockEntity.registerReiShapes()
		);
		
		{
			SimpleMember fireClayBricks = SimpleMember.forBlock(MIBlock.BLOCK_DEFINITIONS.get(MI.id("fire_clay_bricks")));
			SimpleMember bronzePlatedBricks = SimpleMember.forBlock(MIBlock.BLOCK_DEFINITIONS.get(MI.id("bronze_plated_bricks")));
			HatchFlags hatches = new HatchFlags.Builder().with(HatchType.ITEM_INPUT, HatchType.ITEM_OUTPUT, HatchType.FLUID_INPUT).build();
			ShapeTemplate shape = new ShapeTemplate.Builder(BRONZE_PLATED_BRICKS)
					.add3by3(-1, fireClayBricks, false, hatches)
					.add3by3(0, bronzePlatedBricks, true, HatchFlags.NO_HATCH)
					.add3by3(1, bronzePlatedBricks, false, HatchFlags.NO_HATCH)
					.build();
			hook.register(
					"Large Steam Furnace", "large_steam_furnace", "large_furnace",
					BRONZE_PLATED_BRICKS, true, false, false,
					(bep) -> new SteamMultipliedCraftingMultiblockBlockEntity(
							bep, "large_steam_furnace", new ShapeTemplate[]{shape},
							OverclockComponent.getDefaultCatalysts(),
							MIMachineRecipeTypes.FURNACE, 8, EuCostTransformers.percentage(() -> 0.75f)
					)
			);
			ReiMachineRecipes.registerMultiblockShape("large_steam_furnace", shape);
			ReiMachineRecipes.registerWorkstation("bronze_furnace", EI.id("large_steam_furnace"));
			ReiMachineRecipes.registerWorkstation("steel_furnace", EI.id("large_steam_furnace"));
		}
		
		hook.register(
				"Large Electric Furnace", "large_electric_furnace", "large_furnace",
				HEATPROOF, true, false, false,
				LargeElectricFurnaceBlockEntity::new
		);
		ReiMachineRecipes.registerWorkstation("bronze_furnace", EI.id("large_electric_furnace"));
		ReiMachineRecipes.registerWorkstation("steel_furnace", EI.id("large_electric_furnace"));
		ReiMachineRecipes.registerWorkstation("electric_furnace", EI.id("large_electric_furnace"));
		
		{
			SimpleMember bronzePlatedBricks = SimpleMember.forBlock(MIBlock.BLOCK_DEFINITIONS.get(MI.id("bronze_plated_bricks")));
			HatchFlags hatches = new HatchFlags.Builder().with(HatchType.ITEM_INPUT, HatchType.ITEM_OUTPUT, HatchType.FLUID_INPUT).build();
			ShapeTemplate shape = new ShapeTemplate.Builder(BRONZE_PLATED_BRICKS).add3by3LevelsRoofed(-1, 1, bronzePlatedBricks, hatches).build();
			hook.register(
					"Large Steam Macerator", "large_steam_macerator", "large_macerator",
					BRONZE_PLATED_BRICKS, true, false, false,
					(bep) -> new SteamMultipliedCraftingMultiblockBlockEntity(
							bep, "large_steam_macerator", new ShapeTemplate[]{shape},
							OverclockComponent.getDefaultCatalysts(),
							MIMachineRecipeTypes.MACERATOR, 8, EuCostTransformers.percentage(() -> 0.75f)
					)
			);
			ReiMachineRecipes.registerMultiblockShape("large_steam_macerator", shape);
			ReiMachineRecipes.registerWorkstation("bronze_macerator", EI.id("large_steam_macerator"));
			ReiMachineRecipes.registerWorkstation("steel_macerator", EI.id("large_steam_macerator"));
		}
		
		{
			SimpleMember steelPlatedBricks = SimpleMember.forBlock(EIBlocks.STEEL_PLATED_BRICKS);
			HatchFlags hatches = new HatchFlags.Builder().with(HatchType.ITEM_INPUT, HatchType.ITEM_OUTPUT, HatchType.ENERGY_INPUT).build();
			ShapeTemplate shape = new ShapeTemplate.Builder(Casings.STEEL_PLATED_BRICKS).add3by3LevelsRoofed(-1, 1, steelPlatedBricks, hatches).build();
			hook.register(
					"Large Electric Macerator", "large_electric_macerator", "large_macerator",
					Casings.STEEL_PLATED_BRICKS, true, false, false,
					(bep) -> new ElectricMultipliedCraftingMultiblockBlockEntity(
							bep, "large_electric_macerator", new ShapeTemplate[]{shape},
							MachineTier.MULTIBLOCK,
							MIMachineRecipeTypes.MACERATOR, 16, EuCostTransformers.percentage(() -> 0.75f)
					)
			);
			ReiMachineRecipes.registerMultiblockShape("large_electric_macerator", shape);
			ReiMachineRecipes.registerWorkstation("bronze_macerator", EI.id("large_electric_macerator"));
			ReiMachineRecipes.registerWorkstation("steel_macerator", EI.id("large_electric_macerator"));
			ReiMachineRecipes.registerWorkstation("electric_macerator", EI.id("large_electric_macerator"));
		}
	}
	
	public static void singleBlockCrafting(SingleBlockCraftingMachinesMIHookContext hook)
	{
		// @formatter:off
		
		hook.register(
				"Bending Machine", "bending_machine", RecipeTypes.BENDING_MACHINE,
				1, 1, 0, 0,
				(params) -> {},
				new ProgressBar.Parameters(77, 34, "compress"),
				new RecipeEfficiencyBar.Parameters(38, 62),
				new EnergyBar.Parameters(18, 30),
				(items) -> items.addSlot(56, 35).addSlot(102, 35),
				(fluids) -> {},
				true, false, false,
				TIER_BRONZE | TIER_STEEL | TIER_ELECTRIC,
				16
		);
		
		hook.register(
				"Alloy Smelter", "alloy_smelter", RecipeTypes.ALLOY_SMELTER,
				2, 1, 0, 0,
				(params) -> {},
				new ProgressBar.Parameters(88, 33, "arrow"),
				new RecipeEfficiencyBar.Parameters(38, 62),
				new EnergyBar.Parameters(14, 34),
				(items) -> items.addSlots(40, 35, 2, 1).addSlot(120, 35),
				(fluids) -> {},
				true, false, false,
				TIER_STEEL | TIER_ELECTRIC,
				16
		);
		
		hook.register(
				"Canning Machine", "canning_machine", RecipeTypes.CANNING_MACHINE,
				2, 2, 1, 1,
				(params) -> {},
				new ProgressBar.Parameters(79, 34, "canning"),
				new RecipeEfficiencyBar.Parameters(38, 66),
				new EnergyBar.Parameters(14, 35),
				(items) -> items.addSlots(58, 27, 1, 2).addSlots(102, 27, 1, 2),
				(fluids) -> fluids.addSlot(38, 27).addSlot(122, 27),
				true, false, true,
				TIER_STEEL | TIER_ELECTRIC,
				16
		);
		
		hook.register(
				"Composter", "composter", RecipeTypes.COMPOSTER,
				2, 2, 1, 1,
				(params) -> {},
				new ProgressBar.Parameters(78, 34, "centrifuge"),
				new RecipeEfficiencyBar.Parameters(38, 66),
				new EnergyBar.Parameters(14, 35),
				(items) -> items.addSlots(58, 27, 1, 2).addSlots(102, 27, 1, 2),
				(fluids) -> fluids.addSlot(38, 27).addSlot(122, 27),
				true, true, false,
				TIER_BRONZE | TIER_STEEL | TIER_ELECTRIC,
				16
		);
		
		// @formatter:on
	}
	
	public static void singleBlockSpecial(SingleBlockSpecialMachinesMIHookContext hook)
	{
		hook.register(
				"Bronze Solar Boiler", "bronze_solar_boiler", "solar_boiler",
				MachineCasings.BRICKED_BRONZE, true, true, false,
				(bep) -> new SolarBoilerMachineBlockEntity(bep, true),
				MachineBlockEntity::registerFluidApi
		);
		hook.register(
				"Steel Solar Boiler", "steel_solar_boiler", "solar_boiler",
				MachineCasings.BRICKED_STEEL, true, true, false,
				(bep) -> new SolarBoilerMachineBlockEntity(bep, false),
				MachineBlockEntity::registerFluidApi
		);
		
		hook.register(
				"Steel Honey Extractor", "steel_honey_extractor", "honey_extractor",
				MachineCasings.STEEL, true, false, true,
				(bep) -> new SteamFluidHarvestingMachineBlockEntity(
						bep, "steel_honey_extractor",
						2, HoneyExtractorBehavior.STEEL,
						16 * FluidType.BUCKET_VOLUME, EIFluids.HONEY
				),
				MachineBlockEntity::registerFluidApi
		);
		hook.register(
				"Electric Honey Extractor", "electric_honey_extractor", "honey_extractor",
				CableTier.LV.casing, true, false, true,
				(bep) -> new ElectricFluidHarvestingMachineBlockEntity(
						bep, "electric_honey_extractor",
						4, HoneyExtractorBehavior.ELECTRIC,
						32 * FluidType.BUCKET_VOLUME, EIFluids.HONEY
				),
				MachineBlockEntity::registerFluidApi,
				ElectricFluidHarvestingMachineBlockEntity::registerEnergyApi
		);
		
		hook.register(
				"Steel Brewery", "steel_brewery", "brewery",
				MachineCasings.STEEL, true, false, true,
				(bep) -> new SteamBreweryMachineBlockEntity(bep, false),
				MachineBlockEntity::registerItemApi,
				MachineBlockEntity::registerFluidApi
		);
		hook.register(
				"Electric Brewery", "electric_brewery", "brewery",
				CableTier.LV.casing, true, false, true,
				ElectricBreweryMachineBlockEntity::new,
				MachineBlockEntity::registerItemApi,
				MachineBlockEntity::registerFluidApi,
				ElectricBreweryMachineBlockEntity::registerEnergyApi
		);
		
		hook.register(
				"Bronze Waste Collector", "bronze_waste_collector", "waste_collector",
				MachineCasings.BRONZE, false, true, false,
				(bep) -> new SteamFluidHarvestingMachineBlockEntity(
						bep, "bronze_waste_collector",
						1, WasteCollectorBehavior.BRONZE,
						8 * FluidType.BUCKET_VOLUME, EIFluids.MANURE
				),
				MachineBlockEntity::registerFluidApi
		);
		hook.register(
				"Steel Waste Collector", "steel_waste_collector", "waste_collector",
				MachineCasings.STEEL, false, true, false,
				(bep) -> new SteamFluidHarvestingMachineBlockEntity(
						bep, "steel_waste_collector",
						2, WasteCollectorBehavior.STEEL,
						16 * FluidType.BUCKET_VOLUME, EIFluids.MANURE
				),
				MachineBlockEntity::registerFluidApi
		);
		hook.register(
				"Electric Waste Collector", "electric_waste_collector", "waste_collector",
				CableTier.LV.casing, false, true, false,
				(bep) -> new ElectricFluidHarvestingMachineBlockEntity(
						bep, "electric_waste_collector",
						4, WasteCollectorBehavior.ELECTRIC,
						32 * FluidType.BUCKET_VOLUME, EIFluids.MANURE
				),
				MachineBlockEntity::registerFluidApi,
				ElectricFluidHarvestingMachineBlockEntity::registerEnergyApi
		);
		
		hook.register(
				"Machine Chainer", "machine_chainer",
				MachineChainerMachineBlockEntity::new,
				MachineChainerMachineBlockEntity::registerCapabilities
		);
		
		hook.register(
				"Universal Transformer", "universal_transformer", "universal_transformer",
				CableTier.LV.casing, false, true, true, false,
				UniversalTransformerMachineBlockEntity::new,
				UniversalTransformerMachineBlockEntity::registerEnergyApi
		);
		
		for(CableTier tier : new CableTier[]{CableTier.LV, CableTier.MV, CableTier.HV})
		{
			String name = "%s Solar Panel".formatted(tier.shortEnglishName);
			String id = "%s_solar_panel".formatted(tier.name);
			String overlayFolder = "solar_panel/%s".formatted(tier.name);
			hook.register(
					name, id, overlayFolder,
					tier.casing, false, true, true, false,
					(bep) -> new SolarPanelMachineBlockEntity(bep, id, tier),
					MachineBlockEntity::registerItemApi,
					MachineBlockEntity::registerFluidApi,
					SolarPanelMachineBlockEntity::registerEnergyApi
			);
		}
		
		hook.register(
				"Local Wireless Charging Station", "local_wireless_charging_station", "wireless_charging_station/local",
				CableTier.MV.casing, false, true, true, false,
				(bep) -> new WirelessChargerMachineBlockEntity(bep, "local_wireless_charging_station", CableTier.MV, (m, p) -> m.getBlockPos().closerThan(p.blockPosition(), EIConfig.localWirelessChargingStationRange)),
				WirelessChargerMachineBlockEntity::registerEnergyApi
		);
		hook.register(
				"Global Wireless Charging Station", "global_wireless_charging_station", "wireless_charging_station/global",
				CableTier.HV.casing, false, true, true, false,
				(bep) -> new WirelessChargerMachineBlockEntity(bep, "global_wireless_charging_station", CableTier.HV, (m, p) -> m.getLevel() == p.level()),
				WirelessChargerMachineBlockEntity::registerEnergyApi
		);
		hook.register(
				"Interdimensional Wireless Charging Station", "interdimensional_wireless_charging_station", "wireless_charging_station/interdimensional",
				CableTier.EV.casing, false, true, true, false,
				(bep) -> new WirelessChargerMachineBlockEntity(bep, "interdimensional_wireless_charging_station", CableTier.EV, (m, p) -> true),
				WirelessChargerMachineBlockEntity::registerEnergyApi
		);
	}
}
