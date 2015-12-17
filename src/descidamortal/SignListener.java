package descidamortal;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import descidamortal.Arena.ArenaState;

public class SignListener implements Listener {
	
	private ConfigManager cm = new ConfigManager();
	
	@EventHandler
	public void onCreateSign(SignChangeEvent e) {
		Player p = e.getPlayer();
		if(e.getLine(0).equalsIgnoreCase("[dm]")) {
			if(!e.getLine(1).isEmpty()) {
			if(p.hasPermission("dm.admin")) {
			String arena = e.getLine(1).toLowerCase();
			Arena a = DescidaMortal.getArenaManager().getArena(arena);
			if(a != null) {
			List<String> list = cm.getConfig().getStringList("format");
			e.setLine(0, list.get(0).replaceAll("&", "§"));
			e.setLine(1, list.get(1).replaceAll("&", "§").replaceAll("@arena", e.getLine(1)));
			e.setLine(2, list.get(2).replaceAll("&", "§").replaceAll("@num", "" + a.getPlayersSize()).replaceAll("@max", "" + cm.getInt("max-players")));
			if(a.getState()==ArenaState.AGUARDANDO) {
			e.setLine(3, list.get(3).replaceAll("@state", cm.getConfigMessage("aguardando")));
			} else if(a.getState()==ArenaState.JOGANDO) {
			e.setLine(3, list.get(3).replaceAll("@state", cm.getConfigMessage("jogando")));
			}
			a.addSign(e.getBlock());
			a.loadSigns();
			} else {
			p.sendMessage("§3[Descida Mortal] §cA arena §f" + arena + " §cnao existe.");
			e.getBlock().breakNaturally();
			}
			} else {
				p.sendMessage(cm.getConfigMessage("no-permission"));
				e.getBlock().breakNaturally();
			}
		 } else {
			 p.sendMessage(" ");
			 p.sendMessage("§e======= Formato =======");
			 p.sendMessage("§71. §f[dm]");
			 p.sendMessage("§72. §f<arena>");
			 p.sendMessage("§73. §fNada");
			 p.sendMessage("§74. §fNada");
			 p.sendMessage("§e======================");
			 p.sendMessage(" ");
			 e.getBlock().breakNaturally();
		 }
		}
	}
	
	@EventHandler
	public void onClickSign(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST) {
				Sign s = (Sign)e.getClickedBlock().getState();
				if(ChatColor.stripColor(s.getLine(0)).equalsIgnoreCase("[dm]") && !ChatColor.stripColor(s.getLine(1)).isEmpty()) {
					String arena = ChatColor.stripColor(s.getLine(1));
					Arena a = DescidaMortal.getArenaManager().getArena(arena);
					if(a != null) {
						if(a.getState()==ArenaState.AGUARDANDO) {
						 if(a.getPlayersSize()<cm.getInt("max-players")) {
						  if(DescidaMortal.getArenaManager().getPlayerArena(p)==null) {
							 if(p.getInventory().getHeldItemSlot()==0 || p.getInventory().getHeldItemSlot()==8) {
								 p.getInventory().setHeldItemSlot(4);
							 }
							 a.addPlayer(p);
							 a.updateSigns();
						  } else {
							  p.sendMessage(cm.getConfigMessage("dentro-arena"));
						  }
						 } else {
							 p.sendMessage(cm.getConfigMessage("arena-lotada"));
						 }
						} else if(a.getState()==ArenaState.JOGANDO) {
							p.sendMessage(cm.getConfigMessage("arena-jogando"));
						}
					}
				}
			}
		}
	}

}
