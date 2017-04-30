package com.github.unchama.seasonalevents.events.valentine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
	private static boolean isdrop = false;
	private static final String DROPDAY = "2017-02-20";
	private static final String DROPDAYDISP = "2017/02/19";
	private static final String FINISH = "2017-02-27";
	private static final String FINISHDISP = "2017/02/26";

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
			}
			if (now.before(dropdate)) {
				isdrop = true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static SkullMeta playerHeadLore(SkullMeta head) {
		if (isdrop) {
			List<String> lore = new ArrayList<String>();
			lore.add("");
			lore.add(ChatColor.RESET + "" + ChatColor.ITALIC + "" + ChatColor.GREEN + "大切なあなたへ。");
			lore.add(ChatColor.RESET + "" + ChatColor.ITALIC + "" + ChatColor.UNDERLINE + "" + ChatColor.YELLOW + "Happy Valentine 2017");
			head.setLore(lore);
		}
		return head;
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
			if (isdrop) {
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
		if (isdrop) {
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
		List<PotionEffect> ef = Arrays.asList(
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
		List<String> msg = Arrays.asList(
				"火炎耐性", "暗視", "耐性", "跳躍力上昇", "再生能力", "移動速度上昇", "水中呼吸", "緩衝吸収", "攻撃力上昇", "不運");
		int ran = new Random().nextInt(ef.size());
		if (ran != 9) {
			player.addPotionEffect(ef.get(ran));
			player.sendMessage(msg.get(ran) + " IIを奪い取った！あぁ、おいしいなぁ！");
		} else {
			player.addPotionEffect(ef.get(ran));
			player.sendMessage(msg.get(ran) + " IIを感じてしまった…はぁ…むなしいなぁ…");
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
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "リア充を爆発させて奪い取った。");
		lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "食べると一定時間ステータスが変化する。");
		lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "賞味期限を超えると効果が無くなる。");
		lore.add("");
		lore.add(ChatColor.RESET + "" + ChatColor.DARK_GREEN + "賞味期限：" + FINISHDISP);
		lore.add(ChatColor.RESET + "" + ChatColor.AQUA + "ステータス変化（10分）" + ChatColor.GRAY + " （期限内）");
		return lore;
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
		List<String> lore = item.getItemMeta().getLore();
		List<String> plore = getChocoLore();

		// 比較
		return lore.containsAll(plore);
	}

	// アイテム使用時の処理
	private static void useChoco(Player player, ItemStack item) {
		List<String> msg = Arrays.asList(
				player.getName() + "は" + getChocoOwner(item) + "のチョコレートを食べた！猟奇的な味だった。",
				player.getName() + "！" + getChocoOwner(item) + "からのチョコだと思ったかい？ざぁんねんっ！",
				player.getName() + "は" + getChocoOwner(item) + "のプレゼントで鼻血が止まらない！（計画通り）",
				player.getName() + "は" + getChocoOwner(item) + "のチョコレートを頬張ったまま息絶えた！",
				player.getName() + "は" + getChocoOwner(item) + "のチョコにアレが入っているとはを知らずに食べた…",
				player.getName() + "は" + getChocoOwner(item) + "のチョコなんか食ってないであくしろはたらけ",
				getChocoOwner(item) + "は" + player.getName() + "に日頃の恨みを晴らした！スッキリ！",
				getChocoOwner(item) + "による" + player.getName() + "への痛恨の一撃！ハッピーヴァレンタインッ！",
				getChocoOwner(item) + "は" + player.getName() + "が食べる姿を、満面の笑みで見つめている！",
				getChocoOwner(item) + "は悪くない！" + player.getName() + "が悪いんだっ！",
				getChocoOwner(item) + "は" + player.getName() + "を討伐した！",
				"こうして" + getChocoOwner(item) + "のイタズラでまた1人" + player.getName() + "が社畜となった。",
				"おい聞いたか！" + getChocoOwner(item) + "が" + player.getName() + "にチョコ送ったらしいぞー！");
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
		System.out.println(maker + owner);
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
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "手作りのチョコチップクッキー。");
		lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "食べると一定時間ステータスが変化する。");
		lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "賞味期限を超えると効果が無くなる。");
		lore.add("");
		lore.add(ChatColor.RESET + "" + ChatColor.DARK_GREEN + "賞味期限：" + FINISHDISP);
		lore.add(ChatColor.RESET + "" + ChatColor.AQUA + "ステータス変化（10分）" + ChatColor.GRAY + " （期限内）");
		return lore;
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
		String owner = "名称未設定";
		try {
			List<String> lore = item.getItemMeta().getLore();
			String ownerRow = lore.get(lore.size() - 1);
			if (ownerRow.contains(CHOCO_HEAD)) {
				owner = ownerRow.replace(CHOCO_HEAD, "");
			}
		} catch (NullPointerException e) {
		}
		return owner;
	}
}
