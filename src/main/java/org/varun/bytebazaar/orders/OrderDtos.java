package org.varun.bytebazaar.orders;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {
    }

    public record CheckoutRequest(@NotBlank String shippingAddress) {
    }

    public record OrderItemResponse(
            Long productId,
            String productName,
            String brand,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal) {
        static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getProductId(), item.getProductName(), item.getBrand(),
                    item.getUnitPrice(), item.getQuantity(), item.getSubtotal());
        }
    }

    public record OrderResponse(
            Long id,
            OrderStatus status,
            BigDecimal totalAmount,
            String shippingAddress,
            Instant createdAt,
            List<OrderItemResponse> items) {
        static OrderResponse from(CustomerOrder order) {
            return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount(),
                    order.getShippingAddress(), order.getCreatedAt(),
                    order.getItems().stream().map(OrderItemResponse::from).toList());
        }
    }
}
