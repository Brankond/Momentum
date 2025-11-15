package com.momentum.wallet.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String message,
        List<String> details) {
}
