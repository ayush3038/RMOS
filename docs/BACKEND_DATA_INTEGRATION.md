# Backend Data Integration Architecture (Milestone 3.4)

## Architecture Overview

The purpose of this layer is to establish internal backend contracts for the first stage of the RMOS architecture data processing. 

### Source-System Abstraction
We are dealing with integration from multiple Indian Railways source systems such as TMS, SMMS, TDMS, BDMS, COA, and TIMETABLE.
To classify the initial boundary payload from these domains, we use the `DataSourceType` enum. Ingestion payloads carry a status (`IngestionStatus`) which transitions through states like RECEIVED, VALIDATED, REJECTED, and PROCESSED.

### Normalized Source Records
Instead of dumping raw arbitrary JSON straight into the DB, the RMOS system maps incoming payloads to the `SourceDataRecord` entity representing normalized storage of the ingest event. This decouples the core domain processing from raw operational noise and enforces mandatory referential fields (e.g. `sourceRecordId` and `sourceType`).

### Canonical Asset-ID Mapping
Because assets (locomotives, tracks, signaling gear) are referenced by different IDs across disjointed Indian Railways systems, RMOS resolves aliases into one single RMOS Canonical Asset ID. This relationship is stored within `CanonicalAssetMapping`, enabling the mapping resolution stage before core logic evaluates maintenance requirements.

## Freshness Classification

Ingested payloads may have timestamps indicating exactly when they were created or updated in the underlying source system. The `DataFreshnessService` inspects these `receivedAt` times and classifies them simply as `FRESH`, `STALE`, or `UNKNOWN`.

This configurable threshold isn't defining official Indian Railways temporal mandates—instead, it ensures the RMOS application-level logic knows what data is relatively new compared to data that might require re-fetching or is out-of-sync. 
The threshold is defined in `application.yml` via `rmos.integration.data-freshness.threshold-minutes` (default 60 minutes).

## Design Philosophy & Next Steps

Currently, **real external connectors are deferred**. There are no external API outbound calls or Kafka queues polling. Validating the storage schemas, endpoints, domain models, and data validation bounds provides the foundation needed for future stages where external ingest logic will be wired up to these newly established data sinks.
