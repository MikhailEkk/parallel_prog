package org.example;

public class Main {
    public static void main(String[] args) {
        Store store = new Store(5000.0);
        store.addProduct(new Product("Laptop", 1200.0, 5));
        store.addProduct(new Product("Phone", 800.0, 20));
        store.addProduct(new Product("Headphones", 150.0, 50));

        DisruptorSetup disruptorSetup = new DisruptorSetup(store);
        OrderService orderService = disruptorSetup.getOrderService();

        // Клиенты размещают заказы
        Client client1 = new Client("Alice", 300000.0);
        Client client2 = new Client("Bob", 5000.0);

        orderService.placeOrder(client1, "Laptop", 7);
        orderService.placeOrder(client2, "Phone", 2);

        // Поставка товаров
        orderService.supplyProduct("Laptop", 20);

        // Ожидание, чтобы обработка успела завершиться
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Завершение работы Disruptor
        disruptorSetup.shutdown();
    }
}
