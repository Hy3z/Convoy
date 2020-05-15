package fr.convoyteam.convoy;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.convoyteam.convoy.enums.Team;

/**
 * Main class, représente le plugin et la seule game
 * @author XxGoldenbluexX
 *
 */

public class Main extends JavaPlugin implements Listener {
	
	/**
	 * Liste des joueurs dans la partie
	 */
	private final HashMap<Player,Team> InGamePlayers = new HashMap<Player,Team>();
	private static final long GAME_TIME=1200*15;
	private final PluginManager pmanager = Bukkit.getPluginManager();
	private final ConfigReader cfgReader = new ConfigReader(this);
	private final LangManager lang = new LangManager(this);
	private final BukkitRunnable gameTimer = new BukkitRunnable() {
		@Override
		public void run() {
			stopGame();
		}
	};
	private boolean gameStarted=false;
	private Location defenderSpawn=null;
	private Location pusherSpawn=null;
	
	@Override
	public void onEnable() {
		getCommand("conv").setExecutor(new CommandManager(this,cfgReader));
		pmanager.registerEvents(this, this);
	}
	
	/**
	 * Permet de faire rejoindre la partie a un joueur.
	 * @param player Le joueur que tu veut ajouter a la partie.
	 * @return true si le joueur a bien rejoins, false si il était déja dans la partie.
	 */
	
	public void startGame() {
		gameStarted=true;
		gameTimer.runTaskLater(this, GAME_TIME);
	}
	
	public void stopGame() {
		if (gameStarted) {
			gameStarted=false;
			gameTimer.cancel();
		}
	}
	
	public boolean addPlayer(Player player) {
		if (InGamePlayers.containsKey(player)) {
			return false;
		}
		if (getPushers().size()<getDefenders().size()) {
			InGamePlayers.put(player,Team.PUSHERS);
		}else {
			InGamePlayers.put(player,Team.DEFENDERS);
		}
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
}