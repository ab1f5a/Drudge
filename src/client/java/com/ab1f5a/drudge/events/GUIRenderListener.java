package com.ab1f5a.drudge.events;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiGraphics;
import com.ab1f5a.drudge.event.Event;
import com.ab1f5a.drudge.event.Listener;

public interface GUIRenderListener extends Listener
{
	public void onRenderGUI(GuiGraphics context, float partialTicks);
	
	public static class GUIRenderEvent extends Event<GUIRenderListener>
	{
		private final float partialTicks;
		private final GuiGraphics context;
		
		public GUIRenderEvent(GuiGraphics context, float partialTicks)
		{
			this.context = context;
			this.partialTicks = partialTicks;
		}
		
		@Override
		public void fire(ArrayList<GUIRenderListener> listeners)
		{
			for(GUIRenderListener listener : listeners)
				listener.onRenderGUI(context, partialTicks);
		}
		
		@Override
		public Class<GUIRenderListener> getListenerType()
		{
			return GUIRenderListener.class;
		}
	}
}
