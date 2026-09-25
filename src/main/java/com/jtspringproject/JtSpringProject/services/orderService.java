package com.jtspringproject.JtSpringProject.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jtspringproject.JtSpringProject.dao.orderDao;
import com.jtspringproject.JtSpringProject.models.Order;
import com.jtspringproject.JtSpringProject.models.OrderItem;
import com.jtspringproject.JtSpringProject.models.Product;
import com.jtspringproject.JtSpringProject.models.User;

@Service
public class orderService {
    private final orderDao orderDao;

    public orderService(orderDao orderDao) {
        this.orderDao = orderDao;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        return orderDao.findAll();
    }

    @Transactional
    public Order placeOrder(User customer, List<Product> products, String shippingAddress, String paymentMethod) {
        if (customer == null || products == null || products.isEmpty()) {
            throw new IllegalArgumentException("A customer and at least one product are required.");
        }
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Shipping address is required.");
        }
        if (!"CASH_ON_DELIVERY".equals(paymentMethod)) {
            throw new IllegalArgumentException("Unsupported payment method.");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus("PLACED");
        order.setPaymentMethod(paymentMethod);
        order.setShippingAddress(shippingAddress.trim());
        order.setCreatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        for (Product product : products) {
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(1);
            item.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
            order.addItem(item);
            total = total.add(item.getUnitPrice());
        }
        order.setTotalAmount(total);
        return orderDao.save(order);
    }
}
