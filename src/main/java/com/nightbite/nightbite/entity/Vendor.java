package com.nightbite.nightbite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shopName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private LocalTime openTime = LocalTime.of(18, 0);

    @Column(nullable = false)
    private LocalTime closeTime = LocalTime.of(23, 59);

    @Column(nullable = false)
    private boolean approved;

    @Column(nullable = false)
    private int slotMinutes = 30;

    @Column(nullable = false)
    private int cutoffMinutes = 10;

    @Column(nullable = false)
    private int baseDeliveryFee = 40;

    @Column(nullable = false)
    private int minBatchSize = 1;
}
