package com.rmos.repository;

import com.rmos.domain.CanonicalAssetMapping;
import com.rmos.domain.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CanonicalAssetMappingRepository extends JpaRepository<CanonicalAssetMapping, Long> {
    Optional<CanonicalAssetMapping> findBySourceTypeAndSourceAssetId(DataSourceType sourceType, String sourceAssetId);
}
