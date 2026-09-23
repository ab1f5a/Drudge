package com.ab1f5a.drudge.events;

import java.util.ArrayList;

import com.ab1f5a.drudge.event.Event;
import com.ab1f5a.drudge.event.Listener;

public interface PostMotionListener extends Listener
{
	public void onPostMotion();
	
	public static class PostMotionEvent extends Event<PostMotionListener>
	{
		public static final PostMotionEvent INSTANCE = new PostMotionEvent();
		
		@Override
		public void fire(ArrayList<PostMotionListener> listeners)
		{
			for(PostMotionListener listener : listeners)
				listener.onPostMotion();
		}
		
		@Override
		public Class<PostMotionListener> getListenerType()
		{
			return PostMotionListener.class;
		}
	}
}
