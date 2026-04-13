package com.skyblockexp.ezrtp.config;

/**
 * An immutable named teleport center that can be targeted via {@code /rtp <name>}.
 *
 * <p>Named centers are declared under {@code centers.named} in {@code rtp.yml} and are
 * resolved case-insensitively at command time.
 *
 * @param name  the raw (case-preserving) name as defined in configuration
 * @param world the target world name
 * @param x     the center X coordinate
 * @param z     the center Z coordinate
 */
public record NamedCenter(String name, String world, int x, int z) {}
