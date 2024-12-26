package example;

import org.example.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HighLoadStoreTest {

    private static Store store;
    private static DisruptorSetup disruptorSetup;
    private static OrderService orderService;
    private static final int NUM_CLIENTS = 100;
    private static final int NUM_THREADS = 50;
    private static final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

    private static double initialTotalBalance;
    private static int initialTotalProducts;

    @BeforeAll
    static void setUp() {
        // Создаем магазин с начальными деньгами
        store = new Store(0);

        // Добавляем товары в магазин
        store.addProduct(new Product("Laptop", 1000, 50));
        store.addProduct(new Product("Phone", 600, 100));
        store.addProduct(new Product("Headphones", 100, 200));

        disruptorSetup = new DisruptorSetup(store);
        orderService = disruptorSetup.getOrderService();

        // Подсчет начального состояния товаров и баланса
        initialTotalBalance = store.getBalance();
        initialTotalProducts = store.getProductInventory().values().stream().mapToInt(Integer::intValue).sum();
    }

    @Test
    @Order(1)
    void highLoadTest() throws InterruptedException, ExecutionException {
        long startTime = System.nanoTime();

        // Создаем клиентов
        List<Client> clients = new ArrayList<>();
        for (int i = 1; i <= NUM_CLIENTS; i++) {
            clients.add(new Client("Client" + i, 5000));
        }

        // Создаем задачи на заказы
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Client client : clients) {
            futures.add(CompletableFuture.runAsync(() -> {
                // Каждый клиент делает несколько заказов
                orderService.placeOrder(client, "Laptop", 1);
                orderService.placeOrder(client, "Phone", 2);
                orderService.placeOrder(client, "Headphones", 5);
            }, executor));
        }

        // Параллельные поставки товаров
        CompletableFuture<Void> supplyFuture = CompletableFuture.runAsync(() -> {
            IntStream.range(0, 20).forEach(i -> {
                store.supplyProducts("Laptop", 10);
                store.supplyProducts("Phone", 20);
                store.supplyProducts("Headphones", 50);
                try {
                    Thread.sleep(200); // Имитируем задержку поставок
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }, executor);

        // Ожидаем завершения всех задач
        CompletableFuture<?>[] futureArray = futures.toArray(new CompletableFuture<?>[0]);
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(supplyFuture, CompletableFuture.allOf(futureArray));
        allFutures.get();  // Ждем завершения всех операций

        long endTime = System.nanoTime();
        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.printf("High load test completed in %.2f seconds%n", durationInSeconds);

        // Подсчет финального состояния товаров и баланса
        double finalTotalBalance = store.getBalance();
        int finalTotalProducts = store.getProductInventory().values().stream().mapToInt(Integer::intValue).sum();

        // Подсчет товаров у клиентов
        int totalClientProducts = clients.stream()
                .flatMap(client -> client.getPurchasedProducts().values().stream())
                .mapToInt(Integer::intValue).sum();

        // Подсчет оставшихся средств у всех клиентов
        double totalClientBalance = clients.stream().mapToDouble(Client::getBalance).sum();

        // Проверка целостности системы
        double initialTotalMoney = initialTotalBalance + clients.size() * 5000;
        double finalTotalMoney = finalTotalBalance + totalClientBalance;

        int totalProductsBefore = initialTotalProducts;
        int totalProductsAfter = finalTotalProducts + totalClientProducts;

        System.out.println("Initial Total Money: " + initialTotalMoney);
        System.out.println("Final Total Money: " + finalTotalMoney);
        System.out.println("Initial Total Products: " + totalProductsBefore);
        System.out.println("Final Total Products: " + totalProductsAfter);

        // Проверка, что сумма денег и товаров осталась неизменной
        assertEquals(initialTotalMoney, finalTotalMoney, 0.001);
        assertEquals(totalProductsBefore + 1600, totalProductsAfter);
    }

    @AfterAll
    static void tearDown() {
        // Завершаем работу Disruptor
        disruptorSetup.shutdown();
        executor.shutdown();
    }
}
