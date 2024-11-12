package com.sololegends.runelite;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.google.inject.Inject;
import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.*;
import net.runelite.client.plugins.chatcommands.ChatCommandsPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(name = "What Pet is that Anyway", description = "Shows the pet name on hover from the !pets command", tags = {
		"pets", "pet", "identify", "command", "help", "icon" })
@PluginDependency(ChatCommandsPlugin.class)
public class WhatPetIsThatPlugin extends Plugin {

	public int pets_icons_offset = -1;
	public int[] pets;

	@Inject
	private Client client;

	@Inject
	private WhatPetIsThatOverlay lab_inv_overlay;

	@Inject
	private OverlayManager overlay_manager;

	@Inject
	private ItemManager item_manager;

	@Override
	protected void startUp() throws Exception {
		log.info("Starting What's that pet anyway");
		overlay_manager.add(lab_inv_overlay);
	}

	@Override
	protected void shutDown() throws Exception {
		log.info("Stopping What's that pet anyway!");
		overlay_manager.remove(lab_inv_overlay);
	}

	public void startup() {
		// I hate everything about this code block..
		// but I'm not sure how else to acquire the pet icon offset without this horrid
		// operation. At least it is only run once..
		EnumComposition petsEnum = client.getEnum(EnumID.PETS);
		pets = new int[petsEnum.size()];
		for (int i = 0; i < petsEnum.size(); ++i) {
			pets[i] = petsEnum.getIntValue(i);
		}
		final int pet_id = petsEnum.getIntValue(0);
		final AsyncBufferedImage abi = item_manager.getImage(pet_id);
		abi.onLoaded(() -> {
			final BufferedImage image = ImageUtil.resizeImage(abi, 18, 16);
			final IndexedSprite sprite = ImageUtil.getImageIndexedSprite(image, client);
			IndexedSprite[] sprites = client.getModIcons();
			for (int i = 0; i < sprites.length; i++) {
				if (Arrays.equals(sprites[i].getPixels(), sprite.getPixels())) {
					pets_icons_offset = i;
					break;
				}
			}
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		switch (event.getGameState()) {
			case STARTING:
				pets_icons_offset = -1;
				pets = null;
				break;
			case LOGGED_IN:
				if (pets_icons_offset == -1) {
					startup();
				}
				break;
			case CONNECTION_LOST:
			case HOPPING:
			case LOADING:
			case LOGGING_IN:
			case LOGIN_SCREEN:
			case LOGIN_SCREEN_AUTHENTICATOR:
			case UNKNOWN:
			default:
				break;
		}
	}

	@Provides
	WhatPetIsThatConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(WhatPetIsThatConfig.class);
	}
}
