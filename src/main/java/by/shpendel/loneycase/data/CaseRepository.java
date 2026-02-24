package by.shpendel.loneycase.data;

import by.shpendel.loneycase.LoneyCasePlugin;
import by.shpendel.loneycase.config.MainConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CaseRepository {
    private final LoneyCasePlugin plugin;
    private MainConfig cfg;
    private HikariDataSource dataSource;

    public CaseRepository(LoneyCasePlugin plugin, MainConfig cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    public void init() {
        close();
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl("jdbc:mysql://" + cfg.mysqlHost + ":" + cfg.mysqlPort + "/" + cfg.mysqlDatabase + "?useSSL=false&characterEncoding=utf8");
        hc.setUsername(cfg.mysqlUsername);
        hc.setPassword(cfg.mysqlPassword);
        hc.setMaximumPoolSize(8);
        dataSource = new HikariDataSource(hc);
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS player_cases (uuid VARCHAR(36) NOT NULL, case_id VARCHAR(64) NOT NULL, amount INT NOT NULL, PRIMARY KEY(uuid, case_id))");
        } catch (SQLException e) {
            plugin.getLogger().severe("Database init error: " + e.getMessage());
        }
    }

    public void reconfigure(MainConfig cfg) {
        this.cfg = cfg;
        init();
    }

    public Map<String, Integer> getCases(UUID uuid) {
        Map<String, Integer> map = new HashMap<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT case_id, amount FROM player_cases WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("case_id"), rs.getInt("amount"));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Read cases error: " + e.getMessage());
        }
        return map;
    }

    public int getAmount(UUID uuid, String caseId) {
        return getCases(uuid).getOrDefault(caseId.toLowerCase(), 0);
    }

    public void add(UUID uuid, String caseId, int amount) {
        int next = Math.max(0, getAmount(uuid, caseId) + amount);
        set(uuid, caseId, next);
    }

    public boolean remove(UUID uuid, String caseId, int amount) {
        int now = getAmount(uuid, caseId);
        if (now < amount) return false;
        set(uuid, caseId, now - amount);
        return true;
    }

    private void set(UUID uuid, String caseId, int amount) {
        caseId = caseId.toLowerCase();
        try (Connection c = dataSource.getConnection()) {
            if (amount <= 0) {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM player_cases WHERE uuid=? AND case_id=?")) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, caseId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO player_cases(uuid, case_id, amount) VALUES(?,?,?) ON DUPLICATE KEY UPDATE amount=?")) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, caseId);
                    ps.setInt(3, amount);
                    ps.setInt(4, amount);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Write cases error: " + e.getMessage());
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }
}
