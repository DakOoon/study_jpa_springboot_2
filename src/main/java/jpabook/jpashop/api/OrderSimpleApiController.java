package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.simplequery.OrderSimpleQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XToTone (ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }

    @GetMapping("api/v2/simple-orders")
    public Result<SimpleOrderResponse> ordersV2() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderResponse> collect = all.stream()
                .map(o -> new SimpleOrderResponse(
                        o.getId(),
                        o.getMember().getName(), // LAZY 초기화
                        o.getOrderDate(),
                        o.getStatus(),
                        o.getDelivery().getAddress() // LAZY 초기화
                )).collect(Collectors.toList());
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
    private static class SimpleOrderResponse {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
    }

    @GetMapping("api/v3/simple-orders")
    public Result<SimpleOrderResponse> ordersV3() {
        List<Order> all = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderResponse> collect = all.stream()
                .map(o -> new SimpleOrderResponse(
                        o.getId(),
                        o.getMember().getName(),
                        o.getOrderDate(),
                        o.getStatus(),
                        o.getDelivery().getAddress()
                )).collect(Collectors.toList());
        return new Result<>(collect.size(), collect);
    }

    @GetMapping("api/v4/simple-orders")
    public Result<OrderSimpleQueryDto> ordersV4() {
        List<OrderSimpleQueryDto> all = orderSimpleQueryRepository.findOrderDtos();
        return new Result<>(all.size(), all);
    }
}
