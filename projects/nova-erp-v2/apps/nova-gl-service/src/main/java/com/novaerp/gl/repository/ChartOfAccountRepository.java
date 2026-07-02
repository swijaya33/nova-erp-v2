package com.novaerp.gl.repository;

import com.novaerp.common.model.ChartOfAccountEntity;
import com.novaerp.common.sak.AccountCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccountEntity, Long> {

    List<ChartOfAccountEntity> findByTenantIdAndActiveTrue(String tenantId);

    Optional<ChartOfAccountEntity> findByNameId(String accountNameId);

    @Query("SELECT a FROM ChartOfAccountEntity a WHERE a.tenantId = :tenantId AND a.category = :category AND a.active = true")
    List<ChartOfAccountEntity> findByCategory(String tenantId, AccountCategory category);

    boolean existsByTenantIdAndAccountCode(String tenantId, String accountCode);

    @Query("SELECT COUNT(a) FROM ChartOfAccountEntity a WHERE a.tenantId = :tenantId AND a.active = true")
    long countActiveAccounts(String tenantId);
}
