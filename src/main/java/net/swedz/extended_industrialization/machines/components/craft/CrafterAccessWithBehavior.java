package net.swedz.extended_industrialization.machines.components.craft;

import aztech.modern_industrialization.api.machine.component.CrafterAccess;

public interface CrafterAccessWithBehavior extends CrafterAccess
{
	CrafterAccessBehavior getBehavior();
}
