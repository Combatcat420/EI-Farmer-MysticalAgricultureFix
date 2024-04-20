package net.swedz.miextended.machines.components;

import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.MultiblockInventoryComponent;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.swedz.miextended.api.MachineInventoryHelper;
import net.swedz.miextended.api.event.FarmlandLoseMoistureEvent;
import net.swedz.miextended.api.isolatedlistener.IsolatedListener;
import net.swedz.miextended.api.isolatedlistener.IsolatedListeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FarmerComponent implements IComponent, IsolatedListener<FarmlandLoseMoistureEvent>
{
	private final MultiblockInventoryComponent inventory;
	private final IsActiveComponent            isActive;
	
	public boolean tilling;
	
	private Level        level;
	private ShapeMatcher shapeMatcher;
	
	private List<BlockPos> dirtPositions = List.of();
	
	public FarmerComponent(MultiblockInventoryComponent inventory, IsActiveComponent isActive)
	{
		this.inventory = inventory;
		this.isActive = isActive;
	}
	
	public void fromOffsets(BlockPos controllerPos, Direction controllerDirection, List<BlockPos> offsets)
	{
		List<BlockPos> dirtPositions = new ArrayList<>(offsets.size());
		for(BlockPos offset : offsets)
		{
			BlockPos worldPos = ShapeMatcher.toWorldPos(controllerPos, controllerDirection, offset);
			dirtPositions.add(worldPos);
		}
		this.dirtPositions = Collections.unmodifiableList(dirtPositions);
	}
	
	private boolean consumeWater(Simulation simulation)
	{
		return MachineInventoryHelper.consumeFluid(inventory.getFluidInputs(), Fluids.WATER, 50, simulation) == 50;
	}
	
	@SuppressWarnings("deprecation")
	private boolean till(FarmerBlocks dirtBlocks, FarmerBlocks cropBlocks)
	{
		if(!tilling)
		{
			return false;
		}
		
		for(FarmerBlock entry : dirtBlocks)
		{
			BlockPos pos = entry.pos();
			BlockState state = entry.state();
			if(state.is(BlockTags.DIRT))
			{
				BlockState newState = Blocks.FARMLAND.defaultBlockState();
				if(Blocks.FARMLAND.canSurvive(newState, level, pos))
				{
					level.setBlock(pos, newState, 1 | 2 | 8);
					level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean wetten(FarmerBlocks dirtBlocks, FarmerBlocks cropBlocks)
	{
		if(this.consumeWater(Simulation.SIMULATE))
		{
			for(FarmerBlock entry : dirtBlocks)
			{
				BlockPos pos = entry.pos();
				BlockState state = entry.state();
				if(state.getBlock() instanceof FarmBlock)
				{
					int moisture = state.getValue(FarmBlock.MOISTURE);
					if(moisture < 7 && this.consumeWater(Simulation.ACT))
					{
						level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, 7), 2);
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean fertilize(FarmerBlocks dirtBlocks, FarmerBlocks cropBlocks)
	{
		// TODO
		return false;
	}
	
	private boolean harvest(FarmerBlocks dirtBlocks, FarmerBlocks cropBlocks)
	{
		// TODO
		return false;
	}
	
	private boolean plant(FarmerBlocks dirtBlocks, FarmerBlocks cropBlocks)
	{
		// TODO
		return false;
	}
	
	public void run()
	{
		if(level == null)
		{
			return;
		}
		
		FarmerBlocks dirtBlocks = new FarmerBlocks();
		FarmerBlocks cropBlocks = new FarmerBlocks();
		for(BlockPos pos : dirtPositions)
		{
			dirtBlocks.put(pos);
			cropBlocks.put(pos.above());
		}
		dirtBlocks.shuffle();
		cropBlocks.shuffle();
		
		this.till(dirtBlocks, cropBlocks);
		this.wetten(dirtBlocks, cropBlocks);
		this.fertilize(dirtBlocks, cropBlocks);
		this.harvest(dirtBlocks, cropBlocks);
		this.plant(dirtBlocks, cropBlocks);
	}
	
	@Override
	public void on(FarmlandLoseMoistureEvent event)
	{
		if(isActive.isActive && this.consumeWater(Simulation.SIMULATE))
		{
			this.consumeWater(Simulation.ACT);
			event.setCanceled(true);
		}
	}
	
	public void registerListeners(Level level, ShapeMatcher shapeMatcher)
	{
		this.level = level;
		this.shapeMatcher = shapeMatcher;
		IsolatedListeners.register(level, shapeMatcher.getSpannedChunks(), FarmlandLoseMoistureEvent.class, this);
	}
	
	public void unregisterListeners(Level level, ShapeMatcher shapeMatcher)
	{
		IsolatedListeners.unregister(level, shapeMatcher.getSpannedChunks(), FarmlandLoseMoistureEvent.class, this);
		this.shapeMatcher = null;
	}
	
	@Override
	public void writeNbt(CompoundTag tag)
	{
		tag.putBoolean("tilling", tilling);
	}
	
	@Override
	public void readNbt(CompoundTag tag, boolean isUpgradingMachine)
	{
		tilling = tag.getBoolean("tilling");
	}
	
	private final class FarmerBlocks extends ArrayList<FarmerBlock>
	{
		public void put(BlockPos pos)
		{
			this.add(new FarmerBlock(pos, level.getBlockState(pos)));
		}
		
		public void shuffle()
		{
			Collections.shuffle(this);
		}
	}
	
	private record FarmerBlock(BlockPos pos, BlockState state)
	{
	}
}
