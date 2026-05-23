package org.varun.bytebazaar.orders;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.varun.bytebazaar.orders.OrderDtos.CheckoutRequest;
import org.varun.bytebazaar.orders.OrderDtos.OrderResponse;
import org.varun.bytebazaar.users.UserAccount;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    // Handles order list and checkout API requests.
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    List<OrderResponse> list(@AuthenticationPrincipal UserAccount user) {
        // Returns orders placed by current user.
        return orderService.list(user);
    }

    @PostMapping
    OrderResponse checkout(@AuthenticationPrincipal UserAccount user, @Valid @RequestBody CheckoutRequest request) {
        // Converts cart into a new order.
        return orderService.checkout(user, request);
    }
}
