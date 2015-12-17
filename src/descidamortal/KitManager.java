package descidamortal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitManager {
	
	private List<Kit> kits = new ArrayList<>();
	private HashMap<String, Kit> using = new HashMap<>();
	
	public KitManager() {
		String s = File.separator;
		File dir = new File(DescidaMortal.getPlugin().getDataFolder() + s + "Kits" + s);
		for(File f : dir.listFiles()) {
			Kit k = new Kit(f.getName().replaceAll(".yml", ""));
			kits.add(k);
		} 
	}
	
	public Kit getKit(String id) {
		Kit k = null;
		for(int i=0; i<kits.size(); i++) {
			if(kits.get(i).getID().equals(id)) {
				k = kits.get(i);
				break;
			}
		}
		return k;
	}
	
	public Kit getPlayerKit(Player p) {
		Kit k = null;
		for(Entry<String, Kit> entry : using.entrySet()) {
			if(entry.getKey().equals(p.getName())) {
				k = entry.getValue();
				break;
			}
		}
		return k;
	}
	
	public Kit[] getKits() {
		Kit[] all = {};
		for(int i=0; i<kits.size(); i++) {
			all = Arrays.copyOf(all, all.length + 1);
			all[all.length - 1] = kits.get(i);
		}
		return all;
	}
	
	public void setKit(Player p, Kit k) {
		using.put(p.getName(), k);
	}
	
	public boolean hasKit(Player p) {
		if(using.containsKey(p.getName())) {
			return true;
		}
		return false;
	}
	
	public Kit getKitByIcon(ItemStack icon) {
		Kit k = null;
		for(int i=0; i<kits.size(); i++) {
			if(kits.get(i).getIcon().equals(icon)) {
				k = kits.get(i);
				break;
			}
		}
		return k;
	}
	
	public void resetKit(Player p) {
		if(hasKit(p)) {
			using.remove(p.getName());
		}
	}
	
	public void equiparKit(Player p) {
		Kit k = getPlayerKit(p);
		p.getEquipment().setArmorContents(k.getEquipment());
		p.getInventory().setContents(k.getItems());
		p.setMaxHealth(k.getHealth());
		p.setHealth(k.getHealth());
	}
	
}
