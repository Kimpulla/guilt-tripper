package com.guiltTripper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface GuiltTripperConfig extends Config
{
	@ConfigItem(
			keyName = "greeting",
			name = "Welcome Greeting",
			description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "You again!?";
	}
}