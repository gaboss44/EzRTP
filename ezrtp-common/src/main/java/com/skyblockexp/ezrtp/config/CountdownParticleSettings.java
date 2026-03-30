package com.skyblockexp.ezrtp.config;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;
import java.util.logging.Logger;

public final class CountdownParticleSettings {
    private final boolean enabled;
    private final Particle particle;
    private final int points;
    private final double radius;
    private final double heightOffset;
    private final double extra;
    private final boolean force;
    private final Particle secondaryParticle;
    private final int secondaryCount;
    private final double secondaryOffset;

    public CountdownParticleSettings(boolean enabled, Particle particle, int points, double radius,
                                     double heightOffset, double extra, boolean force,
                                     Particle secondaryParticle, int secondaryCount, double secondaryOffset) {
        this.enabled = enabled;
        this.particle = particle;
        this.points = points;
        this.radius = radius;
        this.heightOffset = heightOffset;
        this.extra = extra;
        this.force = force;
        this.secondaryParticle = secondaryParticle;
        this.secondaryCount = secondaryCount;
        this.secondaryOffset = secondaryOffset;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Particle getParticle() {
        return particle;
    }

    public int getPoints() {
        return points;
    }

    public double getRadius() {
        return radius;
    }

    public double getHeightOffset() {
        return heightOffset;
    }

    public double getExtra() {
        return extra;
    }

    public boolean isForce() {
        return force;
    }

    public Particle getSecondaryParticle() {
        return secondaryParticle;
    }

    public int getSecondaryCount() {
        return secondaryCount;
    }

    public double getSecondaryOffset() {
        return secondaryOffset;
    }

    public static CountdownParticleSettings fromConfiguration(ConfigurationSection section, Logger logger) {
        if (section == null) {
            return disabled();
        }
        boolean enabled = section.getBoolean("enabled", false);
        Particle particle = parseParticle(section.getString("particle",
            section.getString("type", "ENCHANTMENT_TABLE")), logger, Particle.ENCHANTMENT_TABLE);
        int points = Math.max(1, section.getInt("points", 12));
        double radius = section.getDouble("radius", 1.2D);
        double heightOffset = section.getDouble("height-offset", 0.8D);
        double extra = section.getDouble("extra", 0.0D);
        boolean force = section.getBoolean("force", false);
        String secondaryName = section.getString("secondary-particle", "");
        Particle secondaryParticle = secondaryName == null || secondaryName.isBlank()
                ? null
                : parseParticle(secondaryName, logger, null);
        int secondaryCount = Math.max(0, section.getInt("secondary-count", 6));
        double secondaryOffset = section.getDouble("secondary-offset", 0.35D);
        return new CountdownParticleSettings(enabled, particle, points, radius, heightOffset, extra, force,
                secondaryParticle, secondaryCount, secondaryOffset);
    }

    public static CountdownParticleSettings disabled() {
        return new CountdownParticleSettings(false, Particle.ENCHANTMENT_TABLE, 12,
                1.2D, 0.8D, 0.0D, false, null, 0, 0.35D);
    }

    private static Particle parseParticle(String name, Logger logger, Particle fallback) {
        try {
            return Particle.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ex) {
            if (logger != null) {
                logger.warning("Invalid particle type: " + name);
            }
            return fallback;
        }
    }
}
