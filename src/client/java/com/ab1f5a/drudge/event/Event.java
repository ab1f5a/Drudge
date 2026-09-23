package com.ab1f5a.drudge.event;

import java.util.ArrayList;

public abstract class Event<T extends Listener>
{
	public abstract void fire(ArrayList<T> listeners);
	
	public abstract Class<T> getListenerType();
}
