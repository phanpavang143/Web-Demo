package com.jtspringproject.JtSpringProject.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jtspringproject.JtSpringProject.models.Order;

@Repository
public class orderDao {
    private final SessionFactory sessionFactory;

    public orderDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public Order save(Order order) {
        sessionFactory.getCurrentSession().save(order);
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from CUSTOMER_ORDER order by createdAt desc", Order.class)
                .list();
    }
}
