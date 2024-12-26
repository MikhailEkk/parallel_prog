package org.example;

import com.lmax.disruptor.RingBuffer;

import java.util.concurrent.CompletableFuture;

public class OrderService {
    private final RingBuffer<OrderSupplyEvent> ringBuffer;

    public OrderService(RingBuffer<OrderSupplyEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public CompletableFuture<Void> placeOrder(Client client, String productName, int quantity) {
        return CompletableFuture.runAsync(() -> {
            long sequence = ringBuffer.next(); // Получаем следующий индекс
            try {
                OrderSupplyEvent event = ringBuffer.get(sequence);
                OrderRequest orderRequest = new OrderRequest(productName, quantity, client);
                event.setOrderRequest(orderRequest);
                event.setEventType(EventType.ORDER);
                System.out.println("Order placed: " + orderRequest);
            } finally {
                ringBuffer.publish(sequence); // Публикуем событие
            }
        });
    }

    public CompletableFuture<Void> supplyProduct(String productName, int quantity) {
        return CompletableFuture.runAsync(() -> {
            long sequence = ringBuffer.next(); // Получаем следующий индекс
            try {
                OrderSupplyEvent event = ringBuffer.get(sequence);
                event.setProductName(productName);
                event.setQuantity(quantity);
                event.setEventType(EventType.SUPPLY);
                System.out.println("Supply placed: " + productName + ", " + quantity + " units.");
            } finally {
                ringBuffer.publish(sequence); // Публикуем событие
            }
        });
    }
}
