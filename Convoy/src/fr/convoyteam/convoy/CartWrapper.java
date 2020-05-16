package fr.convoyteam.convoy;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

public class CartWrapper implements Listener {
	public CartWrapper() {
		
	}
	@EventHandler
	public void onMinecartCollision(VehicleEntityCollisionEvent event) {
		if(event.getVehicle().getVehicle().getType()==EntityType.MINECART_COMMAND) {
			event.setCancelled(true);
		}
	}
}
