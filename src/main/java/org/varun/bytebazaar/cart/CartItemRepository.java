package org.varun.bytebazaar.cart;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.bytebazaar.users.UserAccount;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserOrderByIdAsc(UserAccount user);

    Optional<CartItem> findByUserAndProductId(UserAccount user, Long productId);

    void deleteByUser(UserAccount user);
}
