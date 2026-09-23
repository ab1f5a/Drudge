package com.ab1f5a.drudge.module;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.google.gson.JsonArray;

import com.ab1f5a.drudge.Feature;
import com.ab1f5a.drudge.util.json.JsonException;
import com.ab1f5a.drudge.util.json.JsonUtils;
import com.ab1f5a.drudge.util.json.WsonArray;

public final class EnabledModulesFile
{
	private final Path path;
	private boolean disableSaving;
	
	public EnabledModulesFile(Path path)
	{
		this.path = path;
	}
	
	public void load(ModuleList moduleList)
	{
		try
		{
			WsonArray wson = JsonUtils.parseFileToArray(path);
			enableModules(moduleList, wson);
			
		}catch(NoSuchFileException e)
		{
			
		}catch(IOException | JsonException e)
		{
			System.out.println("Couldn't load " + path.getFileName());
			e.printStackTrace();
		}
		
		save(moduleList);
	}
	
	private void enableModules(ModuleList modules, WsonArray wson)
	{
		try
		{
			disableSaving = true;
			
			for(Feature module : modules.getAllModules())
				module.setEnabled(false);
			
			for(String name : wson.getAllStrings())
			{
				Feature module = modules.getModuleByName(name);
				if(module == null || !module.isStateSaved())
					continue;
				
				module.setEnabled(true);
			}
			
		}finally
		{
			disableSaving = false;
		}
	}
	
	public void save(ModuleList modules)
	{
		if(disableSaving)
			return;
		
		JsonArray json = createJson(modules);
		
		try
		{
			JsonUtils.toJson(json, path);
			
		}catch(IOException | JsonException e)
		{
			System.out.println("Couldn't save " + path.getFileName());
			e.printStackTrace();
		}
	}
	
	private JsonArray createJson(ModuleList modules)
	{
		Stream<Feature> enabledModules = modules.getAllModules().stream()
			.filter(Feature::isEnabled).filter(Feature::isStateSaved);
		
		JsonArray json = new JsonArray();
		enabledModules.map(Feature::getName).forEach(name -> json.add(name));
		
		return json;
	}
}
