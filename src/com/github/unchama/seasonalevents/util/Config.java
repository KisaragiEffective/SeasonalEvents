package com.github.unchama.seasonalevents.util;

import org.bukkit.configuration.file.FileConfiguration;

import com.github.unchama.seasonalevents.SeasonalEvents;

public class Config {
	private static FileConfiguration config;
	private SeasonalEvents plugin;

	// コンストラクタ
	public Config(SeasonalEvents plugin) {
		this.plugin = plugin;
		saveDefaultConfig();
	}

	// コンフィグのロード
	public void loadConfig() {
		config = getConfig();
	}

	// コンフィグのリロード
	public void reloadConfig() {
		plugin.reloadConfig();
		config = getConfig();
	}

	// コンフィグのセーブ
	public void saveConfig() {
		plugin.saveConfig();
	}

	// plugin.ymlがない時にDefaultのファイルを生成
	public void saveDefaultConfig() {
		plugin.saveDefaultConfig();
	}

	// plugin.ymlファイルからの読み込み
	public FileConfiguration getConfig() {
		return plugin.getConfig();
	}

	public double getDropPer() {
		return Double.parseDouble(config.getString("dropper"));
	}

	public String getWikiAddr() {
		return config.getString("wiki");
	}

	/**
	 * イベントドロップ終了日を指定します。(西暦4桁-月2桁-日付2桁)
	 *
	 * @return イベントドロップ終了日 (西暦4桁-月2桁-日付2桁)
	 */
	public String getDropFinishDay() {
		return config.getString("DropFinishDay");
	}

	/**
	 * イベント終了日を取得します。(西暦4桁-月2桁-日付2桁)
	 *
	 * @return イベント終了日 (西暦4桁-月2桁-日付2桁)
	 */
	public String getEventFinishDay() {
		return config.getString("EventFinishDay");
	}

	/**
	 * イベントドロップ終了日(表示用)を取得します。(西暦4桁/月2桁/日付2桁)
	 *
	 * @return イベントドロップ終了日(表示用) (西暦4桁/月2桁/日付2桁)
	 */
	public String getDropFinishDayDisp() {
		return config.getString("DropFinishDayDisp");
	}

	/**
	 * イベント終了日(表示用)を取得します。(西暦4桁/月2桁/日付2桁)
	 *
	 * @return イベント終了日(表示用) (西暦4桁/月2桁/日付2桁)
	 */
	public String getEventFinishDayDisp() {
		return config.getString("EventFinishDayDisp");
	}
}
