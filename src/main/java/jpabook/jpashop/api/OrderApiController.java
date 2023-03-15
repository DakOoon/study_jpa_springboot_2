package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.query.OrderFlatDto;
import jpabook.jpashop.repository.query.OrderItemQueryDto;
import jpabook.jpashop.repository.query.OrderQueryDto;
import jpabook.jpashop.repository.query.OrderQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        all.forEach(order -> {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
            order.getOrderItems()
                    .forEach(o -> o.getItem().getName()); // Lazy 강제 초기화
        });
        return all;
    }


    @GetMapping("api/v2/orders")
    public Result<OrderDto> ordersV2() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = all.stream()
                .map(o -> new OrderDto(
                        o.getId(),
                        o.getMember().getName(), // LAZY 초기화
                        o.getOrderDate(),
                        o.getStatus(),
                        o.getDelivery().getAddress(), // LAZY 초기화
                        o.getOrderItems().stream()
                                .map(oi -> new OrderItemDto(
                                        oi.getItem().getName(),
                                        oi.getOrderPrice(),
                                        oi.getCount()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
        return new Result<>(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    private static class Result<T> {
        private int count;
        private List<T> data;
    }

    @Data
    @AllArgsConstructor
    private static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
    }

    @Data
    @AllArgsConstructor
    private static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;
    }

    @GetMapping("api/v3/orders")
    public Result<OrderDto> ordersV3() {
        List<Order> all = orderRepository.findAllWithItem();
        List<OrderDto> collect = all.stream()
                .map(o -> new OrderDto(
                        o.getId(),
                        o.getMember().getName(), // LAZY 초기화
                        o.getOrderDate(),
                        o.getStatus(),
                        o.getDelivery().getAddress(), // LAZY 초기화
                        o.getOrderItems().stream()
                                .map(oi -> new OrderItemDto(
                                        oi.getItem().getName(),
                                        oi.getOrderPrice(),
                                        oi.getCount()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
        return new Result<>(collect.size(), collect);
    }

    @GetMapping("api/v3.1/orders")
    public Result<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> all = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> collect = all.stream()
                .map(o -> new OrderDto(
                        o.getId(),
                        o.getMember().getName(), // LAZY 초기화
                        o.getOrderDate(),
                        o.getStatus(),
                        o.getDelivery().getAddress(), // LAZY 초기화
                        o.getOrderItems().stream()
                                .map(oi -> new OrderItemDto(
                                        oi.getItem().getName(),
                                        oi.getOrderPrice(),
                                        oi.getCount()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
        return new Result<>(collect.size(), collect);
    }

    @GetMapping("api/v4/orders")
    public Result<OrderQueryDto> ordersV4() {
        List<OrderQueryDto> all = orderQueryRepository.findOrderQueryDtos();
        return new Result<>(all.size(), all);
    }

    @GetMapping("api/v5/orders")
    public Result<OrderQueryDto> ordersV5() {
        List<OrderQueryDto> all = orderQueryRepository.findAllByDto_optimization();
        return new Result<>(all.size(), all);
    }

    @GetMapping("api/v6/orders")
    public Result<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> all = orderQueryRepository.findAllByDto_flat();
        List<OrderQueryDto> transform = all.stream()
                .collect(Collectors.groupingBy(dto -> new OrderQueryDto(
                        dto.getOrderId(),
                        dto.getName(),
                        dto.getOrderDate(),
                        dto.getOrderStatus(),
                        dto.getAddress()), Collectors.mapping(dto -> new OrderItemQueryDto(
                                    dto.getOrderId(),
                                    dto.getItemName(),
                                    dto.getOrderPrice(),
                                    dto.getCount()), Collectors.toList())))
                .entrySet()
                .stream()
                .map(e -> new OrderQueryDto(
                        e.getKey().getOrderId(),
                        e.getKey().getName(),
                        e.getKey().getOrderDate(),
                        e.getKey().getOrderStatus(),
                        e.getKey().getAddress(),
                        e.getValue()))
                .collect(Collectors.toList());

        return new Result<>(transform.size(), transform);
    }
}
