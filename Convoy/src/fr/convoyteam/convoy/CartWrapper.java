package fr.convoyteam.convoy;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
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
	private final byte SQUARED_MAX_PUSH_RANGE = 4;
	private int TOTAL_RAIL;
	private Block previousBlock;
	private int railNumber;
	private ArrayList<Material> railList = new ArrayList<Material>();
	public CartWrapper(Main _mainref, Location _spawnLocation, Location _endLocation, Integer _TOTAL_RAIL) {
		mainref=_mainref;
		spawnLocation=_spawnLocation;
		endLocation=_endLocation;
		TOTAL_RAIL=_TOTAL_RAIL;
		world=spawnLocation.getWorld();
		fillRailList();
		summonCart();
	}
	public void fillRailList() {
		railList.add(Material.RAIL);
		railList.add(Material.ACTIVATOR_RAIL);
		railList.add(Material.DETECTOR_RAIL);
		railList.add(Material.POWERED_RAIL);
	}
	
	public void tick() {
		if(getPushersInRange().size()>=1) {
			if(cart.getMaxSpeed()==0) {
				cart.setMaxSpeed(0.4);
			}
			cart.setVelocity(new Vector(0,0.05,0));	
		}
		showAdvancementForPushers();
		
	}
	public void showAdvancementForPushers() {
		for(Player pusher : mainref.getPushers()) {
			pusher.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_GREEN+"Advancement: "+ChatColor.GOLD+Math.round((railNumber/TOTAL_RAIL)*100)+"%"));
		}
	}
	public ArrayList<Player> getPushersInRange(){
		ArrayList<Player> pushersInRange = new ArrayList<Player>();
		for(Player pusher : mainref.getPushers()) {
			if(cart.getLocation().distanceSquared(pusher.getLocation())<=SQUARED_MAX_PUSH_RANGE) {
				pushersInRange.add(pusher);
			}
		}
		return pushersInRange;
	}
	
	public void summonCart() {
		Float yaw = getYawForLocation();
		Location spawnLoc = spawnLocation;
		spawnLoc.setYaw(yaw);
		cart = (Minecart)world.spawnEntity(spawnLoc, EntityType.MINECART_TNT);
		cart.setInvulnerable(true);
	}
	
	public Float getYawForLocation() {
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
	
	public BlockFace getRailDirection() {
		Shape railShape = ((Rail)spawnLocation.getBlock().getBlockData()).getShape();
		switch(railShape) {
		case NORTH_SOUTH:
			if(railList.contains(world.getBlockAt(spawnLocation.add(0, 0, -1)).getType())) {
				return BlockFace.NORTH;
			}
			return BlockFace.SOUTH;
		case EAST_WEST:
			if(railList.contains(world.getBlockAt(spawnLocation.add(1, 0, 0)).getType())) {
				return BlockFace.EAST;
			}
			return BlockFace.WEST;
		default:
			//LES RAILS NE SONT PAS BONS
			mainref.stopGame();
			return null;
		}
		
	}
	@EventHandler
	public void onCartMove(VehicleMoveEvent event) {
		if(event.getVehicle()==cart) {
			if(cart.getLocation()==endLocation){
				mainref.stopGame();
				return;
			}
			Block currentRail = cart.getLocation().getBlock();
			if(currentRail!=previousBlock) {
				previousBlock=currentRail;
				railNumber++;
			}
			if(getPushersInRange().size()==0&&cart.getMaxSpeed()!=0) {
				cart.setMaxSpeed(0);
			}
		}
	}
	
	@EventHandler
	public void onMinecartCollision(VehicleEntityCollisionEvent event) {
		if(event.getVehicle()==cart) {
			event.setCollisionCancelled(true);
		}
	}
}
