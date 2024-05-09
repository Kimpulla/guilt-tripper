package com.guiltTripper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;



@Slf4j
@PluginDescriptor(
		name = "Guilt Tripper"
)
public class GuiltTripperPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private GuiltTripperConfig config;

	private final String[] messages = GuiltTripperMessages.MESSAGES;
	private final Random random = new Random();
	private Timer timer;
	private Clip clip = null;

	@Override
	protected void startUp() throws Exception {
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
				if (client.getGameState() == GameState.LOGGED_IN) {
					clientThread.invokeLater(() -> {
						printRandomMessage();
						if (config.allowSound()) {
							playSound();
						}
					});
				}
			}
		}, 15 * 60 * 1000, 15 * 60 * 1000);
	}

	@Override
	protected void shutDown() throws Exception {
		if (timer != null) {
			timer.cancel();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			printDefaultMessages("Take the following message with a grain of salt...");
			startTimer();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event) {
		NPC npc = event.getNpc();
		int dropChance = BossDropRate.getDropRate(npc.getName());

		if (BossName.NPC_NAMES.contains(npc.getName()) && npc.getHealthRatio() == 0) {
			if (random.nextInt(dropChance) == 0) {
				printSpecificMessage("You have a funny feeling like you're being followed.");

				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						clientThread.invokeLater(() -> {
							printSpecificMessage("Turns out it was just a lost kitten.");
						});
					}
				}, 5000);
			}
		}
	}



	private void printRandomMessage() {
		String randomMessage = "<col=00ff00>" + messages[random.nextInt(messages.length)] + "</col>";
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", randomMessage, null);
	}

	private void printSpecificMessage(String message) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>" + message + "</col>", null);
	}

	private void printDefaultMessages(String message) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=000000>" + message + "</col>", null);
	}

	private void playSound() {
		if (!config.allowSound()) {
			return;
		}

		if (clip != null) {
			clip.close();
			clip = null;
		}

		String[] sounds = {"laughter.wav", "ba-dum-tishh.wav", "oldman.wav", "ooouuh.wav", "sneeze.wav", "trumpet.wav"};
		int index = random.nextInt(sounds.length);

		try {
			URL soundUrl = getClass().getResource("/" + sounds[index]);
			if (soundUrl == null) {
				System.out.println("Sound file not found!");
				return;
			}
			try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl)) {
				clip = AudioSystem.getClip();
				clip.open(audioIn);
				setVolume(clip, config.soundVolume());
				clip.start();
			}
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.out.println("Error playing sound:" + e.getMessage());
		}
	}

	private void setVolume(Clip clip, int volume) {
		if (volume < 0 || volume > 100) {
			throw new IllegalArgumentException("Volume must be between 0 and 100");
		}

		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float minDb = gainControl.getMinimum();
		float maxDb = gainControl.getMaximum();
		float gain = ((maxDb - minDb) * (volume / 100.0f)) + minDb;
		gainControl.setValue(gain);
	}

	@Provides
	GuiltTripperConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(GuiltTripperConfig.class);
	}
}
