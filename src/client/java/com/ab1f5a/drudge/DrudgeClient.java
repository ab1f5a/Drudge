package com.ab1f5a.drudge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.client.Minecraft;

import com.ab1f5a.drudge.event.EventManager;
import com.ab1f5a.drudge.events.PostMotionListener;
import com.ab1f5a.drudge.events.PreMotionListener;
import com.ab1f5a.drudge.keybinds.DrudgeKeybinds;
import com.ab1f5a.drudge.module.ModuleList;
import com.ab1f5a.drudge.module.SettingsFile;
import com.ab1f5a.drudge.mixinterface.IMinecraftClient;

public enum DrudgeClient
{
	INSTANCE;

	public static final String VERSION = "1.0.0";
	public static final String MC_VERSION = "26.1.1";

	public static Minecraft MC;
	public static IMinecraftClient IMC;

	private EventManager eventManager;
	private ModuleList modules;
	private SettingsFile settingsFile;
	private RotationFaker rotationFaker;
	private Path drudgeFolder;

	private boolean enabled = true;

	void initialize()
	{
		MC = Minecraft.getInstance();
		IMC = (IMinecraftClient)MC;
		drudgeFolder = createDrudgeFolder();

		eventManager = new EventManager(this);

		modules = new ModuleList(drudgeFolder.resolve("enabled-features.json"));
		settingsFile = new SettingsFile(drudgeFolder.resolve("settings.json"));
		settingsFile.load(modules);

		new DrudgeKeybinds(modules);

		rotationFaker = new RotationFaker();
		eventManager.add(PreMotionListener.class, rotationFaker);
		eventManager.add(PostMotionListener.class, rotationFaker);
	}

	private Path createDrudgeFolder()
	{
		Path folder = MC.gameDirectory.toPath().normalize().resolve("drudge");

		try
		{
			Files.createDirectories(folder);

		}catch(IOException e)
		{
			throw new RuntimeException(
				"Couldn't create .minecraft/drudge folder.", e);
		}

		return folder;
	}

	public void saveSettings()
	{
		settingsFile.save(modules);
	}

	public EventManager getEventManager()
	{
		return eventManager;
	}

	public ModuleList getModules()
	{
		return modules;
	}

	public RotationFaker getRotationFaker()
	{
		return rotationFaker;
	}

	public Path getDrudgeFolder()
	{
		return drudgeFolder;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
