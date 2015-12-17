package descidamortal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import descidamortal.Arena.ArenaState;

public class KitListener implements Listener {
	
	private ConfigManager cm = new ConfigManager();
	private Inventory selector = Bukkit.createInventory(null, cm.getInt("inv-slots"), cm.getConfigMessage("inv-title"));
	private String saiu = cm.getConfigMessage("saiu-partida");
	private String abandonou = cm.getConfigMessage("abandonou");
	private int max_players = cm.getInt("max-players");
	
	public KitListener() {
		for(Kit k : DescidaMortal.getKitManager().getKits()) {
			selector.setItem(selector.firstEmpty(), k.getIcon());
		}
	}
	
	@EventHandler
	public void onPlayerOpenKitSelector(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(e.getAction().name().contains("RIGHT")) {
			if(a!=null && a.hasPlayer(p) && a.getState()==ArenaState.AGUARDANDO) {
			 if(p.getItemInHand().equals(DescidaMortal.getArenaManager().getKitSelector())) {
				p.openInventory(selector);
			 } else if(p.getItemInHand().equals(DescidaMortal.getArenaManager().getExitArena())) {
				for(Player o : a.getPlayers()) {
				  if(o!=p) {
				  o.sendMessage(saiu.replaceAll("@player", p.getName()).replaceAll("@num", "" + (a.getPlayersSize()-1)).replaceAll("@max", "" + max_players));
				  }
				}
				a.removePlayer(p);
				a.updateSigns();
				p.sendMessage(abandonou);
			 }
			}
		}
	}
	
	@EventHandler
	public void onPlayerSelectKit(InventoryClickEvent e) {
		if(e.getCurrentItem()==null)return;
		Player p = (Player)e.getWhoClicked();
		Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
		if(e.getInventory().equals(selector)) {
			if(a!=null && a.hasPlayer(p) && a.getState()==ArenaState.AGUARDANDO) {
				e.setCancelled(true);
				Kit k = DescidaMortal.getKitManager().getKitByIcon(e.getCurrentItem());
				if(k!=null) {
					if(p.hasPermission(k.getPermission())) {
					DescidaMortal.getKitManager().setKit(p, k);
					p.sendMessage(cm.getConfigMessage("escolheu-kit").replaceAll("@kit", ChatColor.stripColor(k.getIcon().getItemMeta().getDisplayName())));
				 } else {
					p.sendMessage(cm.getConfigMessage("no-permission")); 
				 }
				}
				p.closeInventory();
			} else {
				e.setCancelled(true);
				p.closeInventory();
			}
		}
	}

}
