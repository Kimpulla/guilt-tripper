package com.guiltTripper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.sound.sampled.FloatControl;


import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

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
	protected void startUp() throws Exception {
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
				if (client.getGameState() == GameState.LOGGED_IN) {
					clientThread.invokeLater(() -> {
						printRandomMessage();
						playSound();
					});
				}
			}
		}, 0, 10 * 1000); // 10 seconds
	}

	@Override
	protected void shutDown() throws Exception {
		System.out.println("Guilt Tripper stopped!");
		if (timer != null) {
			timer.cancel();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			printRandomMessage();
			startTimer();
		}
	}

	private void printRandomMessage() {
		String randomMessage = "<col=ff0000>" + messages[random.nextInt(messages.length)] + "</col>";
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", randomMessage, null);
	}

	private Clip clip = null;

	// TODO: LISÄÄ ÄÄNIÄ
	private void playSound() {
		if (!config.allowSound()) {  // Tarkista, ovatko äänet sallittuja
			return;  // Jos ei, lopeta metodi tässä
		}

		if (clip != null) {
			clip.close();
			clip = null;  // Aseta clip nulliksi suljetun klipin jälkeen
		}
		try {
			// Ladataan ääni resurssina
			URL soundUrl = getClass().getResource("/laughter.wav");
			if (soundUrl == null) {
				System.out.println("Sound file not found!");
				return;
			}
			try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl)) {
				clip = AudioSystem.getClip();
				clip.open(audioIn);
				setVolume(clip, config.soundVolume()); // Käytä konfiguraation äänenvoimakkuutta
				clip.start();
			}
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.out.println("Error playing sound:" + e.getMessage());
		}
	}


	// Apumetodi äänenvoimakkuuden asettamiseksi
	private void setVolume(Clip clip, int volume) {
		if (volume < 0 || volume > 100) {
			throw new IllegalArgumentException("Volume must be between 0 and 100");
		}

		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float minDb = gainControl.getMinimum(); // esim. -80 dB tai vastaava
		float maxDb = gainControl.getMaximum(); // +6.0206 dB

		// Lasketaan dB arvo lineaarisesti volume prosentin perusteella
		float gain = ((maxDb - minDb) * (volume / 100.0f)) + minDb;
		gainControl.setValue(gain);

		System.out.println("Volume set to " + volume + "% (" + gain + " dB)");
	}








	@Provides
	GuiltTripperConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(GuiltTripperConfig.class);
	}
}
