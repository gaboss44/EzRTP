package com.skyblockexp.ezrtp.util.compat;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticleCompatTest {

    @Test
    void spawnParticleWithNullWorldDoesNotThrow() {
        // Null world should be handled gracefully
        ParticleCompat.spawnParticle(null, Particle.FLAME, null, 1, 0, 0, 0, 0.0, null, false);
    }

    @Test
    void spawnParticleWithUnsupportedSignaturesDoesNotThrow() {
        // We don't have a real World instance in unit tests; ensure call doesn't throw for null location
        ParticleCompat.spawnParticle(null, Particle.PORTAL, (Location) null, 0, 0.0, 0.0, 0.0, 0.0, null, false);
    }
}
