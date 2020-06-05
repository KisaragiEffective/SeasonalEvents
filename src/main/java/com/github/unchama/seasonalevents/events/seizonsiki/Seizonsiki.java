package com.github.unchama.seasonalevents.events.seizonsiki;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.unchama.seasonalevents.SeasonalEvents;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Mana;
import com.github.unchama.seichiassist.data.PlayerData;

public class Seizonsiki implements Listener {
	private static boolean isdrop = false;
	private static final String DROPDAY = "2017-01-16";
	private static final String DROPDAYDISP = calcYesterday(DROPDAY);
	private static final String FINISH = "2017-01-22";
	private static final String FINISHDISP = calcYesterday(FINISH);

	private final SeasonalEvents plugin = SeasonalEvents.getInstance();

	// yyyy/MM/dd
	public static String calcYesterday(String s) {
		try {
			Instant ins = new SimpleDateFormat("yyyy-MM-dd").parse(s).toInstant().minus(1, ChronoUnit.DAYS);
			return ins.get(ChronoField.YEAR) + "/" + ins.get(ChronoField.MONTH_OF_YEAR) + "/" + ins.get(ChronoField.DAY_OF_MONTH);
		} catch (ParseException e) {
			throw new RuntimeException("フォーマットが正しくありません！", e);
		}
	}
	public Seizonsiki() {
		try {
			// イベント開催中か判定
			Date now = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date finishdate = format.parse(FINISH);
			Date dropdate = format.parse(DROPDAY);
			if (now.before(finishdate)) {
				// リスナーを登録
				Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
			}
			if (now.before(dropdate)) {
				isdrop = true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getType().equals(EntityType.ZOMBIE) &&
				event.getEntity().getKiller() != null) {
			killEvent(event.getEntity().getKiller(), event.getEntity().getLocation());
		}
	}

	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event) {
		if (isdrop) {
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + Seizonsiki.DROPDAYDISP + "までの期間限定で、シーズナルイベント『チャラゾンビたちの成ゾン式！』を開催しています。");
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "詳しくは下記wikiをご覧ください。");
			event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + SeasonalEvents.config.getWikiAddr());
		}
	}

	@EventHandler
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
		if (checkPrize(event.getItem())) {
			usePrize(event.getPlayer());
		}
	}

	/** ゾンビがプレイヤーに倒されたとき発生 */
	private void killEvent(Player killer, Location loc) {
		if (isdrop) {
			double dp = SeasonalEvents.config.getDropPer();
			// 0.0 - 100.0
			double rand = Math.random() * 100;
			if (rand < dp) {
				// 報酬をドロップ
				killer.getWorld().dropItemNaturally(loc, makePrize());
			}
		}
	}

	// チャラゾンビの肉判定
	private boolean checkPrize(ItemStack item) {
		// Lore取得
		if(!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
			return false;
		}
		List<String> lore = item.getItemMeta().getLore();
		List<String> plore = getPrizeLore();

		// 比較
		return lore.containsAll(plore);
	}

	// アイテム使用時の処理
	private void usePrize(Player player) {
		UUID uuid = player.getUniqueId();
		PlayerData pd = SeichiAssist.playermap.get(uuid);
		Mana mana = pd.activeskilldata.mana;

		double max = mana.calcMaxManaOnly(player, pd.level);
		mana.increaseMana(max * 0.1, player, pd.level);
		player.playSound(player.getLocation(),Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F);
	}

	private ItemStack makePrize() {
		ItemStack prize = new ItemStack(Material.GOLDEN_APPLE, 1);
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
		itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "ゾんご");
		itemmeta.setLore(getPrizeLore());
		prize.setItemMeta(itemmeta);
		return prize;
	}

	private List<String> getPrizeLore() {
		return Collections.unmodifiableList(Arrays.asList(
				"",
				ChatColor.RESET + "" +  ChatColor.GRAY + "成ゾン式で暴走していたチャラゾンビから没収した。",
				ChatColor.RESET + "" +  ChatColor.GRAY + "ゾンビたちが栽培しているりんご。",
				ChatColor.RESET + "" +  ChatColor.GRAY + "良質な腐葉土で1つずつ大切に育てられた。",
				ChatColor.RESET + "" +  ChatColor.GRAY + "栄養豊富で、食べるとマナが10%回復する。",
				ChatColor.RESET + "" +  ChatColor.GRAY + "腐りやすいため賞味期限を超えると効果が無くなる。",
				"",
				ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "賞味期限：" + FINISHDISP,
				ChatColor.RESET + "" +  ChatColor.AQUA + "マナ回復（10％）" + ChatColor.GRAY + " （期限内）"
		));
	}
}
