package com.novaerp.gl.repository;

import com.novaerp.common.model.JournalEntryLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLineEntity, Long> {

    List<JournalEntryLineEntity> findByJournalEntryId(Long journalEntryId);

    void deleteByJournalEntryId(Long journalEntryId);
}
