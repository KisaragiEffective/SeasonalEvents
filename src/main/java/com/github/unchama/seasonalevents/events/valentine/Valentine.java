package com.github.unchama.seasonalevents.events.valentine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.github.unchama.seasonalevents.util.Config;
import com.github.unchama.seichiassist.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.unchama.seasonalevents.SeasonalEvents;
import com.github.unchama.seichiassist.util.Util;

public class Valentine implements Listener {
	private static boolean drop = false;
	public static boolean isInEvent = false;
	private static Config config = SeasonalEvents.config;
	/*
	時間に関してだが、Date#beforeは指定した日付よりも前->true 後->false
	つまり2018-02-20の場合は、2018/02/20 00:00 よりも前ならtrueを返します。
	鯖再起動は4時なので、その際に判定が行われる関係で2018/02/20 4時までが期限となります。
	*/
	private static final String DROPDAY = config.getDropFinishDay();
	private static final String DROPDAYDISP = config.getDropFinishDayDisp();
	private static final String FINISH = config.getEventFinishDay();
	private static final String FINISHDISP = config.getEventFinishDayDisp();
	private static final List<PotionEffect> applyEffects = Arrays.asList(
			new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 12000, 1),
			new PotionEffect(PotionEffectType.NIGHT_VISION, 12000, 1),
			new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 12000, 1),
			new PotionEffect(PotionEffectType.JUMP, 12000, 1),
			new PotionEffect(PotionEffectType.REGENERATION, 12000, 1),
			new PotionEffect(PotionEffectType.SPEED, 12000, 1),
			new PotionEffect(PotionEffectType.WATER_BREATHING, 12000, 1),
			new PotionEffect(PotionEffectType.ABSORPTION, 12000, 1),
			new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 12000, 1),
			new PotionEffect(PotionEffectType.UNLUCK, 1200, 1));
	private static final List<String> japaneseEffectName = Arrays.asList(
			"火炎耐性", "暗視", "耐性", "跳躍力上昇", "再生能力", "移動速度上昇", "水中呼吸", "緩衝吸収", "攻撃力上昇", "不運");
	public Valentine(SeasonalEvents parent) {
		try {
			// イベント開催中か判定
			Date now = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date finishdate = format.parse(FINISH);
			Date dropdate = format.parse(DROPDAY);
			if (now.before(finishdate)) {
				// リスナーを登録
				parent.getServer().getPluginManager().registerEvents(this, parent);
				isInEvent = true;
			}
			if (now.before(dropdate)) {
				drop = true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static SkullMeta playerHeadLore(SkullMeta head) {
		if (drop) {
			String year = DROPDAY.substring(0, 4);
			head.setLore(getSkullLore(year));
		}
		return head;
	}

	private static List<String> getSkullLore(String year) {
		return Collections.unmodifiableList(Arrays.asList(
				"",
				"" + ChatColor.RESET + ChatColor.ITALIC + ChatColor.GREEN + "大切なあなたへ。",
				"" + ChatColor.RESET + ChatColor.ITALIC + ChatColor.UNDERLINE + ChatColor.YELLOW + "Happy Valentine " + year
		));
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		try {
			if (event.getEntity() instanceof Monster && event.getEntity().isDead()) {
				killEvent(event.getEntity(), event.getEntity().getLocation());
			}
		} catch (NullPointerException e) {
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		try {
			if (event.getEntity().getLastDamageCause().getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
				// 死因が爆発の場合、確率でアイテムをドロップ
				killEvent(event.getEntity(), event.getEntity().getLocation());
			}
		} catch (NullPointerException e) {
		}
	}

	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event) {
		try {
			if (drop) {
				event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + Valentine.DROPDAYDISP + "までの期間限定で、シーズナルイベント『＜ブラックバレンタイン＞リア充 vs 整地民！』を開催しています。");
				event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "詳しくは下記wikiをご覧ください。");
				event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + SeasonalEvents.config.getWikiAddr());
			}
		} catch (NullPointerException e) {
		}
	}

	@EventHandler
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
		try {
			if (checkPrize(event.getItem())) {
				usePrize(event.getPlayer());
			}
			if (isChoco(event.getItem())) {
				useChoco(event.getPlayer(), event.getItem());
			}
		} catch (NullPointerException e) {
		}
	}

	// プレイヤーにクリーパーが倒されたとき発生
	private void killEvent(Entity entity, Location loc) {
		if (drop) {
			double dp = SeasonalEvents.config.getDropPer();
			double rand = new Random().nextInt(100);
			if (rand < dp) {
				// 報酬をドロップ
				entity.getWorld().dropItemNaturally(loc, makePrize());
			}
		}
	}

	// チョコチップクッキー判定
	private boolean checkPrize(ItemStack item) {
		// Lore取得
		if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
			return false;
		}
		List<String> lore = item.getItemMeta().getLore();
		List<String> plore = getPrizeLore();

		// 比較
		return lore.containsAll(plore);
	}

	// アイテム使用時の処理
	private void usePrize(Player player) {

		int ran = new Random().nextInt(applyEffects.size());
		if (ran != applyEffects.size() - 1) { // UNLUCKではない
			player.addPotionEffect(applyEffects.get(ran));
			player.sendMessage(japaneseEffectName.get(ran) + " IIを奪い取った！あぁ、おいしいなぁ！");
		} else {
			player.addPotionEffect(applyEffects.get(ran));
			player.sendMessage(japaneseEffectName.get(ran) + " IIを感じてしまった…はぁ…むなしいなぁ…");
		}
		player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F);
	}

	private ItemStack makePrize() {
		ItemStack prize = new ItemStack(Material.COOKIE, 1);
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKIE);
		itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "チョコチップクッキー");
		itemmeta.setLore(getPrizeLore());
		prize.setItemMeta(itemmeta);
		return prize;
	}

	private List<String> getPrizeLore() {
		return Collections.unmodifiableList(Arrays.asList(
				"",
				ChatColor.RESET + "" + ChatColor.GRAY + "リア充を爆発させて奪い取った。",
				ChatColor.RESET + "" + ChatColor.GRAY + "食べると一定時間ステータスが変化する。",
				ChatColor.RESET + "" + ChatColor.GRAY + "賞味期限を超えると効果が無くなる。",
				"",
				ChatColor.RESET + "" + ChatColor.DARK_GREEN + "賞味期限：" + FINISHDISP,
				ChatColor.RESET + "" + ChatColor.AQUA + "ステータス変化（10分）" + ChatColor.GRAY + " （期限内）"
		));
	}

	// チョコレート配布
	public static void giveChoco(Player player) {
		if (!Util.isPlayerInventryFill(player)) {
			Util.addItem(player, makeChoco(player));
		} else {
			Util.dropItem(player, makeChoco(player));
		}
	}

	// チョコレート判定
	private static boolean isChoco(ItemStack item) {
		// Lore取得
		if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
			return false;
		}
		List<String> lore2 = item.getItemMeta().getLore();
		List<String> lore1 = getChocoLore();

		// 比較
		return lore2.containsAll(lore1);
	}

	// アイテム使用時の処理
	private static void useChoco(Player player, ItemStack item) {
		// FIXME: BAD NAME
		String a = player.getName();
		// FIXME: BAD NAME
		String b = getChocoOwner(item);
		List<String> msg = Arrays.asList(
				a + "は" + b + "のチョコレートを食べた！猟奇的な味だった。",
				a + "！" + b + "からのチョコだと思ったかい？ざぁんねんっ！",
				a + "は" + b + "のプレゼントで鼻血が止まらない！（計画通り）",
				a + "は" + b + "のチョコレートを頬張ったまま息絶えた！",
				a + "は" + b + "のチョコにアレが入っているとはを知らずに食べた…",
				a + "は" + b + "のチョコなんか食ってないであくしろはたらけ",
				b + "は" + a + "に日頃の恨みを晴らした！スッキリ！",
				b + "による" + a + "への痛恨の一撃！ハッピーヴァレンタインッ！",
				b + "は" + a + "が食べる姿を、満面の笑みで見つめている！",
				b + "は悪くない！" + a + "が悪いんだっ！",
				b + "は" + a + "を討伐した！",
				"こうして" + b + "のイタズラでまた1人" + a + "が社畜となった。",
				"おい聞いたか！" + b + "が" + a + "にチョコ送ったらしいぞー！");
		if (isChocoOwner(item, player.getName())) {
			// HP最大値アップ
			player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 12000, 10));
		} else {
			// 死ぬ
			player.setHealth(0);
			// 全体にメッセージ送信
			Util.sendEveryMessage(msg.get(new Random().nextInt(msg.size())));
		}
		player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F);
	}

	private static ItemStack makeChoco(Player player) {
		ItemStack choco = new ItemStack(Material.COOKIE, 64);
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKIE);
		itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "チョコチップクッキー");
		choco.setItemMeta(itemmeta);
		setChocoLore(choco);
		setChocoOwner(choco, player.getName());
		return choco;
	}

	private static boolean isChocoOwner(ItemStack item, String owner) {
		String maker = getChocoOwner(item);
		SeasonalEvents.getInstance().getLogger().info(maker + owner);
		return maker.equals(owner);
	}

	private static boolean setChocoLore(ItemStack item) {
		try {
			ItemMeta meta = item.getItemMeta();
			meta.setLore(getChocoLore());
			item.setItemMeta(meta);
		} catch (NullPointerException e) {
		}
		return false;
	}

	private static List<String> getChocoLore() {
		// 日付が関わるのでインライン化はしない
		return Collections.unmodifiableList(Arrays.asList(
				"",
				ChatColor.RESET + "" + ChatColor.GRAY + "手作りのチョコチップクッキー。",
				ChatColor.RESET + "" + ChatColor.GRAY + "食べると一定時間ステータスが変化する。",
				ChatColor.RESET + "" + ChatColor.GRAY + "賞味期限を超えると効果が無くなる。",
				"",
				ChatColor.RESET + "" + ChatColor.DARK_GREEN + "賞味期限：" + FINISHDISP,
				ChatColor.RESET + "" + ChatColor.AQUA + "ステータス変化（10分）" + ChatColor.GRAY + " （期限内）"
		));
	}

	private static final String CHOCO_HEAD = ChatColor.RESET + "" + ChatColor.DARK_GREEN + "製作者：";

	private static boolean setChocoOwner(ItemStack item, String owner) {
		try {
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			lore.add(CHOCO_HEAD + owner);
			meta.setLore(lore);
			item.setItemMeta(meta);
			return true;
		} catch (NullPointerException e) {
		}
		return false;
	}

	private static String getChocoOwner(ItemStack item) {
		String owner = null;
		try {
			List<String> lore = item.getItemMeta().getLore();
			String ownerRow = lore.get(lore.size() - 1);
			if (ownerRow.contains(CHOCO_HEAD)) {
				owner = ownerRow.replace(CHOCO_HEAD, "");
			}
		} catch (NullPointerException e) {
		}
		return owner == null ? "名称未設定" : owner;
	}
}
