package com.jvj.yuggoth.desktop;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.*;
import com.jvj.yuggoth.Yuggoth;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		
		//com.badlogic.gdx.backends.lwjgl.
		LwjglApplication app = new LwjglApplication(new Yuggoth(), config);

	}
}
