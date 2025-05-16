package otp.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import otp.service.DatabaseAccessService;
import otp.model.OtpConfig;

import java.sql.*;

public class OtpConfigRepositoryImpl implements OtpConfigRepository {
    private static final Logger logger = LoggerFactory.getLogger(OtpConfigRepositoryImpl.class);
    private static final String SELECT_CONFIG_SQL =
            "SELECT id, length, ttl_seconds FROM otp_config";
    private static final String UPDATE_CONFIG_SQL =
            "UPDATE otp_config SET length = ?, ttl_seconds = ? WHERE id = 1";

    @Override
    public OtpConfig getConfig() {
        try (Connection conn = DatabaseAccessService.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_CONFIG_SQL);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                OtpConfig cfg = new OtpConfig();
                cfg.setId(rs.getLong("id"));
                cfg.setLength(rs.getInt("length"));
                cfg.setTtlSeconds(rs.getInt("ttl_seconds"));
                logger.info("Loaded OTP config: {}", cfg);
                return cfg;
            }
        } catch (SQLException e) {
            logger.error("Error loading OTP config: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        logger.warn("No OTP config found in database");
        return null;
    }

    @Override
    public void updateConfig(int length, int ttlSeconds) {
        try (Connection conn = DatabaseAccessService.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_CONFIG_SQL)) {

            ps.setInt(1, length);
            ps.setInt(2, ttlSeconds);

            int affected = ps.executeUpdate();
            logger.info("Updated OTP config (id=1): length={}, ttlSeconds={} ({} rows)",
                    length, ttlSeconds, affected);
        } catch (SQLException e) {
            logger.error("Error updating OTP config: {}",  e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

