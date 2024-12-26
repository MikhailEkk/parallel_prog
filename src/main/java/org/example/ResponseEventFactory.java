package org.example;

import com.lmax.disruptor.EventFactory;

public class ResponseEventFactory implements EventFactory<ResponseEvent> {
    @Override
    public ResponseEvent newInstance() {
        return new ResponseEvent();
    }
}
