package fr.convoyteam.convoy.weapons;

import fr.convoyteam.convoy.BaseWeapon;

public class BasePistol extends BaseWeapon {
	byte magazine;
	String fireMode;
	float fireRate;
	float damage;
	float precision;
	float bulletSpeed;
	float firingSlow;
	float zoomPower;
	float carrySlow;
	public BasePistol(byte _magazine, String _fireMode, float _fireRate, float _damage, float _precision, float _bulletSpeed, float _firingSlow,
					float _zoomPower, float _carrySlow) {
		super();
		magazine=_magazine;
		fireMode=_fireMode;
		fireRate=_fireRate;
		damage=_damage;
		precision=_precision;
		bulletSpeed=_bulletSpeed;
		firingSlow=_firingSlow;
		zoomPower=_zoomPower;
		carrySlow=_carrySlow;
	}
}
