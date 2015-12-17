package descidamortal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Kit {
 
 private String id;
 private String permission;
 private ItemStack icon;
 private ItemStack[] items;
 private ItemStack[] equipment;
 private boolean retrievable;
 private double health;
 
 @SuppressWarnings("deprecation")
public Kit(String id) {
  this.id = id;
  items = new ItemStack[]{};
  equipment = new ItemStack[]{null, null, null, null};
  String n = File.separator;
  FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(DescidaMortal.getPlugin().getDataFolder() + n + "Kits" + n + id + ".yml"));
  permission = fc.getString("permission");
  retrievable = fc.getBoolean("retrievable");
  health = fc.getDouble("health");
  icon = new ItemStack(fc.getInt("icon"));
  ItemMeta meta = icon.getItemMeta();
  meta.setDisplayName(fc.getString("title").replaceAll("&", "§"));
  ArrayList<String> lore = new ArrayList<>();
  for(String s : fc.getStringList("lore")) {
  lore.add(s.replaceAll("&", "§"));
  }
  meta.setLore(lore);
  icon.setItemMeta(meta);
  for(String s : fc.getStringList("items")) {
  String[] args = s.split(" ");
  ItemStack item = null;
  if(args[0].contains(":")) {
  String[] x = args[0].split(":");
  item = new ItemStack(Integer.valueOf(x[0]), Integer.valueOf(args[1]), (short)Integer.valueOf(x[1]).intValue());
  } else {
  item = new ItemStack(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
   }
   items = Arrays.copyOf(items, items.length + 1);
   items[items.length - 1] = item;
  }
  ItemStack boots = new ItemStack(fc.getInt("equipment.boots.id"));
  List<String> en1 = fc.getStringList("equipment.boots.enchants");
  if(en1!=null && !en1.isEmpty()) {
	  for(String s : en1) {
		  String[] args = s.split(" ");
		  boots.addEnchantment(Enchantment.getById(Integer.valueOf(args[0])), Integer.valueOf(args[1]));
	  }
  }
  ItemStack chestplate = new ItemStack(fc.getInt("equipment.chestplate.id"));
  List<String> en2 = fc.getStringList("equipment.chestplate.enchants");
  if(en2!=null && !en2.isEmpty()) {
	  for(String s : en2) {
		  String[] args = s.split(" ");
		  chestplate.addEnchantment(Enchantment.getById(Integer.valueOf(args[0])), Integer.valueOf(args[1]));
	  }
  }
  ItemStack leggings = new ItemStack(fc.getInt("equipment.leggings.id"));
  List<String> en3 = fc.getStringList("equipment.leggings.enchants");
  if(en3!=null && !en3.isEmpty()) {
	  for(String s : en3) {
		  String[] args = s.split(" ");
		  leggings.addEnchantment(Enchantment.getById(Integer.valueOf(args[0])), Integer.valueOf(args[1]));
	  }
  }
  ItemStack helmet = new ItemStack(fc.getInt("equipment.helmet.id"));
  List<String> en4 = fc.getStringList("equipment.helmet.enchants");
  if(en4!=null && !en4.isEmpty()) {
	  for(String s : en4) {
		  String[] args = s.split(" ");
		  helmet.addEnchantment(Enchantment.getById(Integer.valueOf(args[0])), Integer.valueOf(args[1]));
	  }
  }
  equipment[0] = boots;
  equipment[1] = leggings;
  equipment[2] = chestplate;
  equipment[3] = helmet;
 }
 
 public String getID() {
	return id;
  }
 
 public String getPermission() {
	return permission;
}
 
public ItemStack[] getEquipment() {
 return equipment;
 }
 
 public ItemStack[] getItems() {
 return items;
 }
 
 public ItemStack getIcon() {
 return icon;
 }
 
 public boolean isRetrievable() {
 return retrievable;
 }
 
 public double getHealth() {
	 return health;
 }
 
}
