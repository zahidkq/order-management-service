package com.oms.order_service.service;


import com.oms.order_service.dto.CreateOrderRequest;
import com.oms.order_service.entity.Order;
import com.oms.order_service.entity.OrderItem;
import com.oms.order_service.enums.OrderStatus;
import com.oms.order_service.exceptions.InvalidOrderStateException;
import com.oms.order_service.exceptions.OrderNotFoundException;
import com.oms.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = request.getItems().stream().map(req -> {
            OrderItem item = new OrderItem();
            item.setProductId(req.getProductId());
            item.setQuantity(req.getQuantity());
            item.setPrice(req.getPrice());
            item.setOrder(order);
            return item;
        }).toList();

        order.setItems(items);

        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for orderID: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for orderID: " + orderId));
    }

    public Page<Order> getOrdersByUser(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending()
        );

        return orderRepository.findByUserId(userId, pageable);
    }
}

