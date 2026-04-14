package com.skyblockexp.ezrtp.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * MySQL-backed {@link HotspotStorage} using a HikariCP connection pool.
 *
 * <p>Hotspot writes happen in the background (fire-and-forget via the Bukkit scheduler),
 * so a pool prevents the main thread from blocking on connection acquisition.
 *
 * <p>Table schema (auto-created at startup):
 * <pre>{@code
 * CREATE TABLE IF NOT EXISTS ezrtp_hotspots (
 *   world     VARCHAR(64) NOT NULL,
 *   biome     VARCHAR(64) NOT NULL,
 *   x         DOUBLE      NOT NULL,
 *   y         DOUBLE      NOT NULL,
 *   z         DOUBLE      NOT NULL,
 *   timestamp BIGINT      NOT NULL,
 *   PRIMARY KEY (world, biome, x, z)
 * )
 * }</pre>
 */
public final class MySqlHotspotStorage implements HotspotStorage {

    private static final String TABLE = "ezrtp_hotspots";
    private static final String CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
            + "world     VARCHAR(64) NOT NULL, "
            + "biome     VARCHAR(64) NOT NULL, "
            + "x         DOUBLE      NOT NULL, "
            + "y         DOUBLE      NOT NULL, "
            + "z         DOUBLE      NOT NULL, "
            + "timestamp BIGINT      NOT NULL, "
            + "PRIMARY KEY (world, biome, x, z)"
            + ")";
    private static final String UPSERT =
        "INSERT INTO " + TABLE + " (world, biome, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?, ?)"
            + " ON DUPLICATE KEY UPDATE y = VALUES(y), timestamp = VALUES(timestamp)";
    private static final String SELECT_ALL =
        "SELECT world, biome, x, y, z, timestamp FROM " + TABLE;

    private final HikariDataSource dataSource;
    private final Logger logger;

    /**
     * Creates a new storage instance using the provided JDBC URL and credentials.
     *
     * @param jdbcUrl  JDBC URL, e.g. {@code jdbc:mysql://localhost:3306/ezrtp}
     * @param user     MySQL username
     * @param password MySQL password
     * @param logger   plugin logger for warnings
     */
    public MySqlHotspotStorage(String jdbcUrl, String user, String password, Logger logger) {
        this.logger = logger;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(4);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(3_000);
        config.setIdleTimeout(300_000);
        config.setMaxLifetime(600_000);
        config.setPoolName("ezrtp-hotspot");
        this.dataSource = new HikariDataSource(config);
        setupTable();
    }

    private void setupTable() {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(CREATE_TABLE);
        } catch (SQLException e) {
            logger.warning("Failed to create hotspot table: " + e.getMessage());
        }
    }

    @Override
    public void saveHotspot(String world, String biome, double x, double y, double z) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT)) {
            ps.setString(1, world);
            ps.setString(2, biome);
            ps.setDouble(3, x);
            ps.setDouble(4, y);
            ps.setDouble(5, z);
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Failed to save hotspot: " + e.getMessage());
        }
    }

    @Override
    public List<HotspotRecord> loadAll() {
        List<HotspotRecord> records = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                records.add(new HotspotRecord(
                    rs.getString("world"),
                    rs.getString("biome"),
                    rs.getDouble("x"),
                    rs.getDouble("y"),
                    rs.getDouble("z"),
                    rs.getLong("timestamp")));
            }
        } catch (SQLException e) {
            logger.warning("Failed to load hotspots: " + e.getMessage());
            return Collections.emptyList();
        }
        return records;
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
