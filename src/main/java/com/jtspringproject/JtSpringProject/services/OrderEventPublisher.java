package com.jtspringproject.JtSpringProject.services;

public interface OrderEventPublisher {
	void publish(String event);
}