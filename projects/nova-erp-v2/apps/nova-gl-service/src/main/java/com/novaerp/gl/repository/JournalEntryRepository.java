package com.novaerp.gl.repository;

import com.novaerp.common.model.JournalEntryEntity;
import com.novaerp.common.model.JournalEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntryEntity, Long> {

    List<JournalEntryEntity> findByTenantIdAndPeriod(String tenantId, String period);

    List<JournalEntryEntity> findByStatus(JournalEntryStatus status);

    Optional<JournalEntryEntity> findByEntryNumber(String entryNumber);

    @Query("SELECT je FROM JournalEntryEntity je LEFT JOIN FETCH je.lines WHERE je.id = :id")
    Optional<JournalEntryEntity> findByIdWithLines(Long id);

    List<JournalEntryEntity> findByTenantId(String tenantId);
}
