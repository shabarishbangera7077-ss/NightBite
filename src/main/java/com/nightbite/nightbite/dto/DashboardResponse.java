package com.nightbite.nightbite.dto;

import java.util.List;

public record DashboardResponse(
        long totalOrders,
        List<String> topItems,
        List<String> busiestHours,
        double averageBatchSize,
        double totalDeliveryFeesSaved
) {
}
