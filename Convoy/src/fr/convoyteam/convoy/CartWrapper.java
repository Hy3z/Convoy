package fr.convoyteam.convoy;

import java.util.ArrayList;

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

public class CartWrapper implements Listener {
	private ConfigReader configref;
	private String mapName;
	private Minecart cart;
	private World world;
	private Block spawnBlock;
	private Main mainref;
	private byte SQUARED_MAX_PUSH_RANGE = 4;
	private ArrayList<Material> railList = new ArrayList<Material>();
	public CartWrapper(ConfigReader _configref, String _mapName, World _world, Main _mainref) {
		configref=_configref;
		mapName=_mapName;
		world=_world;
		mainref=_mainref;
		spawnBlock=configref.getCartStart(mapName);
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
		int nbPusher = getPushersInRange().size();
		if(nbPusher>=1) {
			if(cart.getMaxSpeed()==0) {
				cart.setMaxSpeed(0.4);
			}
			cart.setVelocity(new Vector(0,0.05,0));	
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
		Location spawnLoc = spawnBlock.getLocation();
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
		Shape railShape = ((Rail)spawnBlock.getBlockData()).getShape();
		switch(railShape) {
		case NORTH_SOUTH:
			if(railList.contains(world.getBlockAt(spawnBlock.getLocation().add(0, 0, -1)).getType())) {
				return BlockFace.NORTH;
			}
			return BlockFace.SOUTH;
		case EAST_WEST:
			if(railList.contains(world.getBlockAt(spawnBlock.getLocation().add(1, 0, 0)).getType())) {
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
		if(cart.getLocation().getBlock()==configref.getCartEnd(mapName)){
			mainref.stopGame();
			return;
		}
		if(getPushersInRange().size()==0&&cart.getMaxSpeed()!=0) {
			cart.setMaxSpeed(0);
		}
	}
	
	@EventHandler
	public void onMinecartCollision(VehicleEntityCollisionEvent event) {
		if(event.getVehicle()==cart) {
			event.setCollisionCancelled(true);
		}
	}
}
