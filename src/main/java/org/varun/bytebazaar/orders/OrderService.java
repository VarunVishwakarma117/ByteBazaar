package org.varun.bytebazaar.orders;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.varun.bytebazaar.cart.CartItem;
import org.varun.bytebazaar.cart.CartItemRepository;
import org.varun.bytebazaar.catalog.Product;
import org.varun.bytebazaar.catalog.ProductRepository;
import org.varun.bytebazaar.orders.OrderDtos.CheckoutRequest;
import org.varun.bytebazaar.orders.OrderDtos.OrderResponse;
import org.varun.bytebazaar.users.UserAccount;

@Service
public class OrderService {
    // Contains checkout and order history logic.
    private final OrderRepository orders;
    private final CartItemRepository cartItems;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, CartItemRepository cartItems, ProductRepository products) {
        this.orders = orders;
        this.cartItems = cartItems;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(UserAccount user) {
        // Fetch newest orders first for logged-in user.
        return orders.findByUserOrderByCreatedAtDesc(user).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse checkout(UserAccount user, CheckoutRequest request) {
        // Get current cart before creating order.
        List<CartItem> cart = cartItems.findByUserOrderByIdAsc(user);
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Create main order object.
        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setShippingAddress(request.shippingAddress().trim());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart) {
            // Check stock and reduce product stock.
            Product product = cartItem.getProduct();
            if (cartItem.getQuantity() > product.getStock()) {
                throw new IllegalArgumentException("Only " + product.getStock() + " units available for " + product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            products.save(product);

            // Copy cart item details into order item.
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setBrand(product.getBrand());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }

        // Save order, clear cart, and return order response.
        order.setTotalAmount(total);
        CustomerOrder saved = orders.save(order);
        cartItems.deleteByUser(user);
        return OrderResponse.from(saved);
    }
}
