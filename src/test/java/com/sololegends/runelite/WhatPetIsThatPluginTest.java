package com.sololegends.runelite;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WhatPetIsThatPluginTest {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(WhatPetIsThatPlugin.class);
		RuneLite.main(args);
	}
}