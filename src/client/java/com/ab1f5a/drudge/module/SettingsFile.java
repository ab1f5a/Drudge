package com.ab1f5a.drudge.module;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.ab1f5a.drudge.Feature;
import com.ab1f5a.drudge.settings.Setting;
import com.ab1f5a.drudge.util.json.JsonException;
import com.ab1f5a.drudge.util.json.JsonUtils;

public final class SettingsFile
{
	private final Path path;

	public SettingsFile(Path path)
	{
		this.path = path;
	}

	public void load(ModuleList moduleList)
	{
		JsonObject json;

		try
		{
			json = JsonUtils.parseFile(path).getAsJsonObject();

		}catch(NoSuchFileException e)
		{
			return;

		}catch(IOException | JsonException | IllegalStateException e)
		{
			System.err.println("[Drudge] Couldn't load settings: " + e);
			return;
		}

		for(Feature feature : moduleList.getAllModules())
		{
			JsonElement featureJson = json.get(feature.getName());
			if(featureJson == null || !featureJson.isJsonObject())
				continue;

			for(Entry<String, JsonElement> entry : featureJson
				.getAsJsonObject().entrySet())
			{
				Setting setting = feature.getSettings().get(entry.getKey());
				if(setting == null)
					continue;

				try
				{
					setting.fromJson(entry.getValue());

				}catch(RuntimeException e)
				{
					System.err.println("[Drudge] Couldn't load "
						+ feature.getName() + "'s " + entry.getKey()
						+ " setting: " + e);
				}
			}
		}
	}

	public void save(ModuleList moduleList)
	{
		JsonObject json = new JsonObject();

		for(Feature feature : moduleList.getAllModules())
		{
			JsonObject featureJson = new JsonObject();

			for(Entry<String, Setting> entry : feature.getSettings()
				.entrySet())
				featureJson.add(entry.getKey(), entry.getValue().toJson());

			json.add(feature.getName(), featureJson);
		}

		try
		{
			JsonUtils.toJson(json, path);

		}catch(IOException | JsonException e)
		{
			System.err.println("[Drudge] Couldn't save settings: " + e);
		}
	}
}
