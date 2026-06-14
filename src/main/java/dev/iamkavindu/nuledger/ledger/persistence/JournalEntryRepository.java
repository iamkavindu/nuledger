package dev.iamkavindu.nuledger.ledger.persistence;

import dev.iamkavindu.nuledger.ledger.persistence.entity.JournalEntry;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    Optional<JournalEntry> findByIdempotencyKey(String idempotencyKey);

    Optional<JournalEntry> findByReversesEntryId(UUID reversesEntryId);
}
