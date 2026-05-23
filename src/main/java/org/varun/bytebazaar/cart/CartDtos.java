package org.varun.bytebazaar.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.varun.bytebazaar.catalog.Product;

public final class CartDtos {
    private CartDtos() {
    }

    public record AddToCartRequest(@NotNull Long productId, @Min(1) int quantity) {
    }

    public record UpdateCartItemRequest(@Min(1) int quantity) {
    }

    public record CartItemResponse(
            Long id,
            Long productId,
            String productName,
            String brand,
            String imageUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal) {
        static CartItemResponse from(CartItem item) {
            Product product = item.getProduct();
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartItemResponse(item.getId(), product.getId(), product.getName(), product.getBrand(),
                    product.getImageUrl(), product.getPrice(), item.getQuantity(), subtotal);
        }
    }

    public record CartResponse(List<CartItemResponse> items, BigDecimal total) {
    }
}
