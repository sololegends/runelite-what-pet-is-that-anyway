package com.sololegends.runelite;

import net.runelite.client.config.*;

@ConfigGroup("What Pet is That")
public interface WhatPetIsThatConfig extends Config {

	//@formatter:off
	@ConfigSection(
		name = "Debugging", 
		description = "Debugging Options", 
		position = 0,
		closedByDefault = false
	)

	String debug_section = "debugging";

	@ConfigItem(position = 1, 
		section = debug_section, 
		keyName = "wpita_pets_text_offset", 
		name = "Test Offset", 
		description = "Set the text offset in pixels form the 'Pets (n)'"
	)
	default int petsTextOffset() {
		return 48;
	}

	@ConfigItem(position = 2, 
		section = debug_section, 
		keyName = "wpita_pets_icon_width", 
		name = "Icon Width", 
		description = "Set the icon width in pixels for each pet"
	)
	default int petsIconWidth() {
		return 21;
	}

	@ConfigItem(position = 3, 
		section = debug_section, 
		keyName = "wpita_pets_icon_height", 
		name = "Icon Height", 
		description = "Set the icon height in pixels for each pet"
	)
	default int petsIconHeight() {
		return 14;
	}
}
