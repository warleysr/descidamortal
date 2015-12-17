package descidamortal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenaManager {
	
	private List<Arena> arenas = new ArrayList<>();
	private static ConfigManager cm = new ConfigManager();
	private ItemStack[] default_equipment;
	private ItemStack[] default_items;
	private static ItemStack kit_selector;
	private static ItemStack exit_arena; 
	
	@SuppressWarnings("deprecation")
	public ArenaManager() {
		String n = File.separator;
		File dir = new File(DescidaMortal.getPlugin().getDataFolder() + n + "Arenas" + n);
		for(File f : dir.listFiles()) {
			if(DescidaMortal.isValidArena(f.getName().replaceAll(".yml", ""))) {
			FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
			Arena a = new Arena(f.getName().replaceAll(".yml", ""), ConfigManager.getSplitLocation(fc, "lobby-location"), ConfigManager.getSplitLocation(fc, "teleport-location"), ConfigManager.getSplitLocation(fc, "exit-location"));
			arenas.add(a);
		 }
		}
		default_equipment = new ItemStack[]{null, null, null, null};
		ItemStack boots = new ItemStack(cm.getConfig().getInt("equipment.boots.id"));
		  List<String> en1 = cm.getConfig().getStringList("equipment.boots.enchants");
		  if(en1!=null && !en1.isEmpty()) {
			  for(String s : en1) {
				  String[] args = s.split(" ");
				  boots.addEnchantment(Enchantment.getById(Integer.valueOf(args[0])), Integer.valueOf(args[1]));
			  }
		  }
		  ItemStack chestplate = new ItemStack(cm.getConfig().getInt("equipment.chestplate.id"));
		  List<String> en2 = cm.getConfig().getStringList("equipment.chestplate.enchants");
		  if(en2!=null && !en2.isEmpty()) {
			  for(String s : en2) {
				  String[] args = s.split(" ");
				  chestplate.addEnchantment(Enchantment.getById(Integer.valueOf(args[0])), Integer.valueOf(args[1]));
			  }
		  }
		  ItemStack leggings = new ItemStack(cm.getConfig().getInt("equipment.leggings.id"));
		  List<String> en3 = cm.getConfig().getStringList("equipment.leggings.enchants");
		  if(en3!=null && !en3.isEmpty()) {
			  for(String s : en3) {
				  String[] args = s.split(" ");
				  leggings.addEnchantment(Enchantment.getById(Integer.valueOf(args[0])), Integer.valueOf(args[1]));
			  }
		  }
		  ItemStack helmet = new ItemStack(cm.getConfig().getInt("equipment.helmet.id"));
		  List<String> en4 = cm.getConfig().getStringList("equipment.helmet.enchants");
		  if(en4!=null && !en4.isEmpty()) {
			  for(String s : en4) {
				  String[] args = s.split(" ");
				  helmet.addEnchantment(Enchantment.getById(Integer.valueOf(args[0])), Integer.valueOf(args[1]));
			  }
		  }
		  default_equipment[0] = boots;
		  default_equipment[1] = leggings;
		  default_equipment[2] = chestplate;
		  default_equipment[3] = helmet;
		
		List<String> list2 = cm.getConfig().getStringList("items");
		default_items = new ItemStack[]{};
		for(int i=0; i<list2.size(); i++) {
			String[] args = list2.get(i).split(" ");
			default_items = Arrays.copyOf(default_items, default_items.length + 1);
			if(args[0].contains(":")) {
				String[] args2 = args[0].split(":");
				default_items[default_items.length - 1] = new ItemStack(Integer.valueOf(args2[0]), Integer.valueOf(args[1]), (short)Integer.valueOf(args2[1]).intValue());
			} else {
				default_items[default_items.length - 1] = new ItemStack(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
			}
		}
		kit_selector = new ItemStack(cm.getInt("kit-selector.id"));
		ItemMeta meta1 = kit_selector.getItemMeta();
		meta1.setDisplayName(cm.getConfigMessage("kit-selector.title"));
		ArrayList<String> lore1 = new ArrayList<>();
		for(String s : cm.getConfig().getStringList("kit-selector.lore")) {
			lore1.add(s.replaceAll("&", "§"));
		}
		meta1.setLore(lore1);
		kit_selector.setItemMeta(meta1);
		
		exit_arena = new ItemStack(cm.getInt("exit-arena.id"));
		ItemMeta meta2 = exit_arena.getItemMeta();
		meta2.setDisplayName(cm.getConfigMessage("exit-arena.title"));
		ArrayList<String> lore2 = new ArrayList<>();
		for(String s : cm.getConfig().getStringList("exit-arena.lore")) {
			lore2.add(s.replaceAll("&", "§"));
		}
		meta2.setLore(lore2);
		exit_arena.setItemMeta(meta2);
	}
	
	public Arena getArena(String id) {
		Arena arena = null;
		for(Arena a : getArenas()) {
			if(a.getID().equals(id)) {
				arena = a;
				break;
			}
		}
		return arena;
	}
	
	public Arena getPlayerArena(Player p) {
		Arena arena = null;
		for(Arena a : getArenas()) {
			if(a.hasPlayer(p)) {
				arena = a;
				break;
			}
		}
		return arena;
	}
	
	public Arena[] getArenas() {
		Arena[] all = {};
		for(int i=0; i<arenas.size(); i++) {
			Arena arena = arenas.get(i);
			all = Arrays.copyOf(all, all.length + 1);
			all[all.length - 1] = arena;
		}
		return all;
	}
	
	public void equipPlayerWithDefaultItems(Player p) {
		p.getInventory().clear();
		p.getEquipment().setArmorContents(null);
		p.getInventory().addItem(default_items);
		p.getEquipment().setArmorContents(default_equipment);
		if(p.getMaxHealth()!=20) {
			p.setMaxHealth(20);
		}
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
	}
	
	public ItemStack getKitSelector() {
		return kit_selector;
	}

	public ItemStack getExitArena() {
		return exit_arena;
	}

}
