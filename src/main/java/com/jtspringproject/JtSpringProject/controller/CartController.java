package com.jtspringproject.JtSpringProject.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jtspringproject.JtSpringProject.models.Product;
import com.jtspringproject.JtSpringProject.services.productService;

@Controller
public class CartController {

    private static final String CART_PRODUCT_IDS = "cartProductIds";
    private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);
    private final productService productService;
    private final com.jtspringproject.JtSpringProject.services.OrderEventPublisher orderEventPublisher;
    private final com.jtspringproject.JtSpringProject.services.ApplicationMetrics applicationMetrics;

    public CartController(productService productService,
            com.jtspringproject.JtSpringProject.services.OrderEventPublisher orderEventPublisher,
            com.jtspringproject.JtSpringProject.services.ApplicationMetrics applicationMetrics) {
        this.productService = productService;
        this.orderEventPublisher = orderEventPublisher;
        this.applicationMetrics = applicationMetrics;
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        model.addAttribute("cartProducts", productsInCart(session));
        return "cartproduct";
    }

    @PostMapping("/cart/add")
    public String add(@RequestParam("productId") int productId, HttpSession session) {
        Product product = productService.getProduct(productId);
        if (product != null) {
            List<Integer> ids = cartIds(session);
            if (!ids.contains(productId)) {
                ids.add(productId);
                publishEvent("PRODUCT_ADDED_TO_CART", productId);
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String remove(@RequestParam("productId") int productId, HttpSession session) {
        cartIds(session).remove(Integer.valueOf(productId));
        publishEvent("PRODUCT_REMOVED_FROM_CART", productId);
        return "redirect:/cart";
    }

    private void publishEvent(String eventType, int productId) {
        String event = "{\"eventType\":\"" + eventType + "\",\"productId\":" + productId + "}";
        try {
            orderEventPublisher.publish(event);
            applicationMetrics.increment(eventType);
        } catch (RuntimeException exception) {
            LOGGER.warn("AWS telemetry unavailable for cart event {}", eventType, exception);
        }
    }

    private List<Product> productsInCart(HttpSession session) {
        List<Product> products = new ArrayList<>();
        for (Integer id : cartIds(session)) {
            Product product = productService.getProduct(id);
            if (product != null) {
                products.add(product);
            }
        }
        return products;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> cartIds(HttpSession session) {
        Object value = session.getAttribute(CART_PRODUCT_IDS);
        if (value instanceof List<?>) {
            return (List<Integer>) value;
        }
        List<Integer> ids = new ArrayList<>();
        session.setAttribute(CART_PRODUCT_IDS, ids);
        return ids;
    }
}