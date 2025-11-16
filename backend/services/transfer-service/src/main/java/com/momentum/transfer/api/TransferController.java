package com.momentum.transfer.api;

import com.momentum.transfer.api.dto.TransferRequest;
import com.momentum.transfer.api.dto.TransferResponse;
import com.momentum.transfer.domain.TransferSagaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferSagaService transferSagaService;

    public TransferController(TransferSagaService transferSagaService) {
        this.transferSagaService = transferSagaService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> initiate(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferSagaService.initiateTransfer(request);
        HttpStatus status = response.status().isTerminal() ? HttpStatus.OK : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(response);
    }
}
