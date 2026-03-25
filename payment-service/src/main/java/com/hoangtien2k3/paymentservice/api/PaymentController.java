package com.hoangtien2k3.paymentservice.api;

import com.hoangtien2k3.paymentservice.dto.OrderDto;
import com.hoangtien2k3.paymentservice.dto.PaymentDto;
import com.hoangtien2k3.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public Mono<ResponseEntity<List<PaymentDto>>> findAll() {
        return paymentService.findAll()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(Collections.emptyList()));
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<Page<PaymentDto>>> findAll(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "paymentId") String sortBy,
                                                          @RequestParam(defaultValue = "asc") String sortOrder) {
        return paymentService.findAll(page, size, sortBy, sortOrder)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{paymentId}")
    public Mono<ResponseEntity<PaymentDto>> findById(@PathVariable Integer paymentId) {
        return paymentService.findById(paymentId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/getOrder/{orderId}")
    public Mono<ResponseEntity<OrderDto>> getOrder(@PathVariable Integer orderId) {
        return paymentService.getOrderDto(orderId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<PaymentDto>> save(@RequestBody PaymentDto paymentDto) {
        return paymentService.save(paymentDto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @PutMapping("/{paymentId}")
    public Mono<ResponseEntity<PaymentDto>> update(@PathVariable Integer paymentId,
                                                   @RequestBody PaymentDto paymentDto) {
        return paymentService.update(paymentId, paymentDto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{paymentId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Integer paymentId) {
        return paymentService.deleteById(paymentId)
                .map(deleted -> ResponseEntity.ok(deleted))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
