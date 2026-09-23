package com.ab1f5a.drudge.mixin;

import org.lwjgl.sdl.SDLMouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
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
		int code = key.getValue();

		if(key.getType() == InputConstants.Type.MOUSE)
			return (SDLMouse.SDL_GetMouseState(null, null)
				& buttonMask(code)) != 0;

		return InputConstants.isKeyDown(code);
	}

	/**
	 * This version reads the mouse through SDL instead of GLFW. The bound code
	 * is one of the {@code InputConstants.MOUSE_BUTTON_*} numbers, which are
	 * SDL's 1-based button numbers, so the matching bit is one below it.
	 */
	@Unique
	private static int buttonMask(int button)
	{
		return 1 << (Math.max(button, 1) - 1);
	}

	@Shadow
	public abstract void setDown(boolean pressed);
}
