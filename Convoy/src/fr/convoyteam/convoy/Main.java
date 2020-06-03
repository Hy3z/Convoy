package fr.convoyteam.convoy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.convoyteam.convoy.enums.Team;
import fr.convoyteam.convoy.enums.WeaponLevel;

/**
 * Main class, représente le plugin et la seule game
 * @author XxGoldenbluexX
 *
 */

public class Main extends JavaPlugin implements Listener {
	
	/**
	 * Liste des joueurs dans la partie
	 */
	private final WeakHashMap<Player,Team> InGamePlayers = new WeakHashMap<Player,Team>();
	private final WeakHashMap<Player,Integer> playersPoints = new WeakHashMap<Player,Integer>();
	private final ArrayList<BaseWeapon> weapons = new ArrayList<BaseWeapon>();
	private static final long GAME_TIME=1200*15;
	private static final int NB_POINTS=6;
	private long currentGameTime=0;
	private final PluginManager pmanager = Bukkit.getPluginManager();
	private final ConfigReader cfgReader = new ConfigReader(this);
	private final LangManager lang = new LangManager(this);
	private final InterfaceManager itfmanager = new InterfaceManager(this);
	private final BukkitRunnable gameTimer = new BukkitRunnable() {
		@Override
		public void run() {
			tickGame();
		}
	};
	private boolean gameStarted=false;
	private  String mapName="osef";
	//TOLOAD
	private Location defenderSpawn=null;
	private Location pusherSpawn=null;
	private Location cartStart=null;
	private Location cartStop=null;
	private CartWrapper cartRef=null;
	private int pathLength=0;
	
	@Override
	public void onEnable() {
		getCommand("conv").setExecutor(new CommandManager(this,cfgReader));
		pmanager.registerEvents(this, this);
		pmanager.registerEvents(itfmanager, this);
	}
	/**
	 * Renvoie le configReader
	 * @return ConfigReader
	 */
	public ConfigReader getConfigReader() {
		return cfgReader;
	}
	/**
	 * Démarre la partie
	 * @return true si la partie a débuter, false si la map n'a pas pu charger
	 */
	public boolean startGame() {
		if (loadMap(mapName)) {
			gameStarted=true;
			currentGameTime=0;
			cartRef = new CartWrapper(this,cartStart,cartStop,pathLength);
			pmanager.registerEvents(cartRef, this);
			playersPoints.clear();
			for (Player p : InGamePlayers.keySet()) {
				p.getInventory().clear();
				respawnPlayer(p);
				playersPoints.put(p, NB_POINTS);
				p.sendMessage(ChatColor.GREEN+"Game started!");
			}
			gameTimer.runTaskTimer(this, 0,1);
			return true;
		}else {
			return false;
		}
	}
	public void clearWeapons() {
		for (BaseWeapon w : weapons) {
			w.remove();
		}
		weapons.clear();
	}
	
	public void addWeapon(BaseWeapon w) {
		weapons.add(w);
	}
	
	public List<BaseWeapon> getPlayerWeapons(Player p) {
		ArrayList<BaseWeapon> l = new ArrayList<BaseWeapon>();
		for (BaseWeapon w : weapons) {
			if (w.getOwner().equals(p)) {
				l.add(w);
			}
		}
		return l;
	}
	
	public boolean hasThisLevel(List<BaseWeapon> w,WeaponLevel l) {
		for (BaseWeapon a : w) {
			if (a.getWeaponListRef().getLevel()==l) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Met fin a la partie
	 * @return true si la partie a été stoppée, faux si elle était deja stoppée
	 */
	public boolean stopGame() {
		if (gameStarted) {
			for(Player player : getInGamePlayers()) {
				player.sendMessage(ChatColor.GOLD+"Game Ended!");
			}
			gameStarted=false;
			gameTimer.cancel();
			cartRef.destroyCart();
			cartRef.unregister();
			cartRef=null;
			return true;
		}else {
			return false;
		}
	}
	
	public void tickGame() {
		currentGameTime++;
		if (currentGameTime>=GAME_TIME) {
			stopGame();
			return;
		}
		cartRef.tick();
	}
	/**
	 * Permet de charger une map
	 * @param n Nom de la map
	 * @return true si la map est chargée, sinon false.
	 */
	public boolean loadMap(String n) {
		pathLength = cfgReader.getTrackLenght(n);
		if (pathLength<=0) {
			return false;
		}
		defenderSpawn = cfgReader.getDefenderSpawn(n);
		if (defenderSpawn==null) {
			return false;
		}
		pusherSpawn = cfgReader.getPusherSpawn(n);
		if (pusherSpawn==null) {
			return false;
		}
		cartStart = (Location) cfgReader.getCartStart(n);
		if (cartStart==null) {
			return false;
		}
		cartStop = (Location) cfgReader.getCartEnd(n);
		if (cartStop==null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Permet de faire rejoindre la partie a un joueur.
	 * @param player Le joueur que tu veut ajouter a la partie.
	 * @return true si le joueur a bien rejoins, false si il était déja dans la partie.
	 */
	
	public boolean addPlayer(Player player) {
		if (InGamePlayers.containsKey(player)) {
			return false;
		}
		player.setGameMode(GameMode.ADVENTURE);
		if (getPushers().size()<getDefenders().size()) {
			InGamePlayers.put(player,Team.DEFENDERS);
		}else {
			InGamePlayers.put(player,Team.PUSHERS);
		}
		player.getInventory().clear();
		itfmanager.setSettingItemInInventory(player);
		return true;
	}
	
	/**
	 * Permet de faire quitter la partie a un joueur.
	 * @param player Le joueur a retirer de la partie.
	 * @return true si le joueur a bien quitté la partie, false si il n'était pas dedant.
	 */
	
	public boolean removePlayer(Player player) {
		if (InGamePlayers.containsKey(player)) {
			InGamePlayers.remove(player);
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * Permet d'avoir la liste de tout les joueurs présent dans la partie qui sont dans l'équipe Pusher.
	 * @return Liste de tout les Pushers
	 */
	
	public ArrayList<Player> getPushers(){
		ArrayList<Player> pl = new ArrayList<Player>();
		for (Player p : InGamePlayers.keySet()) {
			if (InGamePlayers.get(p)==Team.PUSHERS) {
				pl.add(p);
			}
		}
		return pl;
	}
	
	/**
	 * Permet d'avoir la liste de tout les joueurs présent dans la partie qui sont dans l'équipe Defender.
	 * @return Liste de tout les Defenders
	 */
	
	public ArrayList<Player> getDefenders(){
		ArrayList<Player> pl = new ArrayList<Player>();
		for (Player p : InGamePlayers.keySet()) {
			if (InGamePlayers.get(p)==Team.DEFENDERS) {
				pl.add(p);
			}
		}
		return pl;
	}
	
	public Set<Player> getInGamePlayers(){
		return InGamePlayers.keySet();
	}
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player p = event.getPlayer();
		if (InGamePlayers.containsKey(p)) {
			if (gameStarted) {
				event.setCancelled(true);
			}else if (event.getItemDrop().getItemStack().getType()==Material.SUNFLOWER){
				event.getItemDrop().remove();
				startGame();
			}
		}
	}
	@EventHandler
	public void onPlayerPickupItem(EntityPickupItemEvent event) {
		if (InGamePlayers.containsKey(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	@EventHandler
	public void onPlayerChangeGameMode(PlayerGameModeChangeEvent event) {
		if (InGamePlayers.containsKey(event.getPlayer())) {
			if(gameStarted) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED+lang.get(event.getPlayer(), "game.gamemodeChange"));
				return;
			}
		}
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		lang.add(event.getPlayer());
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		removePlayer(event.getPlayer());
	}
	/**
	 * Méthode appelée par spigot a l'occasion de l'évenement EntityDamageByEntityEvent
	 * @param event évenement
	 */
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (InGamePlayers.containsKey(p) && gameStarted && p.getHealth()<=event.getDamage()) {
				event.setCancelled(true);
				respawnPlayer(p);
			}
		}
	}
	
	/**
	 * Méthode pour faire réaparaitre un joueur a son point d'aparition
	 * Ne fonctione seulement si le joueur est dans la partie, si son spawn est chargé et si la partie a commencé
	 * @param player Le joueur a faire réaparaitre
	 */
	public void respawnPlayer(Player player) {
		if (InGamePlayers.containsKey(player) && gameStarted) {
			if (InGamePlayers.get(player)==Team.DEFENDERS) {
				if (defenderSpawn!=null){
					player.teleport(defenderSpawn);
				}
			}else {
				if (pusherSpawn!=null){
					player.teleport(pusherSpawn);
				}
			}
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,40,9,false,false,true));
		}
	}
	
	
	/**
	 * Permet d'obtenir un text traduit dans la langue du joueur précisé
	 * @param p Joueur dont on souhaite prendre la langue
	 * @param path Chemin du texte traduit
	 * @return Texte du chemin spécifié traduit dans la langue du joueur, retourne une chaine vide si le chemin n'existe pas
	 */
	public String getText(Player p , String path) {
		return lang.get(p, path);
	}
	/**
	 * Permet de savoir si la partie a démarée
	 * @return True si la partie est en cours, sinon faux
	 */
	public boolean isStarted() {
		return gameStarted;
	}
	
	public void RegisterListener(Listener l) {
		pmanager.registerEvents(l, this);
	}
	@EventHandler
	private void onExplosionEvent(EntityExplodeEvent event) {
		event.blockList().clear();
	}
}