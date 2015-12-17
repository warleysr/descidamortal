package descidamortal;

import java.io.File;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DescidaMortal extends JavaPlugin {
	
	private static Plugin plugin;
	private static ArenaManager am = null;
	private static KitManager km = null;
	private static boolean vault = false;
	private static Economy economy = null;
	
	@Override
	public void onEnable() {
		plugin = this;
		setupFiles();
		setExecutor("dm", new SetupCommand());
		am = new ArenaManager();
		km = new KitManager();
		registerEvents(getPlugin(), new ArenaListener(), new SignListener(), new KitListener());
		setupEconomy();
		if(Bukkit.getPluginManager().getPlugin("Vault")!=null) {
			vault=true;
		}
	}
	
	@Override
	public void onDisable() {
		plugin = null;
		for(Arena a : getArenaManager().getArenas()) {
			a.disable();
		}
	}
	
	public static Plugin getPlugin() {
		return plugin;
	}
	
	public void setExecutor(String cmd, CommandExecutor exe) {
		getCommand(cmd).setExecutor(exe);
	}
	
	public void registerEvents(org.bukkit.plugin.Plugin plugin, Listener... listeners) {
		for(Listener listener : listeners) {
			Bukkit.getPluginManager().registerEvents(listener, plugin);
		}
	}
	
	private void setupFiles() {
		String s = File.separator;
		saveDefaultConfig();
		reloadConfig();
		File dir = new File(getPlugin().getDataFolder() + s + "Arenas" + s);
		File dir2 = new File(getPlugin().getDataFolder() + s + "Kits" + s);
		if(!(dir.exists())) {
			dir.mkdir();
		}
		if(!(dir2.exists())) {
			dir2.mkdir();
		}
	}

	public static ArenaManager getArenaManager() {
		return am;
	}
	
	public static KitManager getKitManager() {
		return km;
	}
	
	public static void reloadArenas() {
		am = new ArenaManager();
	}
	
	public static boolean  isValidArena(String id) {
		String s = File.separator;
		FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(getPlugin().getDataFolder() + s + "Arenas" + s + id + ".yml"));
		if(fc.getString("lobby-location")!=null && fc.getString("teleport-location")!=null && fc.getString("exit-location")!=null) {
			return true;
		}
		return false;
	}
	
	public static boolean usingVault() {
		return vault;
	}
	
	private boolean setupEconomy() {
	     if (getServer().getPluginManager().getPlugin("Vault") == null) {
	      return false;
	      }
	      RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	     if (rsp == null) {
	        return false;
	     }
	     economy = rsp.getProvider();
	     return economy != null;
	}
	
	public static void premiar(Player p) {
		economy.depositPlayer(p.getName(), getPlugin().getConfig().getDouble("premio"));
	}
	
}
