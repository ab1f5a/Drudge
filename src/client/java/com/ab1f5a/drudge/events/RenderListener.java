package com.ab1f5a.drudge.events;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.PoseStack;

import com.ab1f5a.drudge.event.Event;
import com.ab1f5a.drudge.event.Listener;

public interface RenderListener extends Listener
{
	public void onRender(PoseStack matrixStack, float partialTicks);
	
	public static class RenderEvent extends Event<RenderListener>
	{
		private final PoseStack matrixStack;
		private final float partialTicks;
		
		public RenderEvent(PoseStack matrixStack, float partialTicks)
		{
			this.matrixStack = matrixStack;
			this.partialTicks = partialTicks;
		}
		
		@Override
		public void fire(ArrayList<RenderListener> listeners)
		{
			for(RenderListener listener : listeners)
				listener.onRender(matrixStack, partialTicks);
		}
		
		@Override
		public Class<RenderListener> getListenerType()
		{
			return RenderListener.class;
		}
	}
}
