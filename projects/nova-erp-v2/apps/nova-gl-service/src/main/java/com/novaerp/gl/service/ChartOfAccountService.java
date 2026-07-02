package com.novaerp.gl.service;

import com.novaerp.common.model.ChartOfAccountEntity;
import com.novaerp.common.sak.AccountCategory;
import com.novaerp.common.sak.SakConstants;
import com.novaerp.gl.repository.ChartOfAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChartOfAccountService {

    private static final Logger log = LoggerFactory.getLogger(ChartOfAccountService.class);
    private static final String DEFAULT_TENANT = "default";

    private final ChartOfAccountRepository repository;

    public ChartOfAccountService(ChartOfAccountRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ChartOfAccountEntity> getAllAccounts() {
        return repository.findByTenantIdAndActiveTrue(DEFAULT_TENANT);
    }

    @Transactional(readOnly = true)
    public Optional<ChartOfAccountEntity> getAccountById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ChartOfAccountEntity> getAccountByNameId(String nameId) {
        return repository.findByNameId(nameId);
    }

    @Transactional(readOnly = true)
    public List<ChartOfAccountEntity> getAccountsByCategory(AccountCategory category) {
        return repository.findByCategory(DEFAULT_TENANT, category);
    }

    @Transactional
    public ChartOfAccountEntity createAccount(ChartOfAccountEntity account) {
        if (account.getTenantId() == null || account.getTenantId().isBlank()) {
            account.setTenantId(DEFAULT_TENANT);
        }
        // Validate SAK prefix
        if (!account.hasValidSakPrefix()) {
            throw new IllegalArgumentException(
                "Account code '" + account.getAccountCode() + "' does not match category prefix for " + account.getCategory());
        }
        return repository.save(account);
    }

    @Transactional
    public ChartOfAccountEntity updateAccount(Long id, ChartOfAccountEntity updated) {
        ChartOfAccountEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        
        if (updated.getAccountCode() != null) existing.setAccountCode(updated.getAccountCode());
        if (updated.getAccountNameId() != null) existing.setAccountNameId(updated.getAccountNameId());
        if (updated.getAccountNameEn() != null) existing.setAccountNameEn(updated.getAccountNameEn());
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getNormalBalance() != null) existing.setNormalBalance(updated.getNormalBalance());
        if (updated.getParentCode() != null) existing.setParentCode(updated.getParentCode());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        
        return repository.save(existing);
    }

    @Transactional
    public void softDeleteAccount(Long id) {
        ChartOfAccountEntity account = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        account.setActive(false);
        repository.save(account);
    }

    /** Seed SAK standard accounts if the table is empty. */
    @Transactional
    public int seedSakAccounts() {
        long count = repository.countActiveAccounts(DEFAULT_TENANT);
        if (count > 0) {
            log.info("Chart of accounts already has {} active accounts — skipping seed", count);
            return 0;
        }

        List<SakConstants.SeedAccount> seeds = SakConstants.getStandardSeedAccounts();
        int seeded = 0;
        for (SakConstants.SeedAccount sa : seeds) {
            ChartOfAccountEntity entity = new ChartOfAccountEntity(
                    sa.code(), sa.nameId(), sa.category(), sa.normalBalance(), null);
            entity.setTenantId(DEFAULT_TENANT);
            repository.save(entity);
            seeded++;
        }
        log.info("Seeded {} SAK standard accounts", seeded);
        return seeded;
    }
}
