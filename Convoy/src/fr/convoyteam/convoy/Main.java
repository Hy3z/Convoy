package fr.convoyteam.convoy;

import java.util.ArrayList;
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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.convoyteam.convoy.enums.Team;

/**
 * Main class, repr�sente le plugin et la seule game
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
	private final BukkitRunnable gameTimer = new BukkitRunnable() {
		@Override
		public void run() {
			tickGame();
		}
	};
	private boolean gameStarted=false;
	private  String mapName="";
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
	}
	/**
	 * D�marre la partie
	 * @return true si la partie a d�buter, false si la map n'a pas pu charger
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
			}
			gameTimer.runTaskTimer(this, 0,1);
			return true;
		}else {
			return false;
		}
	}
	public void reloadWeapons() {
		weapons.clear();
		loadGuns();
	}
	
	public void loadGuns() {
		
	}
	
	/**
	 * Met fin a la partie
	 * @return true si la partie a �t� stopp�e, faux si elle �tait deja stopp�e
	 */
	public boolean stopGame() {
		if (gameStarted) {
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
	 * @return true si la map est charg�e, sinon false.
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
	 * @return true si le joueur a bien rejoins, false si il �tait d�ja dans la partie.
	 */
	
	public boolean addPlayer(Player player) {
		if (InGamePlayers.containsKey(player)) {
			return false;
		}
		player.setGameMode(GameMode.ADVENTURE);
		if (getPushers().size()<getDefenders().size()) {
			InGamePlayers.put(player,Team.PUSHERS);
		}else {
			InGamePlayers.put(player,Team.DEFENDERS);
		}
		player.getInventory().clear();
		ItemStack itm = new ItemStack(Material.SUNFLOWER);
		ItemMeta meta = itm.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD+lang.get(player, "lobby.itemStartName"));
		itm.setItemMeta(meta);
		player.getInventory().setItem(0, itm);
		return true;
	}
	
	/**
	 * Permet de faire quitter la partie a un joueur.
	 * @param player Le joueur a retirer de la partie.
	 * @return true si le joueur a bien quitt� la partie, false si il n'�tait pas dedant.
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
	 * Permet d'avoir la liste de tout les joueurs pr�sent dans la partie qui sont dans l'�quipe Pusher.
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
	 * Permet d'avoir la liste de tout les joueurs pr�sent dans la partie qui sont dans l'�quipe Defender.
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
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED+lang.get(event.getPlayer(), "game.gamemodeChange"));
			return;
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
	 * M�thode appel�e par spigot a l'occasion de l'�venement EntityDamageByEntityEvent
	 * @param event �venement
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
	 * M�thode pour faire r�aparaitre un joueur a son point d'aparition
	 * Ne fonctione seulement si le joueur est dans la partie, si son spawn est charg� et si la partie a commenc�
	 * @param player Le joueur a faire r�aparaitre
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
	 * Permet d'obtenir un text traduit dans la langue du joueur pr�cis�
	 * @param p Joueur dont on souhaite prendre la langue
	 * @param path Chemin du texte traduit
	 * @return Texte du chemin sp�cifi� traduit dans la langue du joueur, retourne une chaine vide si le chemin n'existe pas
	 */
	public String getText(Player p , String path) {
		return lang.get(p, path);
	}
	/**
	 * Permet de savoir si la partie a d�mar�e
	 * @return True si la partie est en cours, sinon faux
	 */
	public boolean isStarted() {
		return gameStarted;
	}
	
	public void RegisterListener(Listener l) {
		pmanager.registerEvents(l, this);
	}
}