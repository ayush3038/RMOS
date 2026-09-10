package com.rmos.repository;

import com.rmos.domain.DataSourceType;
import com.rmos.domain.SourceDataRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SourceDataRecordRepository extends JpaRepository<SourceDataRecord, Long> {
    Optional<SourceDataRecord> findBySourceTypeAndSourceRecordId(DataSourceType sourceType, String sourceRecordId);
}
