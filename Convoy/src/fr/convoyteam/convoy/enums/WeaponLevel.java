package fr.convoyteam.convoy.enums;

public enum WeaponLevel {
	PRIMAIRE(0),
	SECONDAIRE(1),
	TERTIAIRE(2);
	private final int slot;
	private WeaponLevel(int slt) {
		slot=slt;
	}
	
	public int getSlot() {
		return slot;
	}
	
}
