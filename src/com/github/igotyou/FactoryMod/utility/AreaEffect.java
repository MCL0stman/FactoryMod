/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.igotyou.FactoryMod.utility;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Brian Landry
 */
public class AreaEffect extends PotionEffect{
	private final int radius;
	
	public AreaEffect (PotionEffectType type, int duration, int amplifier, boolean ambient) {
		super(type,duration,amplifier,ambient);
		this.radius=1;
	}
	public AreaEffect (int radius, PotionEffectType type, int duration, int amplifier, boolean ambient) {
		super(type,duration,amplifier,ambient);
		this.radius=radius;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public static Set<AreaEffect> areaEffectsFromConfig(ConfigurationSection configurationSection) {
		Set<AreaEffect> areaEffects = new HashSet<AreaEffect>();
		if(configurationSection!=null)
		{
			for(String areaEffectName:configurationSection.getKeys(false))
			{
				areaEffects.add(AreaEffect.fromConfig(configurationSection.getConfigurationSection(areaEffectName)));
			}
		}
		return areaEffects;
	}
	
	public static AreaEffect fromConfig(ConfigurationSection configurationSection) {
		PotionEffect potionEffect=potionEffectFromConfig(configurationSection);
		int radius = configurationSection.getInt("radius", 1);
		return new AreaEffect(radius,potionEffect.getType(),potionEffect.getDuration(),potionEffect.getAmplifier(),potionEffect.isAmbient());
	}
	public static PotionEffect potionEffectFromConfig(ConfigurationSection configurationSection) {
		PotionEffectType effectType = PotionEffectType.getByName(configurationSection.getString("PotionEffect1"));
		int duration = configurationSection.getInt("duration");
		int amplifier = configurationSection.getInt("amplifier");
		boolean ambient = configurationSection.getBoolean("ambient");
		return new PotionEffect(effectType,duration,amplifier,ambient);
	}
}
