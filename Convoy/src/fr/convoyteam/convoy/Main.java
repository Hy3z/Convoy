package fr.convoyteam.convoy;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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
	private final PluginManager pmanager = Bukkit.getPluginManager();
	private boolean gameStarted=false;
	
	@Override
	public void onEnable() {
		getCommand("conv").setExecutor(new CommandManager(this));
		pmanager.registerEvents(this, this);
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
	/**
	 * Méthode appelée par spigot a l'occasion de l'évenement EntityDamageByEntityEvent
	 * @param event évenement
	 */
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (InGamePlayers.containsKey(p) && gameStarted) {
				if (InGamePlayers.get(p)==Team.DEFENDERS) {
					
				}else {
					
				}
			}
		}
	}
}