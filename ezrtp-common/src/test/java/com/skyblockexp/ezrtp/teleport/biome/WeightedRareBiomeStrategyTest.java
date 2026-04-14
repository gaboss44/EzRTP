package com.skyblockexp.ezrtp.teleport.biome;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeightedRareBiomeStrategyTest {

    @Mock
    private BiomeSearchStrategy fallbackStrategy;

    @Mock
    private RareBiomeRegistry registry;

    @Mock
    private World world;

    @Test
    void delegatesToProvidedFallbackWhenTargetsAreNotRare() {
        WeightedRareBiomeStrategy strategy = new WeightedRareBiomeStrategy(fallbackStrategy);
        Set<Biome> targets = Set.of(Biome.PLAINS);

        when(registry.isRareBiome(any(Biome.class))).thenReturn(false);
        int[] expected = new int[]{25, -64};
        when(fallbackStrategy.generateCandidateCoordinates(eq(world), eq(100), eq(-20), eq(50), eq(150),
            eq(targets), eq(registry))).thenReturn(expected);

        int[] result = strategy.generateCandidateCoordinates(world, 100, -20, 50, 150, targets, registry);

        assertSame(expected, result, "Weighted strategy should delegate to fallback when no rare biomes apply");
        verify(fallbackStrategy).generateCandidateCoordinates(eq(world), eq(100), eq(-20), eq(50), eq(150),
            eq(targets), eq(registry));
    }
}
