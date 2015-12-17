package descidamortal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class Arena {
	
	private String id;
	private ArenaState state;
	private List<String> players = new ArrayList<>();
	private Location lobby_location;
	private Location teleport_location;
	private Location exit_location;
	private Sign[] signs;
	private ConfigManager cm = null;
	private BukkitTask core = null;
	private BukkitTask init = null;
	private BukkitTask count = null;
	private Scoreboard sb = null;
	private Objective obj = null;
	private HashMap<String, Score> scores = new HashMap<>();
	private List<String> sign_format;
	
	public Arena(String id, Location ll, Location tl, Location el) {
		this.id = id;
		this.lobby_location = ll;
		this.teleport_location = tl;
		this.exit_location = el;
		state = ArenaState.AGUARDANDO;
		cm = new ConfigManager(this);
		sign_format = cm.getConfig().getStringList("format");
		sb = Bukkit.getScoreboardManager().getNewScoreboard();
		obj = sb.registerNewObjective(getID(), "dummy");
		obj.setDisplayName(cm.getConfigMessage("title"));
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		loadSigns();
		updateSigns();
	}
	
	public String getID() {
		return id;
	}
	
	public ArenaState getState() {
		return state;
	}

	public void setState(ArenaState state) {
		this.state = state;
	}

	public Location getLobbyLocation() {
		return lobby_location;
	}

	public Location getTeleportLocation() {
		return teleport_location;
	}
	
	public Location getExitLocation() {
		return exit_location;
	}
	
	public Player[] getPlayers() {
		Player[] all = {};
		for(int i=0; i<players.size(); i++) {
			Player p = Bukkit.getPlayer(players.get(i));
			if(p != null) {
				all = Arrays.copyOf(all, all.length + 1);
				all[all.length - 1] = p;
			}
		}
		return all;
	}
	
	public int getPlayersSize() {
		return players.size();
	}
	
	@SuppressWarnings("deprecation")
	public void addPlayer(Player p) {
		if(!(hasPlayer(p))) {
			players.add(p.getName());
			p.getInventory().clear();
			p.getEquipment().setArmorContents(null);
			p.teleport(getLobbyLocation());
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			p.getInventory().setItem(0, DescidaMortal.getArenaManager().getKitSelector());
			p.getInventory().setItem(8, DescidaMortal.getArenaManager().getExitArena());
			p.updateInventory();
			for(Player o : getPlayers()) {
				o.sendMessage(cm.getConfigMessage("entrou-partida").replaceAll("@player", p.getName()).replaceAll("@num", "" + getPlayersSize()).replaceAll("@max", "" + cm.getInt("max-players")));
			}
			start();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void removePlayer(Player p) {
		if(hasPlayer(p)) {
			players.remove(p.getName());
			p.getInventory().clear();
			p.getEquipment().setArmorContents(null);
			p.updateInventory();
			p.teleport(getExitLocation());
			DescidaMortal.getKitManager().resetKit(p);
			p.setMaxHealth(20);
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			for(PotionEffect ef : p.getActivePotionEffects()) {
				p.removePotionEffect(ef.getType());
			}
			BarAPI.removeBar(p);
			sb.resetScores(Bukkit.getOfflinePlayer(p.getName()));
			if(scores.containsKey(p.getName())) {
				scores.remove(p.getName());
			}
			int min = cm.getInt("min-players");
			if(getState()==ArenaState.JOGANDO) {
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			stop();
			} else if(getState()==ArenaState.AGUARDANDO) {
				if(getPlayersSize()<min) {
					if(core!=null) {
						core.cancel();
						core = null;
					}
					if(init!=null) {
						init.cancel();
						init = null;
					}
					if(count!=null) {
						count.cancel();
						count = null;
					}
					String msg = cm.getConfigMessage("insuficientes");
					for(Player o : getPlayers()) {
						o.sendMessage(msg);
					}
				}
			}
		}
	}
	
	public boolean hasPlayer(Player p) {
		if(players.contains(p.getName())) {
			return true;
		}
		return false;
	}
	
	public void updateSigns() {
		for(Sign s : signs) {
			s.setLine(0, sign_format.get(0).replaceAll("&", "§"));
			s.setLine(1, sign_format.get(1).replaceAll("&", "§").replaceAll("@arena", getID()));
			s.setLine(2, sign_format.get(2).replaceAll("&", "§").replaceAll("@num", "" + getPlayersSize()).replaceAll("@max", "" + cm.getInt("max-players")));
			if(getState()==ArenaState.AGUARDANDO) {
			s.setLine(3, sign_format.get(3).replaceAll("@state", cm.getConfigMessage("aguardando")));
			} else if(getState()==ArenaState.JOGANDO) {
			s.setLine(3, sign_format.get(3).replaceAll("@state", cm.getConfigMessage("jogando")));
			}
			s.update();
		}
	}
	
	public void loadSigns() {
		cm.reload();
		signs = new Sign[]{};
		List<String> sign_list = cm.getArenaConfig().getStringList("sign-list");
		for(String s : sign_list) {
			String[] args = s.split(";");
			Location loc = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
			if(loc.getBlock().getType() == Material.WALL_SIGN || loc.getBlock().getType() == Material.SIGN_POST) {
				Sign sign = (Sign)loc.getBlock().getState();
				signs = Arrays.copyOf(signs, signs.length + 1);
				signs[signs.length - 1] = sign;
			}
		}
	}
	
	public void addSign(Block b) {
		List<String> sign_list = cm.getArenaConfig().getStringList("sign-list");
		String toAdd = b.getLocation().getWorld().getName() + ";" + b.getLocation().getBlockX() + ";" + b.getLocation().getBlockY() + ";" + b.getLocation().getBlockZ();
		if(!sign_list.contains(toAdd)) {
			sign_list.add(toAdd);
			cm.getArenaConfig().set("sign-list", sign_list);
			cm.saveArenaConfig();
		}
	}
	
	private void start() {
		if(getPlayersSize()>=cm.getInt("min-players") && getPlayersSize()<=cm.getInt("max-players")) {
			if(init==null) {
				for(Player o : getPlayers()) {
					o.sendMessage(cm.getConfigMessage("iniciando-em").replaceAll("@tempo", "" + cm.getInt("iniciar-em")));
				}
				init = new BukkitRunnable() {
					@Override
					public void run() {
						count = new BukkitRunnable() {
							int c = cm.getInt("countdown");
							@SuppressWarnings("deprecation")
							@Override
							public void run() {
								if(c>0) {
									for(Player p : getPlayers()) {
										p.sendMessage(cm.getConfigMessage("iniciando-em").replaceAll("@tempo", "" + c));
										p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
									}
									c--;
								} else {
									count.cancel();
									count = null;
									setState(ArenaState.JOGANDO);
									updateSigns();
									for(Player p : getPlayers()) {
										p.teleport(getTeleportLocation());
										p.setHealth(p.getHealth());
										p.setFoodLevel(20);
										if(p.getGameMode()!=GameMode.SURVIVAL) {
											p.setGameMode(GameMode.SURVIVAL);
										}
										p.getInventory().clear();
										p.getEquipment().setArmorContents(null);
										p.sendMessage(cm.getConfigMessage("partida-iniciada"));
										Kit k = DescidaMortal.getKitManager().getPlayerKit(p);
										if(k!=null) {
											DescidaMortal.getKitManager().equiparKit(p);
										} else {
											DescidaMortal.getArenaManager().equipPlayerWithDefaultItems(p);
										}
										p.updateInventory();
										Score s = obj.getScore(Bukkit.getOfflinePlayer(p.getName()));
										s.setScore(p.getLocation().getBlockY());
										scores.put(p.getName(), s);
										p.setScoreboard(sb);
									}
									init.cancel();
									init = null;
									cancel();
									core = new BukkitRunnable() {
										int remaning = cm.getInt("time");
										int max = remaning;
										String bar_title = cm.getConfigMessage("bar");
										@Override
										public void run() {
											if(remaning>0) {
												for(Player o : getPlayers()) {
													BarAPI.setMessage(o, bar_title.replaceAll("@tempo", format(remaning)), (float)100*remaning/max);
												}
												remaning--;
											} else {
												for(Player o : getPlayers()) {
													o.teleport(getExitLocation());
													o.sendMessage(cm.getConfigMessage("sem-vencedores"));
												}
												finalizar();
											}
										}
									}.runTaskTimer(DescidaMortal.getPlugin(), 0, 20);
								}
							}
						}.runTaskTimer(DescidaMortal.getPlugin(), 0, 20);
					}
				}.runTaskLater(DescidaMortal.getPlugin(), (cm.getInt("iniciar-em")-cm.getInt("countdown"))*20);
			}
		}
	}
	
	public void updateScoreboard() {
		for(Entry<String, Score> entry : scores.entrySet()) {
			Score s = entry.getValue();
			Player p = Bukkit.getPlayer(entry.getKey());
			if(p!=null && hasPlayer(p)) {
				s.setScore(p.getLocation().getBlockY());
			}
		}
	}
	
	private void stop() {
		if(getState()==ArenaState.JOGANDO && getPlayersSize()<2) {
			for(Player p : getPlayers()) {
				p.teleport(getExitLocation());
				p.sendMessage(cm.getConfigMessage("vencedor").replaceAll("@player", p.getName()));
				p.playSound(p.getLocation(), Sound.FIREWORK_BLAST, 1, 1);
			}
			finalizar();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void finalizar() {
		if(core != null) {
			core.cancel();
			core = null;
		}
		if(init != null) {
			init.cancel();
			init = null;
		}
		if(count != null) {
			count.cancel();
			count = null;
		}
		for(Player p : getPlayers()) {
			p.teleport(getExitLocation());
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			BarAPI.removeBar(p);
			p.getInventory().clear();
			p.getEquipment().setArmorContents(null);
			p.updateInventory();
			p.setMaxHealth(20);
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			for(PotionEffect ef : p.getActivePotionEffects()) {
				p.removePotionEffect(ef.getType());
			}
		}
		setState(ArenaState.AGUARDANDO);
		players.clear();
		scores.clear();
		updateSigns();
	}
	
	@SuppressWarnings("deprecation")
	public void disable() {
		if(core != null) {
			core.cancel();
			core = null;
		}
		if(init != null) {
			init.cancel();
			init = null;
		}
		if(count != null) {
			count.cancel();
			count = null;
		}
		for(Player p : getPlayers()) {
			p.teleport(getExitLocation());
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			p.getInventory().clear();
			p.getEquipment().setArmorContents(null);
			p.updateInventory();
			p.setMaxHealth(20);
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			for(PotionEffect ef : p.getActivePotionEffects()) {
				p.removePotionEffect(ef.getType());
			}
		}
		setState(ArenaState.AGUARDANDO);
		scores.clear();
		players.clear();
	}
	
	 private String format(int i) {
	        int min = i / 60;
	        int sec = i % 60;
	        return (min >= 10 ? min + "" : "0" + min + "") + ":"
	                + (sec >= 10 ? sec + "" : "0" + sec + "");
	    }

	public static enum ArenaState {JOGANDO, AGUARDANDO;}

}
