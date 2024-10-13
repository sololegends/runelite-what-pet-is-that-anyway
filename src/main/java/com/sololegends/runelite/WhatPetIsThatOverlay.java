package com.sololegends.runelite;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

public class WhatPetIsThatOverlay extends Overlay {
	private Pattern PETS_MATCHER = Pattern.compile("<img=([0-9]+)>", Pattern.MULTILINE);

	private final Client client;
	private final WhatPetIsThatPlugin plugin;
	private final WhatPetIsThatConfig config;

	@Inject
	private TooltipManager tooltip_manager;

	@Inject
	private WhatPetIsThatOverlay(Client client, WhatPetIsThatPlugin plugin, WhatPetIsThatConfig config) {
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(Overlay.PRIORITY_MED);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	private void petsOverlay(Graphics2D graphics) {
		// Still loading icon data
		if (plugin.pets_icons_offset == -1) {
			return;
		}
		// Get the chat box lines
		Widget message_lines = client.getWidget(ComponentID.CHATBOX_MESSAGE_LINES);
		for (Widget line : message_lines.getChildren()) {
			// ensure visible, text is not null, and matching pets text
			if (message_lines.getBounds().contains(line.getBounds())
					&& line.getText() != null
					&& line.getText().matches(".*?Pets: \\([0-9]+\\) <img=[0-9]+>.*")) {
				// Get the bounds
				Rectangle bounds = line.getBounds();
				// Get mouse position
				Point mouse_pos = client.getMouseCanvasPosition();
				// If mouse in message bounds
				if (bounds.contains(mouse_pos.getX(), mouse_pos.getY())) {
					// Extract the pets list by icon ids
					Matcher matcher = PETS_MATCHER.matcher(line.getText());
					List<Integer> pet_icon_ids = new ArrayList<>();
					while (matcher.find()) {
						for (int i = 1; i <= matcher.groupCount(); i++) {
							pet_icon_ids.add(Integer.parseInt(matcher.group(i)));
						}
					}

					// Is it double height with many pets?
					int pets_per_line = pet_icon_ids.size();
					if (bounds.height > config.petsIconHeight()) {
						pets_per_line = (bounds.width - config.petsTextOffset()) / config.petsIconWidth();
					}

					// Get the hovered pet by the mouse position within chat line bounds
					int icon_pos = Math.floorDiv((int) (mouse_pos.getX() - (config.petsTextOffset() + bounds.getX())),
							config.petsIconWidth());
					if (mouse_pos.getY() >= bounds.getY() + config.petsIconHeight()) {
						icon_pos = Math.floorDiv((int) (mouse_pos.getX() - bounds.getX()),
								config.petsIconWidth()) + pets_per_line;
					}
					// If in bounds of a known icon position
					if (icon_pos >= 0 && icon_pos < pet_icon_ids.size() && icon_pos <= pets_per_line) {
						int pet_icon = pet_icon_ids.get(icon_pos);
						// Bounds verification!
						if (pet_icon - plugin.pets_icons_offset >= 0 && pet_icon - plugin.pets_icons_offset < plugin.pets.length) {
							String name = client.getItemDefinition(plugin.pets[pet_icon - plugin.pets_icons_offset]).getName();
							// Draw a tool tip with the pet's name
							tooltip_manager.add(new Tooltip("Pet: " + name));
						}
					}
				}
			}
		}
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		petsOverlay(graphics);
		return null;
	}

}
