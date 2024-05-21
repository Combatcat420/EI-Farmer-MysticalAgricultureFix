package net.swedz.extended_industrialization.machines.guicomponents.solarefficiency;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.util.RenderHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.swedz.extended_industrialization.EI;
import net.swedz.extended_industrialization.text.EIText;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Optional;

public final class SolarEfficiencyBarClient implements GuiComponentClient
{
	public final SolarEfficiencyBar.Parameters params;
	
	public boolean working;
	public int     efficiency;
	public int     calcification;
	
	public SolarEfficiencyBarClient(FriendlyByteBuf buf)
	{
		this.params = new SolarEfficiencyBar.Parameters(buf.readInt(), buf.readInt());
		this.readCurrentData(buf);
	}
	
	@Override
	public void readCurrentData(FriendlyByteBuf buf)
	{
		working = buf.readBoolean();
		efficiency = buf.readInt();
		calcification = buf.readInt();
	}
	
	@Override
	public ClientComponentRenderer createRenderer(MachineScreen machineScreen)
	{
		return new Renderer();
	}
	
	private final class Renderer implements ClientComponentRenderer
	{
		private final MIIdentifier     TEXTURE     = new MIIdentifier("textures/gui/efficiency_bar.png");
		private final ResourceLocation SOLAR_STATE = EI.id("textures/gui/container/solar_state.png");
		
		private final int WIDTH = 100, HEIGHT = 2;
		
		@Override
		public void renderBackground(GuiGraphics guiGraphics, int x, int y)
		{
			guiGraphics.blit(TEXTURE,
					x + params.renderX - 1, y + params.renderY - 1,
					0, 2, WIDTH + 2, HEIGHT + 2, 102, 6
			);
			int barPixels = (int) ((float) efficiency / 100 * WIDTH);
			guiGraphics.blit(TEXTURE,
					x + params.renderX, y + params.renderY,
					0, 0, barPixels, HEIGHT, 102, 6
			);
			guiGraphics.blit(SOLAR_STATE,
					x + params.renderX - 20, y + params.renderY + HEIGHT / 2 - 6,
					working ? 0 : 12, 0, 12, 12, 24, 12
			);
		}
		
		@Override
		public void renderTooltip(MachineScreen screen, Font font, GuiGraphics guiGraphics, int x, int y, int cursorX, int cursorY)
		{
			if(RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT, cursorX - x, cursorY - y))
			{
				List<Component> lines = Lists.newArrayList();
				lines.add(EIText.SOLAR_EFFICIENCY.text(efficiency));
				if(calcification >= 0)
				{
					lines.add(EIText.CALCIFICATION_PERCENTAGE.text(calcification));
				}
				guiGraphics.renderTooltip(font, lines, Optional.empty(), cursorX, cursorY);
			}
		}
	}
}
