package org.example;

import com.lmax.disruptor.EventHandler;

public class ResponseEventHandler implements EventHandler<ResponseEvent> {
    @Override
    public void onEvent(ResponseEvent event, long sequence, boolean endOfBatch) {
        switch (event.getEventType()) {
            case ORDER:
                System.out.println("Order response: " + event.getMessage());
                break;
            case SUPPLY:
                System.out.println("Supply response: " + event.getMessage());
                break;
            default:
                System.out.println("Unknown event type: " + event.getMessage());
                break;
        }
    }
}
