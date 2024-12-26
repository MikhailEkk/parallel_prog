package org.example;

import com.lmax.disruptor.dsl.Disruptor;

import java.util.concurrent.Executors;

public class DisruptorSetup {
    private final Disruptor<OrderSupplyEvent> inputDisruptor;
    private final Disruptor<ResponseEvent> outputDisruptor;
    private final OrderService orderService;

    public DisruptorSetup(Store store) {
        OrderSupplyEventFactory inputEventFactory = new OrderSupplyEventFactory();
        int inputBufferSize = 1024;

        // Настройка Disruptor для обработки заказов и поставок
        inputDisruptor = new Disruptor<>(inputEventFactory, inputBufferSize, Executors.defaultThreadFactory());

        ResponseEventFactory outputEventFactory = new ResponseEventFactory();
        int outputBufferSize = 1024;

        // Настройка Disruptor для выходной очереди
        outputDisruptor = new Disruptor<>(outputEventFactory, outputBufferSize, Executors.defaultThreadFactory());

        OrderSupplyEventHandler eventHandler = new OrderSupplyEventHandler(store, outputDisruptor.getRingBuffer());
        inputDisruptor.handleEventsWith(eventHandler);

        ResponseEventHandler responseHandler = new ResponseEventHandler();
        outputDisruptor.handleEventsWith(responseHandler);

        inputDisruptor.start();
        outputDisruptor.start();

        orderService = new OrderService(inputDisruptor.getRingBuffer());
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void shutdown() {
        inputDisruptor.shutdown();
        outputDisruptor.shutdown();
    }
}
