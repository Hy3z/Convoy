package fr.convoyteam.convoy.weapons;


import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import fr.convoyteam.convoy.BaseWeapon;
import fr.convoyteam.convoy.Main;

public class BasePistol extends BaseWeapon {
	private Main mainref;
	private Player player;
	private String weaponName;
	private String weaponType;
	private byte magazine;
	private long reloadTime;
	private String fireMode;
	private long fireRate;
	private long timeBetweenBullet;
	private int bulletNumber;
	private float damage;
	private float explosionRadius;
	private float precision;
	private float bulletSpeed;
	private float zoomPower;
	private int carrySlow;
	private boolean isPrecise=false;
	private boolean hasTravelTime=false;
	private boolean isExplosive=false;
	private boolean hasZoom=false;
	private boolean hasWeight=false;	
	private boolean shootOnCD=false;
	private boolean reloading=false;
	private boolean isSlowed=false;
	private boolean isZooming=false;
	
	private ArrayList<Snowball> bulletsFired = new ArrayList<Snowball>();
	private ArrayList<TNTPrimed> tntFired = new ArrayList<TNTPrimed>();
	private int pistolSlot=0;
	private byte currentBullets=magazine;
	private byte bulletsShot=0;
		//TONY PART
		private ItemStack pistolItem;
	public BasePistol(Main _mainref, Player _player, String _weaponName, String _weaponType, byte _magazine, long _reloadTime, String _fireMode, long _fireRate, 
					float _damage, int _bulletNumber, long _timeBetweenBullet, float _precision, float _bulletSpeed, float _explosionRadius, float _zoomPower, int _carrySlow) {
		super();
		mainref=_mainref;
		player=_player;
		weaponName=_weaponName;
		weaponType=_weaponType;
		magazine=_magazine;
		reloadTime=_reloadTime;
		fireMode=_fireMode;
		fireRate=_fireRate;
		damage=_damage;
		bulletNumber=_bulletNumber;
		timeBetweenBullet=_timeBetweenBullet;
		precision=_precision;
		bulletSpeed=_bulletSpeed;
		explosionRadius=_explosionRadius;
		zoomPower=_zoomPower;
		carrySlow=_carrySlow;
		weaponFeatures();
		updateWeaponName(false);
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	private boolean canReload() {
		if(currentBullets<magazine && !reloading) {
			return true;
		}
		return false;
	}
	
	private boolean canFire() {
		if(currentBullets>0 && !reloading && !shootOnCD) {
			return true;
		}
		return false;
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	private void gunActionManager(Action playerAction) {
		if(playerAction.equals(Action.LEFT_CLICK_AIR)||playerAction.equals(Action.LEFT_CLICK_BLOCK)) {
			if(canReload()) {
				reload();
			}
			return;
		}
		if(playerAction.equals(Action.RIGHT_CLICK_AIR)||playerAction.equals(Action.RIGHT_CLICK_BLOCK)) {
			if(canFire()) {
				fire();
				return;
			}
			if(canReload()&&currentBullets==0) {
				reload();
			}
		}
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	private void reload() {
		//BRUIT DU DEBUT DE RELOAD
		reloading=true;
		updateWeaponName(true);
		cooldownReload();
	}
	private void cooldownReload() {
		new BukkitRunnable() {
			@Override
			public void run() {
				//BRUIT DE FIN DE RELOAD
				reloading=false;
				currentBullets=magazine;
				updateWeaponName(false);
			}
		}.runTaskLater(mainref,reloadTime*20);
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	private void fire() {
		//BRUIT DU TIR
		shootOnCD=true;
		cooldownFire();
		fireRepetively();
		bulletsShot=0;
		currentBullets--;
		updateWeaponName(false);
	}
	private void cooldownFire() {
		new BukkitRunnable() {
			@Override
			public void run() {
				shootOnCD=false;
			}
		}.runTaskLater(mainref,20/fireRate);
	}
	private void fireRepetively() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(bulletsShot<bulletNumber) {
					bulletsShot++;
					fireBullet();
					fireRepetively();
				}
			}
		}.runTaskLater(mainref, 20*timeBetweenBullet);
	}
	private void fireBullet() {
		if(hasTravelTime) {
			fireWithVelocity(player);
			return;
		}
		fireWithRay(player);
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------
	
	private void fireWithVelocity(Player player) {
		summonBullet(player.getEyeLocation());
	}
	private void fireWithRay(Player player){
		Vector bulletVector = getBulletVector(player);
		if(!isExplosive) {
			RayTraceResult rayTrace = player.getWorld().rayTraceEntities(player.getEyeLocation(), bulletVector, 160);
			Entity entityHit = rayTrace.getHitEntity();
			if(entityHit!=null) {
				if(entityHit instanceof Player) {
					//si le joueur est dans la team adverse
					((Player)entityHit).damage(damage);
				}
			}
			return;
		}
		//TODO
		/*DETECTER SI LA TNT OU LA BOULE DE NEIGE APPARTIENT A CETTE CLASSE POUR POUVOIR INFLIGER LES DEGATS
		 * IDEE: CREER UNE CLASSE "BULLET" et une "TNT" qui gère elle même les degats et le radius
		 * METTRE DANS LE MAIN UN CANCEL SUR LEVENT "BLOCKEXPLODEEVENT"
		  */
		RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(player.getEyeLocation(), bulletVector, 160, FluidCollisionMode.NEVER, true);
		Block blockHit = rayTrace.getHitBlock();
		if(blockHit!=null) {
			summonTNT(blockHit.getLocation());
		}
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------
	
	private Float applyPrecisionYaw(Float yaw) {
		Double yawVariability = Math.random()*(yaw-(yaw*precision)+1);
		if(Math.random()>=0.5) {
			return (float) (yaw+yawVariability);
		}else {
			return (float) (yaw-yawVariability);
		}
	}
	private Float applyPrecisionPitch(Float pitch) {
		Double pitchVariability = Math.random()*(pitch-(pitch*precision)+1);
		if(Math.random()>=0.5) {
			return (float) (pitch+pitchVariability);
		}else {
			return (float) (pitch-pitchVariability);
		}
	}
	private Vector getBulletVector(Player player) {
		Float yaw=player.getEyeLocation().getYaw();
		Float pitch=player.getEyeLocation().getPitch();
		if(!isPrecise) {
			yaw=applyPrecisionYaw(yaw);
			pitch=applyPrecisionPitch(pitch);
		}
		Float xValue;
		Float zValue;
		Float yValue;
		if(yaw>=0&&yaw<=90) {
			xValue=-yaw*(1/90);
			zValue=1-(-xValue);
		}else if(yaw>=90&&yaw<=180) {
			zValue=-((yaw-90)*(1/90));
			xValue=-(1-(-zValue));
		}else if(yaw>=180&&yaw<=270) {
			xValue=(yaw-180)*(1/90);
			zValue=-(1-xValue);
		}else {
			zValue=(yaw-270)*(1/90);
			xValue=bulletSpeed-zValue;
		}
		yValue=-(pitch*(1/90));
		return new Vector(xValue*bulletSpeed, yValue*bulletSpeed, zValue*bulletSpeed);
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	private void updateWeaponName(boolean isReloading) {
		ItemMeta meta = pistolItem.getItemMeta();
		if(isReloading) {
			meta.setDisplayName(ChatColor.RED+"Reloading...");
		}else {
			meta.setDisplayName(ChatColor.AQUA+weaponName+ChatColor.WHITE+": "+ChatColor.AQUA+currentBullets+ChatColor.WHITE+"/"+ChatColor.GOLD+magazine);
		}
		pistolItem.setItemMeta(meta);
		player.getInventory().setItem(pistolSlot, pistolItem);
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------
	
	private void summonBullet(Location bulletLoc) {
		Snowball bullet = (Snowball) player.getWorld().spawnEntity(bulletLoc, EntityType.SNOWBALL);
		Vector bulletVector = getBulletVector(player);
		bullet.setVelocity(bulletVector);
		bulletsFired.add(bullet);
	}
	private void summonTNT(Location tntLoc) {
		TNTPrimed tnt = (TNTPrimed)player.getWorld().spawnEntity(tntLoc, EntityType.PRIMED_TNT);
		tnt.setIsIncendiary(false);
		tnt.setFuseTicks(0);
		tntFired.add(tnt);
		
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	private void weaponFeatures() {
		if(weaponType=="secondary") {
			pistolSlot=1;
		}
		if(precision>=1) {
			isPrecise=true;
		}
		if(bulletSpeed>0) {
			hasTravelTime=true;
		}
		if(explosionRadius>0) {
			isExplosive=true;
		}
		if(zoomPower>0) {
			hasZoom=true;
		}
		if(carrySlow!=0) {
			hasWeight=true;
		}
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	@EventHandler
	private void onInteractEvent(PlayerInteractEvent event) {
		if(event.getPlayer().equals(player)) {
			if(event.getItem().isSimilar(pistolItem)) {
				if(event.getAction()!=Action.PHYSICAL) {
					gunActionManager(event.getAction());
					if(fireMode=="SemiAuto") {
						//CENSE EMPECHE DE MAINTENIR CLIC DROIT
						event.setCancelled(true);
					}
				}
			}
		}
	}
	@EventHandler
	private void onPlayerDrop(PlayerDropItemEvent event) {
		if(event.getPlayer().equals(player)) {
			if(event.getItemDrop().getItemStack().isSimilar(pistolItem)) {
				if(canReload()) {
					reload();
				}
			}
		}
	}
	@EventHandler
	private void onChangeSlot(PlayerItemHeldEvent event) {
		if(hasWeight) {
			if(event.getPlayer().equals(player)) {
				if(player.getInventory().getItemInMainHand().isSimilar(pistolItem)){
					isSlowed=true;
					player.setWalkSpeed((float) (player.getWalkSpeed()-(0.2*(carrySlow/100))));
					return;
				}
				if(!player.getInventory().getItemInMainHand().isSimilar(pistolItem) && isSlowed){
					isSlowed=false;
					player.setWalkSpeed((float) (player.getWalkSpeed()+(0.2*(carrySlow/100))));
				}
			}
		}
	}
	@EventHandler
	private void onPlayerSneak(PlayerToggleSneakEvent event) {
		if(hasZoom) {
			if(event.getPlayer().equals(player)) {
				if(player.getInventory().getItemInMainHand().isSimilar(pistolItem)) {
					if(event.isSneaking() && !isZooming) {
						player.setWalkSpeed((float) (player.getWalkSpeed()-(0.2*(zoomPower/100))));
						isZooming=true;
						return;
					}
					if(event.isCancelled() && isZooming) {
						player.setWalkSpeed((float) (player.getWalkSpeed()+(0.2*(zoomPower/100))));
						isZooming=false;
					}
				}
			}
		}
	}
	@EventHandler
	private void onTNTExplosion(ExplosionPrimeEvent event) {
		if(tntFired.contains(event.getEntity())) {
			event.setRadius(explosionRadius);
		}
	}
	@EventHandler
	private void onDamage(EntityDamageByEntityEvent event) {
		if(bulletsFired.size()>=1) {
			if(bulletsFired.contains(event.getDamager())) {
				if(isExplosive) {
					//CENSE FAIRE PASSER LA BOULE DE NEIGE A TRAVERS LE MEC, JE VERRAIA APRES
					event.setCancelled(true);
					return;
				}
				if(event.getEntity() instanceof Player) {
					//SI LE MEC EST DANS LA TEAM ENNEMIE
					event.setDamage(damage);
					bulletsFired.remove(event.getDamager());
				}
			}
		}
		if(isExplosive) {
			if(tntFired.size()>=1) {
				if(tntFired.contains(event.getDamager())) {
					if(event.getEntity() instanceof Player) {
						event.setDamage(damage);
						tntFired.remove(event.getDamager());
					}
				}
			}
		}
	}
	@EventHandler
	private void onEntityByBlock(EntityDamageByBlockEvent event) {
		if(isExplosive) {
			if(hasTravelTime) {
				if(bulletsFired.contains(event.getEntity())) {
					summonTNT(event.getEntity().getLocation());
					bulletsFired.remove(event.getEntity());
				}
			}
			return;
		}
		if(bulletsFired.contains(event.getEntity())) {
			bulletsFired.remove(event.getEntity());
		}
		
	}
}
