package fr.convoyteam.convoy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor,TabCompleter {
	private final Main mainref;
	private final ConfigReader configref;
	private ArrayList<String> gameCommands = new ArrayList<String>();
	private ArrayList<String> mapCommands = new ArrayList<String>();
	public CommandManager(Main main, ConfigReader config) {
		mainref=main;
		configref=config;
		
		gameCommands.add("start");
		gameCommands.add("join");
		gameCommands.add("ff");
		gameCommands.add("leave");
		gameCommands.add("restart");
		gameCommands.add("stop");
		gameCommands.add("map");
		
		mapCommands.add("create");
		mapCommands.add("list");
		mapCommands.add("rename");
		mapCommands.add("delete");
		mapCommands.add("setCartStart");
		mapCommands.add("setCartEnd");
		mapCommands.add("setPusherSpawn");
		mapCommands.add("setDefenderSpawn");
	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		switch(arg3[0]) {
		case "start":
			/*if(GameEnCours) {
			gameRunningError(sender);
			return false;
			}*/
			mainref.startGame();
			sender.sendMessage(ChatColor.GREEN+"Game started!");
			return true;
			
		case "join":
			/*if(GameEnCours) {
				gameRunningError(sender);
				return false;
			}*/
			Player toAdd;
			if(arg3.length>2) {
				toAdd=Bukkit.getPlayer(arg3[1]);
				if(toAdd==null) {
					unknownPlayerError(sender);
					return false;
				}
			}else {
				if(!(sender instanceof Player)) {
					notPlayerError(sender);
					return false;
				}
				toAdd=(Player)sender;
			}
			if(mainref.addPlayer(toAdd)) {
				sender.sendMessage(ChatColor.GREEN+"Player joined the game");
				return true;
			}
			sender.sendMessage("");
			sender.sendMessage(ChatColor.RED+"Player is already in game!");
			sender.sendMessage("");
			return false;
			
		case "ff":
			
		case "leave":
			Player toKick;
			if(arg3.length>2) {
				toKick=Bukkit.getPlayer(arg3[1]);
				if(toKick==null) {
					unknownPlayerError(sender);
					return false;
				}
			}else {
				if(!(sender instanceof Player)) {
					notPlayerError(sender);
					return false;
				}
				toKick=(Player)sender;
			}
			if(mainref.removePlayer(toKick)) {
				sender.sendMessage(ChatColor.GREEN+"Player left this game");
				return true;
			}
			sender.sendMessage("");
			sender.sendMessage(ChatColor.RED+"Player is not in this game!");
			sender.sendMessage("");
			return false;
			
		case "restart":
			/*if(GamePasEnCours) {
			gameNotRunningError(sender);
			return false;
			}*/
			mainref.stopGame();
			mainref.startGame();
			return true;
			
		case "stop":
			/*if(GamePasEnCours) {
			gameNotRunningError(sender);
			return false;
			}*/
			mainref.stopGame();
			sender.sendMessage(ChatColor.GREEN+"Game stopped!");
			
		case "map":
			if(arg3.length>1) {
				switch(arg3[1]) {
				case "create":
					if (arg3.length>2) {
						if(configref.createMapConfig(arg3[2])) {
							sender.sendMessage(ChatColor.GREEN+"Map created!");
							return true;
						}
						sender.sendMessage("");
						sender.sendMessage(ChatColor.RED+"This map already exist!");
						sender.sendMessage("");
						return false;
					}
					sender.sendMessage("");
					sender.sendMessage(ChatColor.BLUE+"Usage: "+ChatColor.WHITE+"/conv map create "+ChatColor.GOLD+"<MapName>");
					sender.sendMessage("");
					return false;
				
				case "list":
					sender.sendMessage("");
					sender.sendMessage(ChatColor.DARK_GREEN+"Maps:");
					for(String map : configref.getMapList()) {
						sender.sendMessage(ChatColor.AQUA+"- "+map);
					}
					sender.sendMessage("");
					return true;
					
				case "rename":
					if (arg3.length>3) {
						if (configref.renameMapConfig(arg3[2], arg3[3])) {
							sender.sendMessage(ChatColor.GOLD+arg3[2]+ChatColor.GREEN+" has been renamed to "+ChatColor.GOLD+arg3[3]+ChatColor.GREEN+" !");
							return true;
						}
						mapError(sender);
						return false;
					}
					sender.sendMessage("");
					sender.sendMessage(ChatColor.BLUE+"Usage: "+ChatColor.WHITE+"/conv map rename "+ChatColor.GOLD+"<MapName> <NewName>");
					sender.sendMessage("");
					return false;
					
				case "delete":
					if (arg3.length>2) {
						if(configref.deleteMapConfig(arg3[2])) {
							sender.sendMessage(ChatColor.GREEN+"Map deleted!");
							return true;
						}
						mapError(sender);
						return false;
					}
					sender.sendMessage("");
					sender.sendMessage(ChatColor.BLUE+"Usage: "+ChatColor.WHITE+"/conv map delete "+ChatColor.GOLD+"<MapName>");
					sender.sendMessage("");
					return false;
					
				case "setCartStart":
					if(arg3.length>2) {
						if (sender instanceof Player) {
							YamlConfiguration cfg = configref.getMapConfig(arg3[2]);
							if(cfg!=null) {
								//METTRE LE CART
								return true;
							}
							mapError(sender);
							return false;
						}
						notPlayerError(sender);
						return false;
					}
					sender.sendMessage("");
					sender.sendMessage(ChatColor.BLUE+"Usage: "+ChatColor.WHITE+"/conv map setCartStart "+ChatColor.GOLD+"<MapName>");
					sender.sendMessage(ChatColor.RED+"! "+ChatColor.BLUE+"Look at the rail you want the cart to spawn on "+ChatColor.RED+"!");
					sender.sendMessage("");
					return false;

				case "setCartEnd":
					if(arg3.length>2) {
						if (sender instanceof Player) {
							YamlConfiguration cfg = configref.getMapConfig(arg3[2]);
							if(cfg!=null) {
								//METTRE LE CART
								return true;
							}
							mapError(sender);
							return false;
						}
						notPlayerError(sender);
						return false;
					}
					sender.sendMessage("");
					sender.sendMessage(ChatColor.BLUE+"Usage: "+ChatColor.WHITE+"/conv map setCartEnd "+ChatColor.GOLD+"<MapName>");
					sender.sendMessage(ChatColor.RED+"! "+ChatColor.BLUE+"Look at the rail you want the cart to end on "+ChatColor.RED+"!");
					sender.sendMessage("");
					return false;
					
				case "setPusherSpawn":
					if(arg3.length>2) {
						if (sender instanceof Player) {
							YamlConfiguration cfg = configref.getMapConfig(arg3[2]);
							if(cfg!=null) {
								cfg.set("pusherSpawn", ((Player) sender).getLocation());
								saveConfig(arg3[2],cfg);
								sender.sendMessage(ChatColor.GREEN+"Pushers spawn location set!");
								return true;
							}
							mapError(sender);
							return false;
						}
						notPlayerError(sender);
						return false;
					}
					sender.sendMessage("");
					sender.sendMessage(ChatColor.BLUE+"Usage: "+ChatColor.WHITE+"/conv map setPusherSpawn "+ChatColor.GOLD+"<MapName>");
					sender.sendMessage(ChatColor.RED+"! "+ChatColor.BLUE+"Stand where the respawn point will be "+ChatColor.RED+"!");
					sender.sendMessage("");
					return false;
					
				case "setDefenderSpawn":
					if(arg3.length>2) {
						if (sender instanceof Player) {
							YamlConfiguration cfg = configref.getMapConfig(arg3[2]);
							if(cfg!=null) {
								cfg.set("defenderSpawn", ((Player) sender).getLocation());
								saveConfig(arg3[2],cfg);
								sender.sendMessage(ChatColor.GREEN+"Defenders spawn location set!");
								return true;
							}
							mapError(sender);
							return false;
						}
						notPlayerError(sender);
						return false;
					}
					sender.sendMessage("");
					sender.sendMessage(ChatColor.BLUE+"Usage: "+ChatColor.WHITE+"/conv map setDefenderSpawn "+ChatColor.GOLD+"<MapName>");
					sender.sendMessage(ChatColor.RED+"! "+ChatColor.BLUE+"Stand where the respawn point will be "+ChatColor.RED+"!");
					sender.sendMessage("");
					return false;
				}
			}
		}
		return false;
	}
	public void saveConfig(String mapName, YamlConfiguration cfg) {
		configref.saveMapConfig(mapName,cfg);
	}
	public void mapError(CommandSender sender) {
		sender.sendMessage("");
		sender.sendMessage(ChatColor.RED+"This map does not exist!");
		sender.sendMessage(ChatColor.GREEN+"Try: "+ChatColor.WHITE+"/conv map create "+ChatColor.GOLD+"<MapName>");
		sender.sendMessage("");
	}
	public void notPlayerError(CommandSender sender) {
		sender.sendMessage("");
		sender.sendMessage(ChatColor.RED+"You need to be a player in order to use this command");
		sender.sendMessage("");
	}
	public void unknownPlayerError(CommandSender sender) {
		sender.sendMessage("");
		sender.sendMessage(ChatColor.RED+"This player does not exist!");
		sender.sendMessage("");
	}
	public void gameRunningError(CommandSender sender) {
		sender.sendMessage("");
		sender.sendMessage(ChatColor.RED+"This game is already running!");
		sender.sendMessage("");
	}
	public void gameNotRunningError(CommandSender sender) {
		sender.sendMessage("");
		sender.sendMessage(ChatColor.RED+"This game is not running!");
		sender.sendMessage("");
	}
	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		if(arg3.length<2) {
			return returnFollowCMD(arg3, gameCommands, 0);
		}
		if(arg3[0].equals("map")) {
			return returnFollowCMD(arg3, mapCommands, 1);
		}
		return null;
	}
	public ArrayList<String> returnFollowCMD(String[] args, ArrayList<String> cmdList, int argsPos){
		ArrayList<String> afterCMD = new ArrayList<String>();
		if(!args[argsPos].equals("")) {
			for(String cmd : cmdList) {
				if (cmd.toLowerCase().startsWith(args[argsPos].toLowerCase())) {
					afterCMD.add(cmd);	
				}
			}
		}else {
			afterCMD = cmdList;
		}
		return afterCMD;
	}
}
