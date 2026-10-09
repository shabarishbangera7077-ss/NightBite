package com.nightbite.nightbite.service;

import org.springframework.stereotype.Service;

@Service
public class FeeService {

    public int calculateCurrentFee(int memberCount, int baseDeliveryFee) {
        if (memberCount <= 0) {
            return baseDeliveryFee;
        }
        return (int) Math.ceil(baseDeliveryFee / (double) memberCount);
    }
}
