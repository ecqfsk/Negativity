package com.elikill58.negativity.spigot;

import com.elikill58.negativity.universal.utils.ReflectionUtils;

public enum SubPlatform {

	CRAFTBUKKIT("CraftBukkit", ReflectionUtils.isClassExist("org.spigotmc.SpigotConfig")),
	SPIGOT("Spigot", false),
	FOLIA("Folia", ReflectionUtils.isClassExist("io.papermc.paper.threadedregions.RegionizedServer")),
	PAPER("Paper", ReflectionUtils.isClassExist("com.destroystokyo.paper.PaperVersionFetcher")),
	MOHIST("Mohist", ReflectionUtils.isClassExist("com.mohistmc.MohistMC"));
	
	private final String name;
	private final boolean isThis;
	
	private SubPlatform(String name, boolean isThis) {
		this.name = name;
		this.isThis = isThis;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isThis() {
		return isThis;
	}
	
	public static SubPlatform getSubPlatform() {
		for(SubPlatform sub : values())
			if(sub.isThis())
				return sub;
		return SPIGOT; // default platform
	}
}
