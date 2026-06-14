package dev.iamkavindu.nuledger.ledger.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "journal_lines")
public class JournalLine extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "entry_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID entryId;

    @Column(name = "account_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private LineDirection direction;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;

    @Column(name = "line_order", nullable = false)
    private int lineOrder;

    public enum LineDirection {
        DEBIT,
        CREDIT
    }
}
