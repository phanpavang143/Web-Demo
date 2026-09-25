package com.jtspringproject.JtSpringProject.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jtspringproject.JtSpringProject.dao.orderDao;
import com.jtspringproject.JtSpringProject.models.Order;
import com.jtspringproject.JtSpringProject.models.Product;
import com.jtspringproject.JtSpringProject.models.User;

@ExtendWith(MockitoExtension.class)
class orderServiceTest {

    @Mock
    private orderDao orderDao;

    @InjectMocks
    private orderService orderService;

    @Test
    void placeOrderSnapshotsProductsAndCalculatesTotal() {
        User customer = new User();
        customer.setUsername("customer");

        Product first = product(7, "Rice", 12);
        Product second = product(8, "Tea", 5);
        when(orderDao.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.placeOrder(customer, List.of(first, second), "12 Main Street", "CASH_ON_DELIVERY");

        assertEquals(customer, order.getCustomer());
        assertEquals("PLACED", order.getStatus());
        assertEquals("CASH_ON_DELIVERY", order.getPaymentMethod());
        assertEquals("12 Main Street", order.getShippingAddress());
        assertEquals(new BigDecimal("17"), order.getTotalAmount());
        assertEquals(2, order.getItems().size());
        assertEquals("Rice", order.getItems().get(0).getProductName());
        assertEquals(new BigDecimal("12"), order.getItems().get(0).getUnitPrice());
        verify(orderDao).save(order);
    }

    private Product product(int id, String name, int price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }
}
