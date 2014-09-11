package com.jvj.yuggoth.desktop;

import com.badlogic.gdx.backends.lwjgl.*;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.jvj.yuggoth.Yuggoth;

public class DesktopLauncher {
	
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//com.badlogic.gdx.backends.lwjgl.
		new LwjglApplication(new Yuggoth(), config);
	}
}
