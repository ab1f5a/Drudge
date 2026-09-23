package com.ab1f5a.drudge.module;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.Feature;
import com.ab1f5a.drudge.event.EventManager;
import com.ab1f5a.drudge.events.UpdateListener;
import com.ab1f5a.drudge.features.*;

public final class ModuleList implements UpdateListener
{
	public final AutoToolSwitch autoToolSwitch = new AutoToolSwitch();
	public final BreakFlow breakFlow = new BreakFlow();

	public final TreeFeller treeFeller = new TreeFeller();
	public final TunnelBorer tunnelBorer = new TunnelBorer();
	public final AreaExcavator areaExcavator = new AreaExcavator();

	private final TreeMap<String, Feature> modules =
		new TreeMap<>(String::compareToIgnoreCase);

	private final EnabledModulesFile enabledModulesFile;

	private final EventManager eventManager =
		DrudgeClient.INSTANCE.getEventManager();

	private boolean loaded;

	public ModuleList(Path enabledModulesFile)
	{
		this.enabledModulesFile = new EnabledModulesFile(enabledModulesFile);

		try
		{
			for(Field field : ModuleList.class.getDeclaredFields())
			{
				if(!Feature.class.isAssignableFrom(field.getType()))
					continue;

				Feature module = (Feature)field.get(this);
				modules.put(module.getName(), module);
			}

		}catch(Exception e)
		{
			String message = "Initializing Drudge features";
			CrashReport report = CrashReport.forThrowable(e, message);
			throw new ReportedException(report);
		}

		eventManager.add(UpdateListener.class, this);
	}

	@Override
	public void onUpdate()
	{
		enabledModulesFile.load(this);
		loaded = true;
		eventManager.remove(UpdateListener.class, this);
	}

	public void saveEnabledModules()
	{
		if(!loaded)
			return;

		enabledModulesFile.save(this);
	}

	public Feature getModuleByName(String name)
	{
		return modules.get(name);
	}

	public Collection<Feature> getAllModules()
	{
		return Collections.unmodifiableCollection(modules.values());
	}
}
