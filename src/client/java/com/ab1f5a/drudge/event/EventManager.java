package com.ab1f5a.drudge.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import com.ab1f5a.drudge.DrudgeClient;

public final class EventManager
{
	private final DrudgeClient client;
	private final HashMap<Class<? extends Listener>, ArrayList<? extends Listener>> listenerMap =
		new HashMap<>();
	
	public EventManager(DrudgeClient client)
	{
		this.client = client;
	}
	
	public static <L extends Listener, E extends Event<L>> void fire(E event)
	{
		EventManager eventManager = DrudgeClient.INSTANCE.getEventManager();
		if(eventManager == null)
			return;
		
		eventManager.fireImpl(event);
	}
	
	private <L extends Listener, E extends Event<L>> void fireImpl(E event)
	{
		if(!client.isEnabled())
			return;
		
		try
		{
			Class<L> type = event.getListenerType();
			@SuppressWarnings("unchecked")
			ArrayList<L> listeners = (ArrayList<L>)listenerMap.get(type);
			
			if(listeners == null || listeners.isEmpty())
				return;
				
			ArrayList<L> listeners2 = new ArrayList<>(listeners);
			
			listeners2.removeIf(Objects::isNull);
			
			event.fire(listeners2);
			
		}catch(Throwable e)
		{
			e.printStackTrace();
			
			CrashReport report =
				CrashReport.forThrowable(e, "Firing Drudge event");
			CrashReportCategory section = report.addCategory("Affected event");
			section.setDetail("Event class", () -> event.getClass().getName());
			
			throw new ReportedException(report);
		}
	}
	
	public <L extends Listener> void add(Class<L> type, L listener)
	{
		try
		{
			@SuppressWarnings("unchecked")
			ArrayList<L> listeners = (ArrayList<L>)listenerMap.get(type);
			
			if(listeners == null)
			{
				listeners = new ArrayList<>(Arrays.asList(listener));
				listenerMap.put(type, listeners);
				return;
			}
			
			listeners.add(listener);
			
		}catch(Throwable e)
		{
			e.printStackTrace();
			
			CrashReport report =
				CrashReport.forThrowable(e, "Adding Drudge event listener");
			CrashReportCategory section =
				report.addCategory("Affected listener");
			section.setDetail("Listener type", () -> type.getName());
			section.setDetail("Listener class",
				() -> listener.getClass().getName());
			
			throw new ReportedException(report);
		}
	}
	
	public <L extends Listener> void remove(Class<L> type, L listener)
	{
		try
		{
			@SuppressWarnings("unchecked")
			ArrayList<L> listeners = (ArrayList<L>)listenerMap.get(type);
			
			if(listeners != null)
				listeners.remove(listener);
			
		}catch(Throwable e)
		{
			e.printStackTrace();
			
			CrashReport report =
				CrashReport.forThrowable(e, "Removing Drudge event listener");
			CrashReportCategory section =
				report.addCategory("Affected listener");
			section.setDetail("Listener type", () -> type.getName());
			section.setDetail("Listener class",
				() -> listener.getClass().getName());
			
			throw new ReportedException(report);
		}
	}
}
