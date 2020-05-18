package fr.convoyteam.convoy.weapons;

import org.bukkit.configuration.file.YamlConfiguration;

import fr.convoyteam.convoy.BaseWeapon;

public class BasePistol extends BaseWeapon {
	YamlConfiguration pistolConfig;
	public BasePistol(YamlConfiguration _pistolConfig) {
		pistolConfig=_pistolConfig;
	}
}
