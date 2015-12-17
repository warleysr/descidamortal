package descidamortal;

import java.io.File;
import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {
	
	private ConfigManager cm = new ConfigManager();
	private String saiu = cm.getConfigMessage("saiu-partida");
	private String abandonou = cm.getConfigMessage("abandonou");
	private String nenhuma_partida = cm.getConfigMessage("nenhuma-partida");
	private int max_players = cm.getInt("max-players");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("[Descida Mortal] Comando apenas para players in-game.");
			return true;
		}
		Player p = (Player)sender;
		if(cmd.equalsIgnoreCase("dm")) {
			if(args.length == 0) {
				if(p.hasPermission("dm.admin")) {
				usage(p, cmd);
				} else {
					p.sendMessage("§eUso correto: §7/dm sair");
				}
				return true;
			} else if(args.length == 1) {
				if(args[0].equalsIgnoreCase("sair")) {
					Arena a = DescidaMortal.getArenaManager().getPlayerArena(p);
					if(a!=null && a.hasPlayer(p)) {
						for(Player o : a.getPlayers()) {
							  if(o!=p) {
							  o.sendMessage(saiu.replaceAll("@player", p.getName()).replaceAll("@num", "" + (a.getPlayersSize()-1)).replaceAll("@max", "" + max_players));
							  }
							}
							a.removePlayer(p);
							a.updateSigns();
							p.sendMessage(abandonou);
					} else {
						p.sendMessage(nenhuma_partida);
					}
				} else {
					if(p.hasPermission("dm.admin")) {
					usage(p, cmd);
					} else {
						p.sendMessage(" ");
						p.sendMessage("§eUso correto: §7/dm sair");
						p.sendMessage(" ");
					}
				}
			} else if(args.length >= 3) {
				if(args[0].equalsIgnoreCase("set")) {
					if(!(p.hasPermission("dm.admin"))) {
						p.sendMessage(cm.getConfigMessage("no-permission"));
						return true;
					}
					String arena = args[1].toLowerCase();
					if(args[2].equalsIgnoreCase("ll") || args[2].equalsIgnoreCase("tl") || args[2].equalsIgnoreCase("el")) {
						String s = File.separator;
						File f = new File(DescidaMortal.getPlugin().getDataFolder() + s + "Arenas" + s, arena + ".yml");
						if(!(f.exists())) {
							try {
								f.createNewFile();
								p.sendMessage("§3[Descida Mortal] §6A arena §f" + arena + " §6nao existia. Criada com sucesso.");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					FileConfiguration fc = YamlConfiguration.loadConfiguration(f);	
					if(args[2].equalsIgnoreCase("ll")) {
						fc.set("lobby-location", p.getLocation().getWorld().getName() + ";" + p.getLocation().getBlockX() + ";" + p.getLocation().getBlockY() + ";" + p.getLocation().getBlockZ() + ";" + p.getLocation().getPitch() + ";" + p.getLocation().getYaw());
						try {
							fc.save(f);
						} catch (IOException e) {
							e.printStackTrace();
						}
						p.sendMessage("§3[Descida Mortal] §fLobby §ada arena §f" + arena + " §adefinido com sucesso.");
					} else if(args[2].equalsIgnoreCase("tl")) {
						fc.set("teleport-location", p.getLocation().getWorld().getName() + ";" + p.getLocation().getBlockX() + ";" + p.getLocation().getBlockY() + ";" + p.getLocation().getBlockZ() + ";" + p.getLocation().getPitch() + ";" + p.getLocation().getYaw());
						try {
							fc.save(f);
						} catch (IOException e) {
							e.printStackTrace();
						}
						p.sendMessage("§3[Descida Mortal] §fInicio §ada arena §f" + arena + " §adefinido com sucesso.");
					} else if(args[2].equalsIgnoreCase("el")) {
						fc.set("exit-location", p.getLocation().getWorld().getName() + ";" + p.getLocation().getBlockX() + ";" + p.getLocation().getBlockY() + ";" + p.getLocation().getBlockZ() + ";" + p.getLocation().getPitch() + ";" + p.getLocation().getYaw());
						try {
							fc.save(f);
						} catch (IOException e) {
							e.printStackTrace();
						}
						p.sendMessage("§3[Descida Mortal] §fSaida §ada arena §f" + arena + " §adefinido com sucesso.");
					}
					if(DescidaMortal.isValidArena(arena)) {
						DescidaMortal.reloadArenas();
					}
				} else {
					usage(p, cmd);
				}
			} else {
			   usage(p, cmd);
			 }
			}
		} 
		return false;
	}
	
	public void usage(Player p, String cmd) {
		switch(cmd) {
		case "dm":
			p.sendMessage(" ");
			p.sendMessage("§e======= Uso Correto =======");
			p.sendMessage(" §7/dm set §f<arena> §7<ll, tl, el>");
			p.sendMessage(" §bll §7= Lobby §btl §7= Inicio §bel §7= Saida");
			p.sendMessage("§e=========================");
			p.sendMessage(" ");
			break;
		}
	}

}
