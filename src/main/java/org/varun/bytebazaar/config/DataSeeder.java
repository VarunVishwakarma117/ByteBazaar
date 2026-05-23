package org.varun.bytebazaar.config;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.varun.bytebazaar.catalog.Product;
import org.varun.bytebazaar.catalog.ProductRepository;
import org.varun.bytebazaar.users.Role;
import org.varun.bytebazaar.users.UserAccount;
import org.varun.bytebazaar.users.UserRepository;

@Configuration
public class DataSeeder {
    // Adds demo products and demo user when database is empty.
    @Bean
    CommandLineRunner seedData(ProductRepository products, UserRepository users, PasswordEncoder passwordEncoder) {
        return args -> {
            // Add sample products only once.
            if (products.count() == 0) {
                products.saveAll(List.of(
                        product("HP Pavilion Laptop", "HP", "Laptops",
                                "15 inch laptop with 16GB RAM and 512GB SSD.",
                                "129999.00", 8, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=900&q=80"),
                        product("Lenovo Slim Laptop", "Lenovo", "Laptops",
                                "Lightweight laptop for study and office work.",
                                "84999.00", 14, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80"),
                        product("Samsung Galaxy Phone", "Samsung", "Mobiles",
                                "5G phone with AMOLED display and 256GB storage.",
                                "64999.00", 25, "https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=900&q=80"),
                        product("Wireless Earbuds", "Boat", "Audio",
                                "Bluetooth earbuds with charging case.",
                                "7999.00", 40, "https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?auto=format&fit=crop&w=900&q=80"),
                        product("Dell 27 inch Monitor", "Dell", "Monitors",
                                "27 inch monitor for coding and daily use.",
                                "32999.00", 12, "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=900&q=80"),
                        product("Mechanical Keyboard", "Redragon", "Accessories",
                                "Wired mechanical keyboard with backlight.",
                                "5999.00", 30, "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=900&q=80")
                ));
            }

            // Add one demo customer for easy testing.
            if (!users.existsByEmailIgnoreCase("customer@bytebazaar.dev")) {
                UserAccount user = new UserAccount();
                user.setName("Demo Customer");
                user.setEmail("customer@bytebazaar.dev");
                user.setPasswordHash(passwordEncoder.encode("password"));
                user.setRole(Role.CUSTOMER);
                users.save(user);
            }
        };
    }

    private Product product(String name, String brand, String category, String description, String price, int stock,
            String imageUrl) {
        // Helper method to create product object neatly.
        Product product = new Product();
        product.setName(name);
        product.setBrand(brand);
        product.setCategory(category);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setImageUrl(imageUrl);
        product.setActive(true);
        return product;
    }
}
