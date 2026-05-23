package org.varun.bytebazaar.cart;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.varun.bytebazaar.cart.CartDtos.AddToCartRequest;
import org.varun.bytebazaar.cart.CartDtos.CartItemResponse;
import org.varun.bytebazaar.cart.CartDtos.CartResponse;
import org.varun.bytebazaar.catalog.Product;
import org.varun.bytebazaar.catalog.ProductRepository;
import org.varun.bytebazaar.users.UserAccount;

@Service
public class CartService {
    // Contains cart business logic like add, update, remove, and total calculation.
    private final CartItemRepository cartItems;
    private final ProductRepository products;

    public CartService(CartItemRepository cartItems, ProductRepository products) {
        this.cartItems = cartItems;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(UserAccount user) {
        // Convert database cart rows into response objects.
        List<CartItemResponse> items = cartItems.findByUserOrderByIdAsc(user).stream()
                .map(CartItemResponse::from)
                .toList();

        // Add all item subtotals to get cart total.
        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, total);
    }

    @Transactional
    public CartResponse add(UserAccount user, AddToCartRequest request) {
        // Product must exist and be active before adding to cart.
        Product product = products.findById(request.productId())
                .filter(Product::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Use existing cart item if product is already in cart, otherwise create one.
        CartItem item = cartItems.findByUserAndProductId(user, request.productId()).orElseGet(() -> {
            CartItem created = new CartItem();
            created.setUser(user);
            created.setProduct(product);
            created.setQuantity(0);
            return created;
        });

        // Increase quantity and check stock limit.
        int nextQuantity = item.getQuantity() + request.quantity();
        ensureInStock(product, nextQuantity);
        item.setQuantity(nextQuantity);
        cartItems.save(item);
        return getCart(user);
    }

    @Transactional
    public CartResponse update(UserAccount user, Long itemId, int quantity) {
        // User can update only his/her own cart item.
        CartItem item = cartItems.findById(itemId)
                .filter(cartItem -> cartItem.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        ensureInStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return getCart(user);
    }

    @Transactional
    public CartResponse remove(UserAccount user, Long itemId) {
        // User can remove only his/her own cart item.
        CartItem item = cartItems.findById(itemId)
                .filter(cartItem -> cartItem.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        cartItems.delete(item);
        return getCart(user);
    }

    private void ensureInStock(Product product, int quantity) {
        // Prevent cart quantity from going above available stock.
        if (quantity > product.getStock()) {
            throw new IllegalArgumentException("Only " + product.getStock() + " units available for " + product.getName());
        }
    }
}
