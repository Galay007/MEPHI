package otp.repository;

import otp.model.OtpConfig;

public interface OtpConfigRepository {

    OtpConfig getConfig();

    void updateConfig(int length, int ttlSeconds);

}

