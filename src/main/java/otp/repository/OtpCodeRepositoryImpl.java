package otp.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import otp.service.DatabaseAccessService;
import otp.model.OtpCode;
import otp.model.OtpStatus;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-реализация OtpCodeDao.
 * Управляет записями OTP-кодов в таблице otp_codes.
 */
public class OtpCodeRepositoryImpl implements OtpCodeRepository {
    private static final Logger logger = LoggerFactory.getLogger(OtpCodeRepositoryImpl.class);

    private static final String INSERT_SQL =
            "INSERT INTO otp_codes (user_id, operation_id, code, status, created_at) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_CODE_SQL =
            "SELECT * FROM otp_codes WHERE code = ? AND status = 'ACTIVE'";
    private static final String SELECT_BY_USER_SQL =
            "SELECT * FROM otp_codes WHERE user_id = ?";
    private static final String UPDATE_MARK_USED_SQL =
            "UPDATE otp_codes SET status = 'USED' WHERE id = ?";
    private static final String UPDATE_MARK_EXPIRED_SQL =
            "UPDATE otp_codes SET status = 'EXPIRED' WHERE id = ?";
    private static final String DELETE_BY_USER_SQL =
            "DELETE FROM otp_codes WHERE user_id = ?";

    @Override
    public void save(OtpCode code) {
        if (code.getCreatedAt() == null) {
            code.setCreatedAt(LocalDateTime.now());
        }
        try (Connection conn = DatabaseAccessService.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, code.getUserId());
            if (code.getOperationId() != null) {
                ps.setString(2, code.getOperationId());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, code.getCode());
            ps.setString(4, code.getStatus().name());
            ps.setTimestamp(5, Timestamp.valueOf(code.getCreatedAt()));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Saving OTP code failed, no rows affected.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    code.setId(keys.getLong(1));
                }
            }
            logger.info("Saved OTP code: {}", code);
        } catch (SQLException e) {
            logger.error("Error saving OTP code [{}]: {}", code.getCode(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public OtpCode findByCode(String code) {
        try (Connection conn = DatabaseAccessService.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CODE_SQL)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OtpCode found = mapRow(rs);
                    logger.info("Found OTP by code {}: {}", code, found);
                    return found;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding OTP by code [{}]: {}", code, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<OtpCode> findAllByUser(Long userId) {
        List<OtpCode> list = new ArrayList<>();
        try (Connection conn = DatabaseAccessService.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USER_SQL)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            logger.info("Found {} OTP codes for user {}", list.size(), userId);
        } catch (SQLException e) {
            logger.error("Error finding OTP codes for user [{}]: {}", userId, e.getMessage());
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public void markAsUsed(Long id) {
        try (Connection conn = DatabaseAccessService.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_MARK_USED_SQL)) {
            ps.setLong(1, id);

            int affected = ps.executeUpdate();
            logger.info("Marked OTP id {} as USED ({} rows affected)", id, affected);
        } catch (SQLException e) {
            logger.error("Error marking OTP id [{}] as USED: {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markAsExpired(Long id) {
        try (Connection conn = DatabaseAccessService.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_MARK_EXPIRED_SQL)) {
            ps.setLong(1, id);

            int affected = ps.executeUpdate();
            logger.info("Marked OTP id {} as EXPIRED ({} rows affected)", id, affected);
        } catch (SQLException e) {
            logger.error("Error marking OTP id [{}] as EXPIRED: {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAllOtpByUserId(Long userId) {
        try (Connection conn = DatabaseAccessService.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_USER_SQL)) {
            ps.setLong(1, userId);

            int affected = ps.executeUpdate();
            logger.info("Deleted {} OTP codes for user {}", affected, userId);
        } catch (SQLException e) {
            logger.error("Error deleting OTP codes for user [{}]: {}", userId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private OtpCode mapRow(ResultSet rs) throws SQLException {
        OtpCode code = new OtpCode();
        code.setId(rs.getLong("id"));
        code.setUserId(rs.getLong("user_id"));
        code.setOperationId(rs.getString("operation_id"));
        code.setCode(rs.getString("code"));
        code.setStatus(OtpStatus.valueOf(rs.getString("status")));
        code.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return code;
    }
}

