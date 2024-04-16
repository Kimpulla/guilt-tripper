package com.guiltTripper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "guiltTripper"
)
public class Plugin extends net.runelite.client.plugins.Plugin
{
	@Inject
	private Client client;

	@Inject
	private Config config;

	@Override
	protected void startUp() throws Exception
	{
		//log.info("guiltTripper started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		//log.info("guiltTripper stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "guiltTripper says " + config.greeting(), null);
		}
	}

	@Provides
	Config provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(Config.class);
	}
}
