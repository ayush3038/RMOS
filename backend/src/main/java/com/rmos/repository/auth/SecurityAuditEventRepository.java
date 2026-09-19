package com.rmos.repository.auth;

import com.rmos.domain.auth.SecurityAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, Long> {
}
