package dev.iamkavindu.nuledger.ledger.persistence;

import dev.iamkavindu.nuledger.ledger.persistence.entity.JournalLine;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {
    List<JournalLine> findByEntryIdOrderByLineOrderAsc(UUID entryId);
}
