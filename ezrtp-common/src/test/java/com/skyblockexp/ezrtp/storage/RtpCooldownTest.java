package com.skyblockexp.ezrtp.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RTP cooldown and usage limit logic.
 */
class RtpCooldownTest {

    @TempDir
    File tempDir;
    
    private RtpUsageStorage storage;
    private UUID testPlayer;
    private String testWorld;

    @BeforeEach
    void setUp() {
        File storageFile = new File(tempDir, "test-rtp-usage.yml");
        storage = new YamlRtpUsageStorage(storageFile);
        testPlayer = UUID.randomUUID();
        testWorld = "world";
    }

    @Test
    void testFirstTimeUserHasNoCooldown() {
        // First time user should have lastRtp = 0
        long lastRtp = storage.getLastRtpTime(testPlayer, testWorld);
        assertEquals(0, lastRtp, "First time user should have lastRtp = 0");
        
        // Simulate cooldown check logic from RandomTeleportCommand
        int cooldownSeconds = 300; // 5 minutes
        long now = System.currentTimeMillis();
        
        // The bug was that (now - 0) is always > cooldownSeconds * 1000
        // Fixed by adding lastRtp > 0 check
        boolean shouldBlockCooldown = cooldownSeconds > 0 && lastRtp > 0 && (now - lastRtp) < cooldownSeconds * 1000L;
        
        assertFalse(shouldBlockCooldown, "First time user should NOT be blocked by cooldown");
    }

    @Test
    void testCooldownBlocksRecentUser() {
        int cooldownSeconds = 300; // 5 minutes
        long now = System.currentTimeMillis();
        long recentTime = now - (cooldownSeconds * 1000L / 2); // 2.5 minutes ago
        
        // Set last RTP time
        storage.setLastRtpTime(testPlayer, testWorld, recentTime);
        long lastRtp = storage.getLastRtpTime(testPlayer, testWorld);
        
        // Check cooldown
        boolean shouldBlockCooldown = cooldownSeconds > 0 && lastRtp > 0 && (now - lastRtp) < cooldownSeconds * 1000L;
        
        assertTrue(shouldBlockCooldown, "User within cooldown period should be blocked");
    }

    @Test
    void testCooldownAllowsAfterExpiration() {
        int cooldownSeconds = 300; // 5 minutes
        long now = System.currentTimeMillis();
        long oldTime = now - (cooldownSeconds * 1000L + 1000L); // 5 minutes + 1 second ago
        
        // Set last RTP time
        storage.setLastRtpTime(testPlayer, testWorld, oldTime);
        long lastRtp = storage.getLastRtpTime(testPlayer, testWorld);
        
        // Check cooldown
        boolean shouldBlockCooldown = cooldownSeconds > 0 && lastRtp > 0 && (now - lastRtp) < cooldownSeconds * 1000L;
        
        assertFalse(shouldBlockCooldown, "User after cooldown period should NOT be blocked");
    }

    @Test
    void testUsageCounterIncrement() {
        // Initially should be 0
        int dailyCount = storage.getUsageCount(testPlayer, testWorld, "daily");
        assertEquals(0, dailyCount);
        
        // Increment
        storage.incrementUsage(testPlayer, testWorld, "daily");
        dailyCount = storage.getUsageCount(testPlayer, testWorld, "daily");
        assertEquals(1, dailyCount);
        
        // Increment again
        storage.incrementUsage(testPlayer, testWorld, "daily");
        dailyCount = storage.getUsageCount(testPlayer, testWorld, "daily");
        assertEquals(2, dailyCount);
    }

    @Test
    void testDailyLimitEnforcement() {
        int dailyLimit = 10;
        
        // Use RTP 10 times
        for (int i = 0; i < 10; i++) {
            storage.incrementUsage(testPlayer, testWorld, "daily");
        }
        
        int dailyCount = storage.getUsageCount(testPlayer, testWorld, "daily");
        assertEquals(10, dailyCount);
        
        // Check if limit is reached (this check happens in RandomTeleportCommand)
        boolean limitReached = dailyLimit > 0 && dailyCount >= dailyLimit;
        assertTrue(limitReached, "Daily limit should be reached after 10 uses");
        
        // Note: The storage layer itself doesn't enforce limits - it just tracks counts.
        // The enforcement logic is in RandomTeleportCommand which checks the count before allowing RTP.
        // This test verifies the storage correctly tracks usage counts.
        storage.incrementUsage(testPlayer, testWorld, "daily");
        dailyCount = storage.getUsageCount(testPlayer, testWorld, "daily");
        assertEquals(11, dailyCount);
        
        limitReached = dailyLimit > 0 && dailyCount >= dailyLimit;
        assertTrue(limitReached, "Should still be at/over limit");
    }

    @Test
    void testWeeklyLimitEnforcement() {
        int weeklyLimit = 50;
        
        // Use RTP 50 times
        for (int i = 0; i < 50; i++) {
            storage.incrementUsage(testPlayer, testWorld, "weekly");
        }
        
        int weeklyCount = storage.getUsageCount(testPlayer, testWorld, "weekly");
        assertEquals(50, weeklyCount);
        
        // Check if limit is reached
        boolean limitReached = weeklyLimit > 0 && weeklyCount >= weeklyLimit;
        assertTrue(limitReached, "Weekly limit should be reached after 50 uses");
    }

    @Test
    void testResetUsage() {
        // Set up some usage
        storage.incrementUsage(testPlayer, testWorld, "daily");
        storage.incrementUsage(testPlayer, testWorld, "daily");
        storage.incrementUsage(testPlayer, testWorld, "weekly");
        
        assertEquals(2, storage.getUsageCount(testPlayer, testWorld, "daily"));
        assertEquals(1, storage.getUsageCount(testPlayer, testWorld, "weekly"));
        
        // Reset daily
        storage.resetUsage(testPlayer, testWorld, "daily");
        assertEquals(0, storage.getUsageCount(testPlayer, testWorld, "daily"));
        assertEquals(1, storage.getUsageCount(testPlayer, testWorld, "weekly"), "Weekly should not be affected");
        
        // Reset weekly
        storage.resetUsage(testPlayer, testWorld, "weekly");
        assertEquals(0, storage.getUsageCount(testPlayer, testWorld, "weekly"));
    }

    @Test
    void testPersistence() {
        // Set data
        storage.setLastRtpTime(testPlayer, testWorld, 12345678L);
        storage.incrementUsage(testPlayer, testWorld, "daily");
        storage.incrementUsage(testPlayer, testWorld, "weekly");
        storage.save();
        
        // Reload
        storage.reload();
        
        // Verify data persisted
        assertEquals(12345678L, storage.getLastRtpTime(testPlayer, testWorld));
        assertEquals(1, storage.getUsageCount(testPlayer, testWorld, "daily"));
        assertEquals(1, storage.getUsageCount(testPlayer, testWorld, "weekly"));
    }

    @Test
    void testMultiplePlayersIndependent() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        // Player 1 uses RTP
        storage.setLastRtpTime(player1, testWorld, 1000L);
        storage.incrementUsage(player1, testWorld, "daily");
        
        // Player 2 uses RTP
        storage.setLastRtpTime(player2, testWorld, 2000L);
        storage.incrementUsage(player2, testWorld, "daily");
        storage.incrementUsage(player2, testWorld, "daily");
        
        // Verify independence
        assertEquals(1000L, storage.getLastRtpTime(player1, testWorld));
        assertEquals(2000L, storage.getLastRtpTime(player2, testWorld));
        assertEquals(1, storage.getUsageCount(player1, testWorld, "daily"));
        assertEquals(2, storage.getUsageCount(player2, testWorld, "daily"));
    }

    @Test
    void testMultipleWorldsIndependent() {
        String world1 = "world";
        String world2 = "world_nether";
        
        // Same player, different worlds
        storage.setLastRtpTime(testPlayer, world1, 1000L);
        storage.incrementUsage(testPlayer, world1, "daily");
        
        storage.setLastRtpTime(testPlayer, world2, 2000L);
        storage.incrementUsage(testPlayer, world2, "daily");
        storage.incrementUsage(testPlayer, world2, "daily");
        
        // Verify independence
        assertEquals(1000L, storage.getLastRtpTime(testPlayer, world1));
        assertEquals(2000L, storage.getLastRtpTime(testPlayer, world2));
        assertEquals(1, storage.getUsageCount(testPlayer, world1, "daily"));
        assertEquals(2, storage.getUsageCount(testPlayer, world2, "daily"));
    }
}
