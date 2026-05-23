package org.varun.bytebazaar.orders;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.bytebazaar.users.UserAccount;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findByUserOrderByCreatedAtDesc(UserAccount user);
}
