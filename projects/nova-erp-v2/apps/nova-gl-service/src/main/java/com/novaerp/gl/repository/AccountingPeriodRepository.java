package com.novaerp.gl.repository;

import com.novaerp.common.sak.AccountingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, Long> {

    Optional<AccountingPeriod> findByTenantIdAndPeriod(String tenantId, String period);

    List<AccountingPeriod> findByTenantId(String tenantId);

    List<AccountingPeriod> findByTenantIdAndClosedFalse(String tenantId);
}
