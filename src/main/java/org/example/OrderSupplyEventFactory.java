package org.example;

import com.lmax.disruptor.EventFactory;

public class OrderSupplyEventFactory implements EventFactory<OrderSupplyEvent> {
    @Override
    public OrderSupplyEvent newInstance() {
        return new OrderSupplyEvent();
    }
}
