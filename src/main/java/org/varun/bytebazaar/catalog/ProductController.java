package org.varun.bytebazaar.catalog;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping
    List<Product> list() {
        return products.findByActiveTrueOrderByCategoryAscNameAsc();
    }

    @GetMapping("/{id}")
    Product get(@PathVariable Long id) {
        return products.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }
}
