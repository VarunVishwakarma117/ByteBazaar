package org.varun.bytebazaar.cart;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.varun.bytebazaar.cart.CartDtos.AddToCartRequest;
import org.varun.bytebazaar.cart.CartDtos.CartResponse;
import org.varun.bytebazaar.cart.CartDtos.UpdateCartItemRequest;
import org.varun.bytebazaar.users.UserAccount;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    CartResponse getCart(@AuthenticationPrincipal UserAccount user) {
        return cartService.getCart(user);
    }

    @PostMapping
    CartResponse add(@AuthenticationPrincipal UserAccount user, @Valid @RequestBody AddToCartRequest request) {
        return cartService.add(user, request);
    }

    @PatchMapping("/{itemId}")
    CartResponse update(@AuthenticationPrincipal UserAccount user, @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.update(user, itemId, request.quantity());
    }

    @DeleteMapping("/{itemId}")
    CartResponse remove(@AuthenticationPrincipal UserAccount user, @PathVariable Long itemId) {
        return cartService.remove(user, itemId);
    }
}
