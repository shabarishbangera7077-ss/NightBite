package com.nightbite.nightbite.service;

import com.nightbite.nightbite.entity.*;
import com.nightbite.nightbite.exception.BusinessException;
import com.nightbite.nightbite.repository.BatchRepository;
import com.nightbite.nightbite.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchRepository batchRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Batch findOrCreateBatch(Vendor vendor, String hostelBlock, LocalDateTime slotTime) {
        LocalDateTime cutoffTime = slotTime.minusMinutes(vendor.getCutoffMinutes());
        return batchRepository.findByVendorAndHostelBlockAndSlotTime(vendor, hostelBlock, slotTime)
                .orElseGet(() -> {
                    Batch batch = Batch.builder()
                            .vendor(vendor)
                            .hostelBlock(hostelBlock)
                            .slotTime(slotTime)
                            .cutoffTime(cutoffTime)
                            .status(BatchStatus.OPEN)
                            .memberCount(0)
                            .finalFeePerStudent(0)
                            .build();
                    try {
                        return batchRepository.saveAndFlush(batch);
                    } catch (DataIntegrityViolationException e) {
                        return batchRepository.findByVendorAndHostelBlockAndSlotTime(vendor, hostelBlock, slotTime)
                                .orElseThrow(() -> e);
                    }
                });
    }

    public List<Batch> getBatchesForVendor(Vendor vendor, BatchStatus status) {
        return batchRepository.findByVendorAndStatusOrderBySlotTimeAsc(vendor, status);
    }

    @Transactional
    public Batch updateBatchStatus(Long batchId, BatchStatus newStatus) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BusinessException("Batch not found"));
        if (newStatus == null) {
            throw new BusinessException("Status is required");
        }

        List<BatchStatus> allowed = switch (batch.getStatus()) {
            case OPEN -> List.of(BatchStatus.LOCKED);
            case LOCKED -> List.of(BatchStatus.PREPARING, BatchStatus.CANCELLED);
            case PREPARING -> List.of(BatchStatus.OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> List.of(BatchStatus.DELIVERED);
            case DELIVERED, CANCELLED -> List.of();
        };

        if (!allowed.contains(newStatus)) {
            throw new BusinessException("Invalid batch status transition from " + batch.getStatus() + " to " + newStatus);
        }

        batch.setStatus(newStatus);
        for (Order order : orderRepository.findByBatchOrderByCreatedAtAsc(batch)) {
            order.setStatus(convertToOrderStatus(newStatus));
        }
        return batchRepository.save(batch);
    }

    private OrderStatus convertToOrderStatus(BatchStatus status) {
        return switch (status) {
            case OPEN -> OrderStatus.PLACED;
            case LOCKED -> OrderStatus.CONFIRMED;
            case PREPARING -> OrderStatus.PREPARING;
            case OUT_FOR_DELIVERY -> OrderStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> OrderStatus.DELIVERED;
            case CANCELLED -> OrderStatus.CANCELLED;
        };
    }

    @Transactional
    public void lockExpiredBatches(FeeService feeService) {
        List<Batch> batches = batchRepository.findByStatusAndCutoffTimeBefore(BatchStatus.OPEN, LocalDateTime.now());
        for (Batch batch : batches) {
            Vendor vendor = batch.getVendor();
            batch.setStatus(BatchStatus.LOCKED);
            batch.setFinalFeePerStudent(feeService.calculateCurrentFee(batch.getMemberCount(), vendor.getBaseDeliveryFee()));

            for (Order order : orderRepository.findByBatchOrderByCreatedAtAsc(batch)) {
                order.setStatus(OrderStatus.CONFIRMED);
                order.setDeliveryFee(batch.getFinalFeePerStudent());
                order.setGrandTotal(order.getItemsTotal() + batch.getFinalFeePerStudent());
            }

            if (batch.getMemberCount() < vendor.getMinBatchSize()) {
                batch.setStatus(BatchStatus.CANCELLED);
                for (Order order : orderRepository.findByBatchOrderByCreatedAtAsc(batch)) {
                    order.setStatus(OrderStatus.CANCELLED);
                }
            }
            batchRepository.save(batch);
        }
    }
}
