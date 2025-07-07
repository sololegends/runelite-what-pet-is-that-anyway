package com.sololegends.runelite;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

public class WhatPetIsThatOverlay extends Overlay {

	private final Client client;
	private final WhatPetIsThatPlugin plugin;

	private static final int PETS_ICON_WIDTH = 21;
	private static final int PETS_ICON_HEIGHT = 14;

	@Inject
	private TooltipManager tooltip_manager;

	@Inject
	private WhatPetIsThatOverlay(Client client, WhatPetIsThatPlugin plugin) {
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(Overlay.PRIORITY_MED);
		this.client = client;
		this.plugin = plugin;
	}

	private boolean checkVisible(Widget parent, Widget line) {
		int child_s = line.getRelativeY() + line.getHeight();
		// Check if child's south most Y coord is within the base bounds of parent's
		// scrolled viewport
		return child_s > parent.getScrollY() && child_s < parent.getHeight() + parent.getScrollY();
	}

	private List<Integer> extractPetIconIds(String pets) {
		int limit = -Integer.MAX_VALUE / 10;
		List<Integer> pet_icons = new ArrayList<>();
		boolean reading = false;
		int int_cache = 0;
		for (int i = 0; i < pets.length(); i++) {
			char c = pets.charAt(i);
			// If at the start of presumably the numbers
			if (c == '=') {
				reading = true;
			}
			// End reference point
			else if (reading && c == '>') {
				reading = false;
				pet_icons.add(-int_cache);
				int_cache = 0;
			} else if (reading) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				int digit = Character.digit(c, 10);
				if (digit < 0 || int_cache < limit) {
					continue;
				}
				int_cache *= 10;
				if (int_cache < limit + digit) {
					continue;
				}
				int_cache -= digit;
			}
		}
		return pet_icons;
	}

	private void petsOverlay(Graphics2D graphics) {
		// Still loading icon data
		if (plugin.pets_icons_offset == -1) {
			return;
		}
		Point mouse_pos = client.getMouseCanvasPosition();
		// Get the chat box lines

		Widget message_lines = client.getWidget(InterfaceID.Chatbox.SCROLLAREA);
		if (message_lines == null || !message_lines.contains(mouse_pos)) {
			return;
		}
		Widget[] lines = message_lines.getChildren();
		if (lines == null) {
			return;
		}
		for (Widget line : lines) {
			// ensure visible, text is not null, and matching pets text
			if (line.getText() != null
					&& checkVisible(message_lines, line)
					&& line.getText().contains("Pets: (")
					&& line.getText().contains("<img=")) {
				// Get the bounds
				Rectangle bounds = line.getBounds();
				// Get mouse position
				// If mouse not-in message bounds, continue
				if (!bounds.contains(mouse_pos.getX(), mouse_pos.getY())) {
					continue;
				}
				// Extract the pets list by icon ids
				List<Integer> pet_icon_ids = extractPetIconIds(line.getText().substring(line.getText().indexOf(")")));

				// Generate the offset based on the pets width
				int petsTextOffset = line.getFont().getTextWidth("Pets (" + pet_icon_ids.size() + ")");

				// Is it double height with many pets?
				int pets_per_line = pet_icon_ids.size();
				if (bounds.height > PETS_ICON_HEIGHT) {
					pets_per_line = (bounds.width - petsTextOffset) / PETS_ICON_WIDTH;
				}

				// Get the hovered pet by the mouse position within chat line bounds
				int icon_pos = Math.floorDiv((int) (mouse_pos.getX() - (petsTextOffset + bounds.getX())),
						PETS_ICON_WIDTH);
				if (mouse_pos.getY() >= bounds.getY() + PETS_ICON_HEIGHT) {
					icon_pos = Math.floorDiv((int) (mouse_pos.getX() - bounds.getX()),
							PETS_ICON_WIDTH) + pets_per_line;
				}
				// If in bounds of a known icon position
				if (icon_pos >= 0 && icon_pos < pet_icon_ids.size()) {
					int petIcon = pet_icon_ids.get(icon_pos);
					// Bounds verification!
					if (petIcon - plugin.pets_icons_offset >= 0 && petIcon - plugin.pets_icons_offset < plugin.pets.length) {
						String name = client.getItemDefinition(plugin.pets[petIcon - plugin.pets_icons_offset]).getName();
						// Draw a tool tip with the pet's name
						tooltip_manager.add(new Tooltip("Pet: " + name));
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
