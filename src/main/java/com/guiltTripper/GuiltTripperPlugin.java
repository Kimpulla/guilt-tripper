package com.guiltTripper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.sound.sampled.*;
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

	private GameState lastGameState = null;
	private boolean isFirstLogin = true;

	@Inject
	private ClientThread clientThread;

	@Inject
	private GuiltTripperConfig config;

	private final String[] messages = GuiltTripperMessages.MESSAGES;
	private final Random random = new Random();
	private Timer timer;
	private Clip clip = null;
	private boolean isPlaying = false;

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
		System.out.println("Current game state: " + gameStateChanged.getGameState());
		System.out.println("Last game state: " + lastGameState);

		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			if (lastGameState == GameState.LOADING && isFirstLogin) {
				printDefaultMessages("Take the following message with a grain of salt...");
				startTimer();
				isFirstLogin = false; // set to false after first login
			} else if (lastGameState != GameState.LOADING) {
				isFirstLogin = true;
			}
		}
		lastGameState = gameStateChanged.getGameState();
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

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged) {
		if (client.getGameState() == GameState.LOGGED_IN
				&& client.getLocalPlayer() != null
				&& client.getLocalPlayer().getHealthRatio() == 0
				&& client.getLocalPlayer().getAnimation() == AnimationID.DEATH) {
			clientThread.invokeLater(() -> {
				if (config.allowSound()) {
					playDeathSound();
				}
			});
		}
	}

	private void printRandomMessage() {
		String randomMessage = "<col=008000>" + messages[random.nextInt(messages.length)] + "</col>";
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

	private void playDeathSound() {
		if (isPlaying) {
			System.out.println("Already playing");
			return;
		}

		System.out.println("Playing death sound");

		if (clip != null) {
			clip.close();
		}

		try {
			URL soundUrl = getClass().getResource("/deathsound.wav");
			if (soundUrl == null) {
				System.out.println("Death sound file not found!");
				return;
			}
			try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(soundUrl)) {
				clip = AudioSystem.getClip();
				clip.open(inputStream);
				setVolume(clip, config.soundVolume());
				clip.start();
				isPlaying = true;
				clip.addLineListener(e -> {
					if (e.getType() == LineEvent.Type.STOP) {
						isPlaying = false;
						System.out.println("Done playing death sound");
					}
				});
			}
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.out.println("Unable to play death sound: " + e);
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
