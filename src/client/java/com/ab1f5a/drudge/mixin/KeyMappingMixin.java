package com.ab1f5a.drudge.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.ab1f5a.drudge.mixinterface.IKeyMapping;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements IKeyMapping
{
	@Shadow
	private InputConstants.Key key;

	@Override
	@Deprecated
	public void drudge_resetPressedState()
	{
		setDown(isActuallyDown());
	}

	@Unique
	private boolean isActuallyDown()
	{
		Window window = Minecraft.getInstance().getWindow();
		int code = key.getValue();

		if(key.getType() == InputConstants.Type.MOUSE)
			return GLFW.glfwGetMouseButton(window.handle(), code) == 1;

		return InputConstants.isKeyDown(window, code);
	}

	@Shadow
	public abstract void setDown(boolean pressed);
}
