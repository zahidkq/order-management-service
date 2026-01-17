package com.oms.order_service.controller;

import com.oms.order_service.dto.ApiResponse;
import com.oms.order_service.dto.CreateOrderRequest;
import com.oms.order_service.dto.OrderResponse;
import com.oms.order_service.entity.Order;
import com.oms.order_service.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(request);

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @PathVariable Long orderId) {

        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                ApiResponse.success(orderService.getOrder(orderId))
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<Order>>> getOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getOrdersByUser(userId, page, size)
                )
        );
    }
}
