package codecrafter47.multiworld.manager;

import PluginReference.*;
import WrapperObjects.Entities.PlayerWrapper;
import WrapperObjects.ItemStackWrapper;
import codecrafter47.multiworld.PluginMultiWorld;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import joebkt.Item;
import joebkt.ItemStack;
import joebkt.NBTTagCompound;
import joebkt.StringParser;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by florian on 13.12.14.
 */
public class MultiInventoryManager {

	private PluginMultiWorld plugin;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private MultiInventoryConfig config = new MultiInventoryConfig();

	private Map<UUID, String> lastWorld = new HashMap<>();

	public MultiInventoryManager(PluginMultiWorld plugin) {
		this.plugin = plugin;
		File store = new File(plugin.getDataFolder(), "inventories.json");
		if(store.exists()){
			try {
				config = gson.fromJson(new FileReader(store), MultiInventoryConfig.class);
			}
			catch (Throwable e) {
				plugin.getLogger().warn("Failed to load inventories.json", e);
			}
		}
	}

	public void saveData() {
		File store = new File(plugin.getDataFolder(), "inventories.json");
		store.getParentFile().mkdirs();
		if(store.exists())store.delete();
		try {
			FileWriter writer = new FileWriter(store);
			gson.toJson(config, writer);
			writer.flush();
			writer.close();
		}
		catch (Throwable e) {
			plugin.getLogger().warn("Failed to save inventories.json", e);
		}
	}

	public List<String> getGroups(){
		return new ArrayList<>(config.inv.keySet());
	}

	public void setGroupForWorld(MC_World world, String group){
		config.inv.get(getWhereForWorld(world)).remove(world.getName());
		config.inv.get(group).add(world.getName());
		saveData();
	}

	public void addGroup(String name){
		config.inv.put(name, new ArrayList<String>());
		saveData();
	}

	public void deleteGroup(String name){
		config.inv.remove(name);
	}

	public void checkWorldChange() {
		for(MC_Player player: plugin.getServer().getPlayers()){
			try {
				if (!player.getWorld().getName().equals(lastWorld.get(player.getUUID()))) {
					// player changed world
					player.setGameMode(MC_GameMode.valueOf(plugin.getStorageManager().getCustomConfig(player.getWorld().getDimension()).getGameMode().name()));
					// save old inv
					if (!getWhereForWorld(getWorldByName(lastWorld.get(player.getUUID()))).equals(getWhereForPlayer(player))) {
						saveInventory(player, getWhereForWorld(getWorldByName(lastWorld.get(player.getUUID()))));
						lastWorld.put(player.getUUID(), player.getWorld().getName());
						loadInventory(player);
					} else {
						lastWorld.put(player.getUUID(), player.getWorld().getName());
					}
				}
			} catch (Exception ex){
				plugin.getLogger().error("Failed to update inventory for " + player.getName(), ex);
			}
		}
	}

	public void onPlayerJoin(MC_Player plr) {
		lastWorld.put(plr.getUUID(), plr.getWorld().getName());
		plr.setGameMode(MC_GameMode.valueOf(plugin.getStorageManager().getCustomConfig(plr.getWorld().getDimension()).getGameMode().name()));
	}

	public void onPlayerDisconnect(UUID uuid) {
		lastWorld.remove(uuid);
	}

	private static class MultiInventoryConfig{
		Map<String, List<String>> inv = new HashMap<>();
		{
			inv.put("default", Arrays.asList("world", "world_nether", "world_end"));
			inv.put("creative", Arrays.asList("creative", "creative_nether", "creative_end"));
		}
	}

	@SneakyThrows
	private void saveInventory(MC_Player player, String where){
		File file = new File(plugin.getDataFolder() + File.separator + "inventory" + File.separator + where, player.getUUID().toString() + ".json");
		file.getParentFile().mkdirs();
		if(file.exists())file.delete();
		file.createNewFile();
		try {
			FileWriter writer = new FileWriter(file);
			gson.toJson(playerToPlayerData(player), writer);
			writer.flush();
			writer.close();
		}
		catch (Throwable e) {
			plugin.getLogger().warn("Failed to save inventory for " + player.getName(), e);
		}
	}

	@SneakyThrows
	private void loadInventory(MC_Player player){
		File file = new File(plugin.getDataFolder() + File.separator + "inventory" + File.separator + getWhereForPlayer(player), player.getUUID().toString() + ".json");
		if(!file.exists()){
			// clear the inventory
			player.setArmor(new ArrayList<>(Arrays.<MC_ItemStack>asList(null, null, null, null)));
			player.setInventory(new ArrayList<>(Arrays.<MC_ItemStack>asList(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)));
			for(int i = 0; i < 27; i++)((PlayerWrapper)player).plr.getEnderChest().setItemAtIdx(i, null);
			for(int i = 0; i < 54; i++)((PlayerWrapper)player).plr.backpack.setItemAtIdx(i, null);
			player.updateInventory();
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setExp(0);
			player.setLevel(0);
			player.setPotionEffects(new ArrayList<MC_PotionEffect>());
			player.setFireTicks(0);
		} else {
			FileReader reader = new FileReader(file);
			applyPlayerDataToPlayer(gson.fromJson(reader, PlayerData.class), player);
			reader.close();
		}
	}

	public String getWhereForWorld(MC_World world){
		for(Map.Entry<String, List<String>> entry: config.inv.entrySet()){
			if(entry.getValue().contains(world.getName()))return entry.getKey();
		}
		return "default";
	}

	private String getWhereForPlayer(MC_Player player){
		return getWhereForWorld(player.getWorld());
	}

	@Data
	public static class PlayerData{
		String armor[];
		String inventory[];
		String enderchest[];
		String backpack[];
		float health;
		int foodLevel;
		float exp;
		int level;
		ArrayList<MC_PotionEffect> potionEffects;
		int fireTicks;
	}

	private PlayerData playerToPlayerData(MC_Player player){
		PlayerData playerData = new PlayerData();
		String[] armor = new String[4];
		List<MC_ItemStack> armor1 = player.getArmor();
		for (int i = 0; i < armor1.size(); i++) {
			MC_ItemStack itemStack = armor1.get(i);
			armor[i] = itemstackToString(itemStack);
		}
		playerData.setArmor(armor);
		String[] inv = new String[36];
		List<MC_ItemStack> inv1 = player.getInventory();
		for (int i = 0; i < inv1.size(); i++) {
			MC_ItemStack itemStack = inv1.get(i);
			inv[i] = itemstackToString(itemStack);
		}
		playerData.setInventory(inv);
		String[] ec = new String[27];
		for(int i = 0; i < 27; i++){
			ec[i] = itemstackToString(new ItemStackWrapper(((PlayerWrapper)player).plr.getEnderChest().getItemAtIdx(i)));
		}
		playerData.setEnderchest(ec);
		String[] bp = new String[54];
		for(int i = 0; i < 54; i++){
			bp[i] = itemstackToString(new ItemStackWrapper(((PlayerWrapper)player).plr.backpack.getItemAtIdx(i)));
		}
		playerData.setBackpack(bp);
		playerData.setHealth(player.getHealth());
		playerData.setFoodLevel(player.getFoodLevel());
		playerData.setExp(player.getExp());
		playerData.setLevel(player.getLevel());
		playerData.setPotionEffects((ArrayList<MC_PotionEffect>) player.getPotionEffects());
		playerData.setFireTicks(player.getFireTicks());
		return playerData;
	}

	private void applyPlayerDataToPlayer(PlayerData playerData, MC_Player player){
		ArrayList<MC_ItemStack> armor = new ArrayList<>();
		for (String s : playerData.getArmor()) {
			armor.add(stringToItemStack(s));
		}
		player.setArmor(armor);
		ArrayList<MC_ItemStack> inv = new ArrayList<>();
		for (String s : playerData.getInventory()) {
			inv.add(stringToItemStack(s));
		}
		player.setInventory(inv);
		for(int i = 0; i < 27; i++){
			MC_ItemStack mc_itemStack = stringToItemStack(playerData.getEnderchest()[i]);
			((PlayerWrapper)player).plr.getEnderChest().setItemAtIdx(i, mc_itemStack != null?((ItemStackWrapper)mc_itemStack).is:null);
		}
		for(int i = 0; i < 54; i++){
			MC_ItemStack mc_itemStack = stringToItemStack(playerData.getBackpack()[i]);
			((PlayerWrapper)player).plr.backpack.setItemAtIdx(i, mc_itemStack != null ? ((ItemStackWrapper) mc_itemStack).is : null);
		}
		player.updateInventory();
		player.setHealth(playerData.getHealth());
		player.setFoodLevel(playerData.getFoodLevel());
		player.setExp(playerData.getExp());
		player.setLevel(playerData.getLevel());
		player.setPotionEffects(playerData.getPotionEffects());
		player.setFireTicks(playerData.getFireTicks());
	}

	private String itemstackToString(MC_ItemStack itemStack){
		if(itemStack == null)return "";
		ItemStack stack = ((ItemStackWrapper)itemStack).is;
		if(stack == null)return "";
		return stack.serializeIntoNBTTagCompound(new NBTTagCompound()).toString();
	}

	@SneakyThrows
	private MC_ItemStack stringToItemStack(String str){
		if(str.isEmpty())return null;
		ItemStack itemStack = new ItemStack(Item.getItemFromID(1));
		itemStack.deserializeFromNBTTagCompound(StringParser.a(str));
		return new ItemStackWrapper(itemStack);
	}

	private MC_World getWorldByName(String name){
		for(MC_World world: plugin.getServer().getWorlds()){
			if(world.getName().equals(name))return world;
		}
		throw new RuntimeException("World " + name + " does not exist!");
	}
}
