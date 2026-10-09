package com.nightbite.nightbite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "batches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vendor_id", "hostel_block", "slot_time"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "hostel_block", nullable = false)
    private String hostelBlock;

    @Column(name = "slot_time", nullable = false)
    private LocalDateTime slotTime;

    @Column(name = "cutoff_time", nullable = false)
    private LocalDateTime cutoffTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status = BatchStatus.OPEN;

    @Column(nullable = false)
    private int memberCount = 0;

    @Column(nullable = false)
    private int finalFeePerStudent = 0;
}
