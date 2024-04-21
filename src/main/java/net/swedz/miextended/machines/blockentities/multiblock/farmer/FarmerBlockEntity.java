package net.swedz.miextended.machines.blockentities.multiblock.farmer;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.swedz.miextended.machines.components.farmer.FarmerComponent;
import net.swedz.miextended.machines.components.farmer.PlantingMode;
import net.swedz.miextended.machines.multiblock.BasicMultiblockMachineBlockEntity;
import net.swedz.miextended.machines.multiblock.members.PredicateSimpleMember;
import net.swedz.miextended.text.MIEText;
import org.apache.commons.compress.utils.Lists;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class FarmerBlockEntity extends BasicMultiblockMachineBlockEntity
{
	private static final int         SHAPE_RADIUS_START            = 3;
	private static final int         SHAPE_RADIUS_LEVEL_MULTIPLIER = 2;
	private static final Component[] SHAPE_TRANSLATIONS            = new Component[]
			{
					MIText.ShapeTextSmall.text(),
					MIText.ShapeTextMedium.text(),
					MIText.ShapeTextLarge.text(),
					MIText.ShapeTextExtreme.text()
			};
	
	protected final ShapeWrapper shapes;
	
	protected final long euCost;
	
	protected final FarmerComponent farmer;
	
	public FarmerBlockEntity(BEP bep, String blockId, long euCost, PlantingMode defaultPlantingMode, boolean canChoosePlantingMode, int maxOperationsPerTask, ShapeWrapper shapes)
	{
		super(
				bep,
				new MachineGuiParameters.Builder(blockId, false).backgroundHeight(128).build(),
				shapes.shapeTemplates()
		);
		
		this.shapes = shapes;
		
		this.euCost = euCost;
		
		this.farmer = new FarmerComponent(inventory, isActive, defaultPlantingMode, maxOperationsPerTask);
		
		this.registerComponents(farmer);
		
		List<ShapeSelection.LineInfo> lines = Lists.newArrayList();
		List<Component> sizes = Lists.newArrayList();
		for(int i = 0; i < 4 && i < shapes.shapeTemplates().length; i++)
		{
			sizes.add(SHAPE_TRANSLATIONS[i]);
		}
		lines.add(new ShapeSelection.LineInfo(sizes.size(), sizes, true));
		lines.add(new ShapeSelection.LineInfo(
				2,
				List.of(MIEText.FARMER_NOT_TILLING.text(), MIEText.FARMER_TILLING.text()),
				true
		));
		if(canChoosePlantingMode)
		{
			lines.add(new ShapeSelection.LineInfo(
					PlantingMode.values().length,
					Stream.of(PlantingMode.values()).map(PlantingMode::textComponent).toList(),
					true
			));
		}
		this.registerGuiComponent(new ShapeSelection.Server(
				new ShapeSelection.Behavior()
				{
					@Override
					public void handleClick(int line, int delta)
					{
						if(line == 0)
						{
							activeShape.incrementShape(FarmerBlockEntity.this, delta);
						}
						else if(line == 1)
						{
							if(delta > 0)
							{
								farmer.tilling = true;
							}
							else if(delta < 0)
							{
								farmer.tilling = false;
							}
						}
						else if(line == 2)
						{
							int newIndex = farmer.plantingMode.ordinal() + delta;
							farmer.plantingMode = PlantingMode.values()[Mth.clamp(newIndex, 0, PlantingMode.values().length - 1)];
						}
					}
					
					@Override
					public int getCurrentIndex(int line)
					{
						if(line == 0)
						{
							return activeShape.getActiveShapeIndex();
						}
						else if(line == 1)
						{
							return farmer.tilling ? 1 : 0;
						}
						else if(line == 2)
						{
							return farmer.plantingMode.ordinal();
						}
						throw new IllegalStateException();
					}
				},
				lines.toArray(new ShapeSelection.LineInfo[0])
		));
	}
	
	public abstract long consumeEu(long max);
	
	@Override
	public void onLink(ShapeMatcher shapeMatcher)
	{
		farmer.registerListeners(level, shapeMatcher);
	}
	
	@Override
	public void onUnlink(ShapeMatcher shapeMatcher)
	{
		farmer.unregisterListeners(level, shapeMatcher);
	}
	
	@Override
	public void onSuccessfulMatch(ShapeMatcher shapeMatcher)
	{
		List<BlockPos> offsets = shapes.dirtPositions()[activeShape.getActiveShapeIndex()];
		farmer.fromOffsets(worldPosition, orientation.facingDirection, offsets);
		
		farmer.updateStackListeners();
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if(level.isClientSide)
		{
			return;
		}
		
		if(this.isShapeValid())
		{
			long eu = this.consumeEu(euCost);
			boolean active = eu > 0;
			this.updateActive(active);
			
			if(active)
			{
				farmer.tick();
			}
		}
		else
		{
			this.updateActive(false);
		}
	}
	
	private void updateActive(boolean active)
	{
		isActive.updateActive(active, this);
	}
	
	public static final class ShapeWrapper
	{
		public static ShapeWrapper of(int maxHeight)
		{
			return new ShapeWrapper(maxHeight);
		}
		
		private static final SimpleMember DIRT = new PredicateSimpleMember((state) -> state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND), Blocks.DIRT);
		
		private final ShapeTemplate[]  shapeTemplates;
		private final List<BlockPos>[] dirtPositions;
		
		private SimpleMember  casingBase;
		private SimpleMember  casingPipe;
		private MachineCasing hatch;
		
		private boolean electric;
		
		public ShapeWrapper(int maxHeight)
		{
			this.shapeTemplates = new ShapeTemplate[maxHeight];
			this.dirtPositions = new List[maxHeight];
		}
		
		public ShapeWrapper withCasing(SimpleMember base, SimpleMember pipe, MachineCasing hatch)
		{
			this.casingBase = base;
			this.casingPipe = pipe;
			this.hatch = hatch;
			return this;
		}
		
		public ShapeWrapper withElectric()
		{
			electric = true;
			return this;
		}
		
		public ShapeWrapper complete()
		{
			HatchFlags.Builder hatchFlagsBuilder = new HatchFlags.Builder();
			if(electric)
			{
				hatchFlagsBuilder.with(HatchType.ENERGY_INPUT);
			}
			HatchFlags hatchFlags = hatchFlagsBuilder.with(HatchType.ITEM_INPUT, HatchType.ITEM_OUTPUT, HatchType.FLUID_INPUT).build();
			
			for(int i = 0; i < shapeTemplates.length; i++)
			{
				ShapeTemplate.Builder builder = new ShapeTemplate.Builder(hatch);
				
				int height = i + 4;
				for(int y = 0; y > -height; y--)
				{
					boolean bottom = y == -height + 1;
					boolean top = y == 0;
					boolean topSecond = y == -1;
					boolean middle = !bottom && !top && !topSecond;
					builder.add3by3(y, middle ? casingPipe : casingBase, middle, middle ? hatchFlags : null);
					if(middle)
					{
						builder.add(0, y, 1, casingPipe);
					}
				}
				
				List<BlockPos> dirtBlocks = Lists.newArrayList();
				double maxDistance = (SHAPE_RADIUS_START + 1.5) + (i * SHAPE_RADIUS_LEVEL_MULTIPLIER);
				int maxDistanceRounded = (int) Math.ceil(maxDistance);
				int maxDistanceSquared = (int) Math.pow(maxDistance, 2);
				for(int x = -maxDistanceRounded; x <= maxDistanceRounded; x++)
				{
					for(int z = -maxDistanceRounded; z <= maxDistanceRounded; z++)
					{
						if(Math.abs(x) <= 1 && Math.abs(z) <= 1)
						{
							continue;
						}
						int distance = (int) (Math.pow(x, 2) + Math.pow(z, 2));
						if(distance < maxDistanceSquared)
						{
							builder.add(x, -1, z + 1, DIRT);
							dirtBlocks.add(new BlockPos(x, -1, z + 1));
						}
					}
				}
				
				shapeTemplates[i] = builder.build();
				dirtPositions[i] = Collections.unmodifiableList(dirtBlocks);
			}
			
			return this;
		}
		
		public ShapeTemplate[] shapeTemplates()
		{
			return shapeTemplates;
		}
		
		public List<BlockPos>[] dirtPositions()
		{
			return dirtPositions;
		}
	}
}
