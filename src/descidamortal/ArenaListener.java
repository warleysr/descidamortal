package descidamortal;

import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import descidamortal.Arena.ArenaState;

public class ArenaListener implements Listener {
	
	private ConfigManager cm = new ConfigManager();
	private int win_block = cm.getInt("win-block");
	private int max_players = cm.getInt("max-players");
	private boolean pvp = cm.getConfig().getBoolean("pvp");
	private String sem_pvp = cm.getConfigMessage("sem-pvp");
	private String saiu = cm.getConfigMessage("saiu-partida");
	private String morreu = cm.getConfigMessage("morreu");
	private String sem_comandos = cm.getConfigMessage("sem-comandos");
	
	@EventHandler
	public void onLeaveArena(PlayerQuitEvent e) {
		e.setQuitMessage(null);
		Player p = e.getPlayer();
		Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(a!=null && a.hasPlayer(p)) {
			for(Player o : a.getPlayers()) {
				o.sendMessage(saiu.replaceAll("@player", p.getName()).replaceAll("@num", "" + (a.getPlayersSize()-1)).replaceAll("@max", "" + max_players));
			}
			a.removePlayer(p);
			a.updateSigns();
		}
	}
	
	@EventHandler
	public void onRespawnInArena(PlayerRespawnEvent e) {
		final Player p = e.getPlayer();
		final Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(a!=null && a.hasPlayer(p) && a.getState()==ArenaState.JOGANDO) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Kit k = DescidaMortal.getKitManager().getPlayerKit(p);
					p.teleport(a.getTeleportLocation());
					for(PotionEffect ef : p.getActivePotionEffects()) {
						p.removePotionEffect(ef.getType());
					}
					if(k!=null) {
						if(k.isRetrievable()) {
						DescidaMortal.getKitManager().equiparKit(p);
						} else {
							DescidaMortal.getArenaManager().equipPlayerWithDefaultItems(p);
						}
					} else {
						DescidaMortal.getArenaManager().equipPlayerWithDefaultItems(p);
					}
				}
			}.runTaskLater(DescidaMortal.getPlugin(), 1);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onWinArena(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(a!=null && a.hasPlayer(p) && a.getState()==ArenaState.JOGANDO) {
			if(e.getFrom().getBlockX()!=e.getTo().getBlockX() || e.getFrom().getBlockZ()!=e.getTo().getBlockZ()) {
				if(e.getTo().getBlock().getRelative(BlockFace.DOWN).getType().getId()==win_block && p.getHealth()>0) {
					for(Player o : a.getPlayers()) {
						o.sendMessage(cm.getConfigMessage("vencedor").replaceAll("@player", p.getName()));
						o.playSound(p.getLocation(), Sound.FIREWORK_BLAST, 1, 1);
					}
					if(DescidaMortal.usingVault()) {
					DescidaMortal.premiar(p);
					}
					a.finalizar();
				}
			}
		}
	}
	
	@EventHandler
	public void toUpdateScoreboard(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(a!=null && a.hasPlayer(p) && a.getState()==ArenaState.JOGANDO) {
			if(e.getFrom().getBlockX()!=e.getTo().getBlockX() || e.getFrom().getBlockY()!=e.getTo().getBlockY() || e.getFrom().getBlockZ()!=e.getTo().getBlockZ()) {
				a.updateScoreboard();
			}
		}
	}
	
	@EventHandler
	public void onDeathInArena(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(a!=null && a.hasPlayer(p) && a.getState()==ArenaState.JOGANDO) {
			e.setDeathMessage(null);
			e.getDrops().clear();
			for(Player o : a.getPlayers()) {
				o.sendMessage(morreu.replaceAll("@player", p.getName()));
			}
		}
	}
	
	@EventHandler
	public void onPvPDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player)e.getEntity();
			Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
			if(a!=null && a.hasPlayer(p) && a.getState()==ArenaState.JOGANDO && !(pvp)) {
				e.setCancelled(true);
				if(e.getDamager() instanceof Player) {
					Player d = (Player)e.getDamager();
					d.sendMessage(sem_pvp);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onDropItemInArena(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(a!=null && a.hasPlayer(p)) {
			e.setCancelled(true);
			p.updateInventory();
		}
	}
	
	@EventHandler
	public void onExecuteCommandInArena(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(a!=null && a.hasPlayer(p)) {
			if(!(e.getMessage().startsWith("/descidamortal"))) {
			e.setCancelled(true);
			p.sendMessage(sem_comandos);
			}
		}
	}
	
	@EventHandler
	public void onBreakBlockInArena(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(a!=null && a.hasPlayer(p)) {
			e.setCancelled(true);
		}
	}
}
