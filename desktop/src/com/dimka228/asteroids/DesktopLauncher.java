package com.dimka228.asteroids;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.dimka228.asteroids.Game;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Game game = Game.getInstance();
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle(game.TITLE);
		config.setWindowedMode(Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
		new Lwjgl3Application(game, config);
	}
}
