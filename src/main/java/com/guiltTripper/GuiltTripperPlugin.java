package com.guiltTripper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@PluginDescriptor(
		name = "Guilt Tripper"
)
public class GuiltTripperPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private GuiltTripperConfig config;

	private final String[] messages = {
			"Touch grass!",
			"Go level up in daylight skills!",
			"Have you tried the breathing skill outside?",
			"Your chair called. It wants a relationship break.",
			"Maybe it’s time to grind some real-life quests?",
			"Even a goblin has better social skills!",
			"I bet even a rock crab walks more than you.",
			"Time to logout and log into reality!",
			"Have you considered leveling up offline?",
			"Get some sunlight, you're not a cave slime!",
			"Even your pet's on a more balanced diet. Time for a walk?"
	};

	private final Random random = new Random();
	private Timer timer;

	@Override
	protected void startUp() throws Exception
	{
		System.out.println("Guilt Tripper started!");
		startTimer();
	}

	private void startTimer() {
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (client.getGameState() == net.runelite.api.GameState.LOGGED_IN) {
					clientThread.invokeLater(() -> printRandomMessage());
				}
			}
		}, 0, 10 * 1000); // 10 seconds
	}

	@Override
	protected void shutDown() throws Exception
	{
		System.out.println("Guilt Tripper stopped!");
		if (timer != null) {
			timer.cancel();
		}
	}

	@Subscribe
	public void onGameStateChanged(net.runelite.api.events.GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == net.runelite.api.GameState.LOGGED_IN) {
			printRandomMessage();
			startTimer();
		}
	}

	private void printRandomMessage() {
		String randomMessage = "<col=ff0000>" + messages[random.nextInt(messages.length)] + "</col>";
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", randomMessage, null);
	}

	@Provides
	GuiltTripperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GuiltTripperConfig.class);
	}
}
