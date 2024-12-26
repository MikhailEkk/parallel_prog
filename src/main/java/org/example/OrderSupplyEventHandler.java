package org.example;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;

public class OrderSupplyEventHandler implements EventHandler<OrderSupplyEvent> {
    private final Store store;
    private final RingBuffer<ResponseEvent> responseRingBuffer;

    public OrderSupplyEventHandler(Store store, RingBuffer<ResponseEvent> responseRingBuffer) {
        this.store = store;
        this.responseRingBuffer = responseRingBuffer;
    }

    @Override
    public void onEvent(OrderSupplyEvent event, long sequence, boolean endOfBatch) {
        if (event.getEventType() == EventType.ORDER) {
            OrderRequest orderRequest = event.getOrderRequest();
            //System.out.println("Processing order: " + orderRequest);

            String result = store.processOrder(orderRequest);

            // Записываем сообщение о заказе в выходной Disruptor
            long responseSequence = responseRingBuffer.next();
            try {
                ResponseEvent responseEvent = responseRingBuffer.get(responseSequence);
                responseEvent.setEventType(EventType.ORDER);
                responseEvent.setMessage(result);
            } finally {
                responseRingBuffer.publish(responseSequence);
            }
        } else if (event.getEventType() == EventType.SUPPLY) {
            String productName = event.getProductName();
            int quantity = event.getQuantity();
            String result  = store.supplyProducts(productName, quantity);
            //System.out.println("Supply received: " + productName + ", " + quantity + " units.");

            // Записываем сообщение о поставке в выходной Disruptor
            long responseSequence = responseRingBuffer.next();
            try {
                ResponseEvent responseEvent = responseRingBuffer.get(responseSequence);
                responseEvent.setEventType(EventType.SUPPLY);
                responseEvent.setMessage(result);
                //responseEvent.setMessage("Supply of " + quantity + " units of " + productName + " processed.");
            } finally {
                responseRingBuffer.publish(responseSequence);
            }
        }
    }
}
