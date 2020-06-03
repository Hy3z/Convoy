package fr.convoyteam.convoy;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.convoyteam.convoy.enums.InterfaceType;

public class InterfaceManager implements Listener {
	private final WeakHashMap<Player,Inventory> playersInMenu = new WeakHashMap<Player,Inventory>();
	private final WeakHashMap<Player,InterfaceType> playersInterfaceType = new WeakHashMap<Player,InterfaceType>();
	private final ArrayList<Player> playersReady = new ArrayList<Player>();
	ItemStack settingItem;
	Main mainref;
	public InterfaceManager(Main _mainref) {
		mainref=_mainref;
		createSettingItem();
	}
	
	private void createSettingItem() {
		ItemStack item = new ItemStack(Material.ANVIL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA+"Convoy");
		ArrayList<String> lore= new ArrayList<String>();
		lore.add(ChatColor.LIGHT_PURPLE+"Drop/RightClick to manage the game");
		meta.setLore(lore);
		item.setItemMeta(meta);
		settingItem=item;
	}
	
	public void setSettingItemInInventory(Player player) {
		player.getInventory().setItem(0, settingItem);
	}
	
	private ItemStack makeItem(boolean isSelected,Material material, String itemName, String... lore) {
		ItemStack item=new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(itemName);
		List<String> listLore=new ArrayList<String>();
		if(isSelected) {
			listLore.add(ChatColor.DARK_GREEN+"\n"+ChatColor.UNDERLINE+"SELECTED");
			meta.addEnchant(Enchantment.DURABILITY, 3, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		for (String i : lore) {
			listLore.add(i);
		}
		meta.setLore(listLore);
		item.setItemMeta(meta);
		return item;
	}
	
	public void closeInterface(Player player) {
		player.closeInventory();
		playersInMenu.remove(player);
		playersInterfaceType.remove(player);
	}
	@EventHandler
	private void onPlayerChangeSlot(PlayerItemHeldEvent event) {
		if(!mainref.isStarted()) {
			Player player = event.getPlayer();
			if(mainref.getInGamePlayers().contains(player)) {
				setSettingItemInInventory(player);
			}	
		}
	}
	@EventHandler
	private void onInventoryClick(InventoryClickEvent event) {
		ItemStack itemClicked = event.getCurrentItem();
		if(itemClicked!=null) {
			Player player = (Player)event.getWhoClicked();
			if(playersInMenu.containsKey(player)) {
				if(event.getInventory().equals(playersInMenu.get(player))) {
					event.setCancelled(true);
					Inventory inventory = playersInMenu.get(player);
					switch(playersInterfaceType.get(player)) {
					case MAIN:
						switch(itemClicked.getType()) {
						case SUNFLOWER:
							if(!mainref.isStarted()) {
								if(mainref.getInGamePlayers().size()==playersReady.size()){
									mainref.startGame();
									closeInterface(player);
									return;
								}
								player.sendMessage(ChatColor.RED+"Everyone isn't ready!");
								return;
							}
							player.sendMessage(ChatColor.RED+"Game is already running!");
							return;
						case EMERALD_BLOCK:
							inventory.setItem(11, makeItem(false, Material.REDSTONE_BLOCK, ChatColor.RED+"Not Ready", ChatColor.LIGHT_PURPLE+"Click here to ready"));
							playersReady.remove(player);
							return;
						case REDSTONE_BLOCK:
							inventory.setItem(11, makeItem(false, Material.EMERALD_BLOCK, ChatColor.GREEN+"Ready", ChatColor.LIGHT_PURPLE+"Click here to un-ready"));
							playersReady.add(player);
							return;
						case PAPER:
							updateInterface(player, InterfaceType.MAP);
							player.openInventory(playersInMenu.get(player));
							return;
						case IRON_HORSE_ARMOR:
							updateInterface(player, InterfaceType.LOADOUT);
							player.openInventory(playersInMenu.get(player));
							return;
						case BLUE_WOOL:
							inventory.setItem(17, makeItem(false, Material.RED_WOOL, ChatColor.RED+"Pushers", ChatColor.LIGHT_PURPLE+"Click to change team"));
							//METTRE LE MEC PUSHER
							return;
						case RED_WOOL:
							inventory.setItem(17, makeItem(false, Material.BLUE_WOOL, ChatColor.AQUA+"Defenders", ChatColor.LIGHT_PURPLE+"Click to change team"));
							//METTRE LE MEC DEFENDER
							return;
						default:
							return;
						}
					case LOADOUT:
						switch(itemClicked.getType()) {
						case ANVIL:
							updateInterface(player, InterfaceType.MAIN);
							player.openInventory(playersInMenu.get(player));
							return;
						default:
							return;
						}
					case MAP:
						switch(itemClicked.getType()) {
						case PAPER:
							//CHANGER LA MAP CHOISIE
							updateInterface(player, InterfaceType.MAP);
							player.openInventory(playersInMenu.get(player));
							return;
						case ANVIL:
							updateInterface(player, InterfaceType.MAIN);
							player.openInventory(playersInMenu.get(player));
							return;
						default:
							return;
						}
					}
				}
			}
		}
	}
	private void updateInterface(Player player, InterfaceType interfaceType) {
		if(!playersInMenu.containsKey(player)) {
			playersInMenu.put(player, Bukkit.createInventory(null, 27, ChatColor.GOLD+"Convoy"));
			playersInterfaceType.put(player, interfaceType);
		}
		Inventory inventory = playersInMenu.get(player);
		switch(interfaceType) {
		case MAIN:
			inventory = Bukkit.createInventory(null, 27, ChatColor.GOLD+"Convoy");
			inventory = createMainInterface(player, inventory);
			break;
		case LOADOUT:
			inventory = Bukkit.createInventory(null, 54, ChatColor.GOLD+"Loadout");
			inventory = createLoadoutInterface(player, inventory);
			break;
		case MAP:
			inventory = Bukkit.createInventory(null, 27, ChatColor.GOLD+"Maps");
			inventory = createMapInterface(player, inventory);
		}
		setPlayerInHashMap(player, inventory, interfaceType);
	}
	
	private Inventory createMainInterface(Player player, Inventory mainMenu) {
		mainMenu.clear();
		mainMenu.setItem(9, makeItem(false, Material.SUNFLOWER, ChatColor.GOLD+"Launch Game", ChatColor.LIGHT_PURPLE+"Click to launch the game"));
		if(playersReady.contains(player)) {
			mainMenu.setItem(11, makeItem(false, Material.EMERALD_BLOCK, ChatColor.GREEN+"Ready", ChatColor.LIGHT_PURPLE+"Click here to un-ready"));
		}else {
			mainMenu.setItem(11, makeItem(false, Material.REDSTONE_BLOCK, ChatColor.RED+"Not Ready", ChatColor.LIGHT_PURPLE+"Click here to get ready"));
		}
		mainMenu.setItem(13, makeItem(false, Material.PAPER, ChatColor.GOLD+"Maps", ChatColor.LIGHT_PURPLE+"Click here to change game's map"));
		mainMenu.setItem(15, makeItem(false, Material.IRON_HORSE_ARMOR, ChatColor.GOLD+"Loadout", ChatColor.LIGHT_PURPLE+"Click here to change your loadout"));
		if(mainref.getPushers().contains(player)) {
			mainMenu.setItem(17, makeItem(false, Material.RED_WOOL, ChatColor.RED+"Pushers", ChatColor.LIGHT_PURPLE+"Click to change team"));
		}else {
			mainMenu.setItem(17, makeItem(false, Material.BLUE_WOOL, ChatColor.AQUA+"Defenders", ChatColor.LIGHT_PURPLE+"Click to change team"));
		}
		return mainMenu;
	}
	
	private Inventory createLoadoutInterface(Player player, Inventory loadoutMenu) {
		loadoutMenu.clear();
		ItemStack backItem = new ItemStack(Material.ANVIL);
		ItemMeta meta = backItem.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED+"Back");
		backItem.setItemMeta(meta);
		loadoutMenu.setItem(8, backItem);
		return loadoutMenu;
	}
	
	private Inventory createMapInterface(Player player, Inventory mapMenu) {
		mapMenu.clear();
		ItemStack backItem = new ItemStack(Material.ANVIL);
		ItemMeta meta = backItem.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED+"Back");
		backItem.setItemMeta(meta);
		mapMenu.setItem(0, backItem);
		ArrayList<String> mapList = mainref.getConfigReader().getMapList();
		for(byte index=0;index<mapList.size();index++) {
			ItemStack mapItem = new ItemStack(Material.PAPER);
			ItemMeta mapMeta = mapItem.getItemMeta();
			mapMeta.setDisplayName(mapList.get(index));
			/*
			if (mapList.get(index) est la map actuelle) {
				mapMeta.addEnchant(Enchantment.MENDING, 1, true);
				mapMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}*/
			mapItem.setItemMeta(mapMeta);
			mapMenu.setItem(index+1, mapItem);
		}
		return mapMenu;
	}
	
	private void setPlayerInHashMap(Player player, Inventory playerInventory, InterfaceType interfaceType) {
		playersInMenu.replace(player, playerInventory);
		playersInterfaceType.replace(player, interfaceType);
	}
	
	@EventHandler
	private void onPlayerDrop(PlayerDropItemEvent event) {
		Item itemDrop = event.getItemDrop();
		if(itemDrop!=null) {
			if(itemDrop.getItemStack().equals(settingItem)) {
				event.setCancelled(true);
				Player player = event.getPlayer();
				updateInterface(player, InterfaceType.MAIN);
				player.openInventory(playersInMenu.get(player));
			}
		}
	}
	
	@EventHandler
	private void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		ItemStack item = event.getItem();
		if(item!=null) {
			if(action.equals(Action.RIGHT_CLICK_AIR)||action.equals(Action.RIGHT_CLICK_BLOCK)) {
				if(item.equals(settingItem)) {
					event.setCancelled(true);
					Player player = event.getPlayer();
					updateInterface(player, InterfaceType.MAIN);
					player.openInventory(playersInMenu.get(player));
				}
			}
		}
	}
}