package descidamortal;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	
	private FileConfiguration fc = null;
	private String id = null;
	
	public ConfigManager() {}
	public ConfigManager(Arena a) {
		this.id = a.getID();
		String s = File.separator;
		fc = YamlConfiguration.loadConfiguration(new File(DescidaMortal.getPlugin().getDataFolder() + s + "Arenas" + s + id + ".yml"));
	}
	
	public int getInt(String path) {
		return DescidaMortal.getPlugin().getConfig().getInt(path);
	}
	
	public String getConfigMessage(String path) {
		return DescidaMortal.getPlugin().getConfig().getString(path).replaceAll("&", "§");
	}
	
	public FileConfiguration getConfig() {
		return DescidaMortal.getPlugin().getConfig();
	}
	
	public FileConfiguration getArenaConfig() {
		return fc;
	}
	
	public void reload() {
		String s = File.separator;
		fc = YamlConfiguration.loadConfiguration(new File(DescidaMortal.getPlugin().getDataFolder() + s + "Arenas" + s + id + ".yml"));
	}
	
	public void saveArenaConfig() {
		String s = File.separator;
		try {
			fc.save(new File(DescidaMortal.getPlugin().getDataFolder() + s + "Arenas" + s + id + ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Location getSplitLocation(FileConfiguration fc, String path) {
		String[] args = fc.getString(path).split(";");
		Location loc = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
		loc.setPitch(Float.valueOf(args[4]));
		loc.setYaw(Float.valueOf(args[5]));
		return loc;
	}

}
