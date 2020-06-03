package fr.convoyteam.convoy;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class CartWrapper implements Listener {
	private Minecart cart;
	private World world;
	private Location spawnLocation;
	private Location endLocation;
	private Main mainref;
	private final float MAX_PUSH_RANGE = 3.5f;
	private int TOTAL_RAIL;
	private Block previousBlock;
	private int railNumber;
	private final ArrayList<Vector> vectorForParticles = fillPushVectorList(5);
	public CartWrapper(Main _mainref, Location _spawnLocation, Location _endLocation, int _TOTAL_RAIL) {
		mainref=_mainref;
		spawnLocation=_spawnLocation;
		endLocation=_endLocation;
		TOTAL_RAIL=_TOTAL_RAIL;
		world=spawnLocation.getWorld();
		previousBlock=spawnLocation.getBlock();
		summonCart();
		cart.setGlowing(true);
	}

//---------------------------------------------------------------------------------------------------------------------------------------------

	private void summonCart() {
		Float yaw = getYawForLocation();
		Location spawnLoc = spawnLocation;
		spawnLoc.setYaw(yaw);
		cart = (Minecart)world.spawnEntity(spawnLoc, EntityType.MINECART_TNT);
		cart.setInvulnerable(true);
	}
	/**
	 * Destroy the cart
	 */ 
	public void destroyCart() {
		cart.remove();
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * To tick for particles, messages and cart end detection
	 */
	public void tick() {
		for(Vector vector : vectorForParticles) {
			Location particleLoc=cart.getLocation().add(vector);
			world.spawnParticle(Particle.END_ROD, particleLoc.getX(), particleLoc.getY(), particleLoc.getZ(), 1, 0, 0, 0, 0.01);
		}
		showAdvancemementForDefenders();
		if(getPushersInRange().size()>=1) {
			if(cart.getMaxSpeed()==0) {
				cart.setMaxSpeed(0.4);
			}
			cart.setVelocity(new Vector(0.05,0,0));	
			showAdvancementForPushers("[Pushing]");
			return;
		}
		showAdvancementForPushers("[Standby]");
	}

//---------------------------------------------------------------------------------------------------------------------------------------------

	private ArrayList<Player> getPushersInRange(){
		ArrayList<Player> pushersInRange = new ArrayList<Player>();
		for(Player pusher : mainref.getPushers()) {
			if(cart.getLocation().distanceSquared(pusher.getLocation())<=MAX_PUSH_RANGE*MAX_PUSH_RANGE) {
				pushersInRange.add(pusher);
			}
		}
		return pushersInRange;
	}

//---------------------------------------------------------------------------------------------------------------------------------------------

	private void showAdvancementForPushers(String cartStatus) {
		for(Player pusher : mainref.getPushers()) {	
			Float percentage = (float)railNumber/TOTAL_RAIL;
			int percent = Math.round(percentage*100);
			if(cartStatus.equals("[Standby]")) {
				pusher.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+cartStatus+": "+ChatColor.GOLD+percent+"%"));
				return;
			}
			pusher.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_GREEN+cartStatus+": "+ChatColor.GOLD+percent+"%"));
		}
	}
	private void showAdvancemementForDefenders() {
		for (Player defender : mainref.getDefenders()) {
			Float percentage = (float)railNumber/TOTAL_RAIL;
			int percent = Math.round(percentage*100);
			if(percent<33) {
				defender.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_GREEN+"Advancemement: "+ChatColor.GOLD+percent+"%"));
				return;
			}
			if(percent<66) {
				defender.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW+"Advancemement: "+ChatColor.GOLD+percent+"%"));
				return;
			}
			defender.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+"Advancemement: "+ChatColor.GOLD+percent+"%"));
		}
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	private Float getYawForLocation() {
		BlockFace direction = getRailDirection();
		switch(direction) {
		case NORTH:
			return -90f;
		case SOUTH:
			return 90f;
		case EAST:
			return 180f;
		case WEST:
			return 0f;
		default:
			return null;
		}
	}
	private BlockFace getRailDirection() {
		Shape railShape;
		BlockData spawnBlockData = spawnLocation.getBlock().getBlockData();
		if(spawnBlockData instanceof Rail) {
			railShape = ((Rail) spawnBlockData).getShape();
		}else {
			mainref.stopGame();
			return null;
		}
		ArrayList<Material> railList = new ArrayList<Material>();
		railList.add(Material.RAIL);
		railList.add(Material.ACTIVATOR_RAIL);
		railList.add(Material.DETECTOR_RAIL);
		railList.add(Material.POWERED_RAIL);
		Location spawnLocationClone=spawnLocation.clone();
		switch(railShape) {
		case NORTH_SOUTH:
			if(railList.contains(world.getBlockAt(spawnLocationClone.add(0, 0, -1)).getType())) {
				return BlockFace.NORTH;
			}
			return BlockFace.SOUTH;
		case EAST_WEST:
			if(railList.contains(world.getBlockAt(spawnLocationClone.add(1, 0, 0)).getType())) {
				return BlockFace.EAST;
			}
			return BlockFace.WEST;
		default:
			//LES RAILS NE SONT PAS BONS
			mainref.stopGame();
			return null;
		}	
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	private ArrayList<Vector> fillPushVectorList(int nbPointsParRayon) {
		ArrayList<Vector> vectorList = new ArrayList<Vector>();
		float pas = MAX_PUSH_RANGE/nbPointsParRayon;
		for (float x=pas; x<=MAX_PUSH_RANGE; x=x+pas) {
			double alpha = Math.acos(x/MAX_PUSH_RANGE);
			double z = (Math.sin(alpha))*MAX_PUSH_RANGE;
			vectorList.add(new Vector(x,0,z));
			vectorList.add(new Vector(-x,0,z));
			vectorList.add(new Vector(x,0,-z));
			vectorList.add(new Vector(-x,0,-z));
		}
		return vectorList;
	}

//---------------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Unregister the cart
	 */
	public void unregister(){
		  HandlerList.unregisterAll(this);
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	@EventHandler
	private void onCartMove(VehicleMoveEvent event) {
		if(event.getVehicle().equals(cart)) {
			if(endLocation.distanceSquared(cart.getLocation())<=0.001) {
				Bukkit.getConsoleSender().sendMessage(""+cart.getLocation());
				mainref.stopGame();
				return;
			}
			Block currentRail = cart.getLocation().getBlock();
			if(!currentRail.equals(previousBlock)) {
				previousBlock=currentRail;
				railNumber++;
			}
			if(getPushersInRange().size()==0&&cart.getMaxSpeed()!=0) {
				cart.setMaxSpeed(0);
			}
		}
	}
}
