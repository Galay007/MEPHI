package otp.repository;

import otp.model.OtpCode;

import java.util.List;


public interface OtpCodeRepository {


    void save(OtpCode code);
    OtpCode findByCode(String code);
    List<OtpCode> findAllByUser(Long userId);
    void markAsUsed(Long id);
    void markAsExpired(Long id);
    void deleteAllOtpByUserId(Long userId);
}

