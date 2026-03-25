package com.hoangtien2k3.paymentservice.service.impl;

import com.google.gson.Gson;
import com.hoangtien2k3.paymentservice.constant.KafkaConstant;
import com.hoangtien2k3.paymentservice.dto.KafkaPaymentDto;
import com.hoangtien2k3.paymentservice.dto.OrderDto;
import com.hoangtien2k3.paymentservice.dto.PaymentDto;
import com.hoangtien2k3.paymentservice.dto.UserDto;
import com.hoangtien2k3.paymentservice.event.EventProducer;
import com.hoangtien2k3.paymentservice.exception.wrapper.PaymentNotFoundException;
import com.hoangtien2k3.paymentservice.helper.PaymentMappingHelper;
import com.hoangtien2k3.paymentservice.repository.PaymentRepository;
import com.hoangtien2k3.paymentservice.security.JwtTokenFilter;
import com.hoangtien2k3.paymentservice.service.CallAPI;
import com.hoangtien2k3.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final CallAPI callAPI;
    private final EventProducer eventProducer;

    private final Gson gson = new Gson();

    @Override
    public Mono<List<PaymentDto>> findAll() {
        log.info("*** Fetch all payments ***");
        return Mono.fromSupplier(() -> paymentRepository.findAll()
                        .stream()
                        .map(PaymentMappingHelper::map)
                        .toList())
                .flatMap(list -> Flux.fromIterable(list)
                        .flatMap(paymentDto -> {
                            String token = JwtTokenFilter.getTokenFromRequest();
                            return callAPI.receiverPaymentDto(paymentDto.getOrderId(), token)
                                    .map(orderDto -> {
                                        paymentDto.setOrderDto(modelMapper.map(orderDto, OrderDto.class));
                                        return paymentDto;
                                    })
                                    .onErrorResume(e -> Mono.just(paymentDto));
                        })
                        .collectList()
                );
    }

    @Override
    public Mono<Page<PaymentDto>> findAll(int page, int size, String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return Mono.fromSupplier(() -> paymentRepository.findAll(pageable)
                        .map(PaymentMappingHelper::map))
                .flatMap(paymentDtos -> Flux.fromIterable(paymentDtos)
                        .flatMap(paymentDto -> {
                            String token = JwtTokenFilter.getTokenFromRequest();
                            return callAPI.receiverPaymentDto(paymentDto.getOrderId(), token)
                                    .map(orderDto -> {
                                        paymentDto.setOrderDto(modelMapper.map(orderDto, OrderDto.class));
                                        return paymentDto;
                                    })
                                    .onErrorResume(e -> Mono.just(paymentDto));
                        })
                        .collectList()
                        .map(list -> new PageImpl<>(list, pageable, list.size()))
                );
    }

    @Override
    public Mono<PaymentDto> findById(Integer paymentId) {
        return Mono.fromSupplier(() -> paymentRepository.findById(paymentId)
                        .map(PaymentMappingHelper::map)
                        .orElseThrow(() -> new PaymentNotFoundException("Payment with id: " + paymentId + " not found")))
                .flatMap(paymentDto -> {
                    String token = JwtTokenFilter.getTokenFromRequest();
                    return callAPI.receiverPaymentDto(paymentDto.getOrderDto().getOrderId(), token)
                            .flatMap(orderDto -> {
                                paymentDto.setOrderDto(modelMapper.map(orderDto, OrderDto.class));
                                return callAPI.receiverUserDto(paymentDto.getUserId(), token)
                                        .map(userDto -> {
                                            paymentDto.setUserDto(modelMapper.map(userDto, UserDto.class));
                                            return paymentDto;
                                        })
                                        .switchIfEmpty(Mono.just(paymentDto));
                            })
                            .onErrorResume(e -> Mono.just(paymentDto));
                });
    }

    @Override
    public Mono<OrderDto> getOrderDto(Integer orderId) {
        String token = JwtTokenFilter.getTokenFromRequest();
        return callAPI.receiverPaymentDto(orderId, token)
                .map(orderDto -> modelMapper.map(orderDto, OrderDto.class));
    }

    @Override
    public Mono<PaymentDto> save(PaymentDto paymentDto) {
        return Mono.just(paymentDto)
                .filter(dto -> !paymentRepository.existsByOrderIdAndIsPayed(dto.getOrderId()))
                .switchIfEmpty(Mono.error(new PaymentNotFoundException("Order already paid")))
                .flatMap(dto -> Mono.fromCallable(() -> PaymentMappingHelper.map(paymentRepository.save(PaymentMappingHelper.map(dto)))))
                .flatMap(saved -> {
                    KafkaPaymentDto kafkaPaymentDto = KafkaPaymentDto.builder()
                            .paymentId(saved.getPaymentId())
                            .isPayed(saved.getIsPayed())
                            .paymentStatus(saved.getPaymentStatus())
                            .orderId(saved.getOrderId())
                            .userId(saved.getUserId())
                            .build();
                    return eventProducer.send(KafkaConstant.STATUS_PAYMENT_SUCCESSFUL, gson.toJson(kafkaPaymentDto))
                            .thenReturn(saved);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<PaymentDto> update(PaymentDto paymentDto) {
        return Mono.fromSupplier(() -> paymentRepository.save(PaymentMappingHelper.map(paymentDto)))
                .map(PaymentMappingHelper::map);
    }

    @Override
    public Mono<PaymentDto> update(Integer paymentId, PaymentDto paymentDto) {
        return findById(paymentId).flatMap(existing -> {
            modelMapper.map(paymentDto, existing);
            return Mono.fromSupplier(() -> paymentRepository.save(PaymentMappingHelper.map(existing)))
                    .map(PaymentMappingHelper::map);
        }).switchIfEmpty(Mono.error(new PaymentNotFoundException("Payment not found")));
    }

    @Override
    public Mono<Void> deleteById(Integer paymentId) {
        return Mono.fromRunnable(() -> paymentRepository.deleteById(paymentId));
    }
}
