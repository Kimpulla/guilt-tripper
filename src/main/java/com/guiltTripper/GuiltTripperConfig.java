package com.guiltTripper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("example")
public interface GuiltTripperConfig extends Config {

	@ConfigItem(
			keyName = "allowSound",
			name = "Allow Sound",
			description = "Enable or disable sound effects"
	)
	default boolean allowSound() {
		return false;
	}

	@ConfigItem(
			keyName = "soundVolume",
			name = "Sound Volume",
			description = "Adjust the volume of the sound effects",
			position = 1
	)
	@Range(
			min = 0,
			max = 100
	)
	default int soundVolume() {
		return 50; // By default, set to 50%
	}
}
