package com.github.unchama.seasonalevents;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.unchama.seasonalevents.events.seizonsiki.Seizonsiki;
import com.github.unchama.seasonalevents.events.valentine.Valentine;
import com.github.unchama.seasonalevents.util.Config;

import java.util.HashMap;
import java.util.Map;

public class SeasonalEvents extends JavaPlugin {
	public static Config config;
	private static SeasonalEvents INSTANCE;

	public SeasonalEvents() {
		SeasonalEvents.INSTANCE = this;
	}

	@Override
	public void onEnable() {
		// コンフィグ読み込み
		config = new Config(this);
		config.loadConfig();
		// loadEventHooks();
		// 成ゾン式イベント
		new Seizonsiki();
		// バレンタインイベント
		new Valentine(this);

		// System.out.println("debug");
	}

	// TODO: Needs test
	private void loadEventHooks() {
		Map<String, Listener> handler = new HashMap<>();
		handler.put("seizon", new Seizonsiki());
		handler.put("valentine", new Valentine(this));
		// Expected Map<String, Object>
		Map<?, ?> events = config.getConfig().getMapList("events").get(0);

		events.forEach((k, v) -> {
			getLogger().info("[Event register] " + k + " : " + v);
		});
	}

	@Override
	public void onDisable() {
	}

	public static SeasonalEvents getInstance() {
		return INSTANCE;
	}
}
