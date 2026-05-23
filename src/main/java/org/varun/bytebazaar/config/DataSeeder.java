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
    @Bean
    CommandLineRunner seedData(ProductRepository products, UserRepository users, PasswordEncoder passwordEncoder) {
        return args -> {
            if (products.count() == 0) {
                products.saveAll(List.of(
                        product("PixelForge Pro 15", "ByteWorks", "Laptops",
                                "Creator laptop with a 15-inch OLED display, Ryzen 9 class performance, 32GB RAM, and 1TB SSD.",
                                "129999.00", 8, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=900&q=80"),
                        product("AeroBook Air 13", "Nimbus", "Laptops",
                                "Ultra-light notebook with all-day battery life, backlit keyboard, and fast USB-C charging.",
                                "84999.00", 14, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80"),
                        product("Nova X1 5G", "Orion", "Mobiles",
                                "Flagship 5G phone with a 120Hz AMOLED screen, triple camera system, and 256GB storage.",
                                "64999.00", 25, "https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=900&q=80"),
                        product("PulseBuds ANC", "SoundArc", "Audio",
                                "Wireless earbuds with adaptive noise cancellation, low-latency gaming mode, and 32-hour case battery.",
                                "7999.00", 40, "https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?auto=format&fit=crop&w=900&q=80"),
                        product("DeskView 27 4K", "ViewNest", "Monitors",
                                "Color-accurate 27-inch 4K IPS monitor with USB-C display input and height adjustable stand.",
                                "32999.00", 12, "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=900&q=80"),
                        product("KeySwift Mechanical", "Tactile Labs", "Accessories",
                                "Hot-swappable mechanical keyboard with compact layout, PBT caps, and white backlighting.",
                                "5999.00", 30, "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=900&q=80")
                ));
            }

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
