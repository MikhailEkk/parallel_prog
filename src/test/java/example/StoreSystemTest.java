package example;

import org.example.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreSystemTest {
    private static Store store;
    private static DisruptorSetup disruptorSetup;
    private static OrderService orderService;
    private static Client client1;
    private static Client client2;
    private static Client client3;

    @BeforeAll
    static void setUp() {
        // Создаем магазин с начальными деньгами
        store = new Store(0);

        // Добавляем товары в магазин
        store.addProduct(new Product("Laptop", 1200, 10));
        store.addProduct(new Product("Phone", 800, 20));
        store.addProduct(new Product("Headphones", 150, 50));

        // Создаем клиентов
        client1 = new Client("Alice", 5000);
        client2 = new Client("Bob", 3000);
        client3 = new Client("Max", 20000);

        // Инициализируем Disruptor и OrderService
        disruptorSetup = new DisruptorSetup(store);
        orderService = disruptorSetup.getOrderService();
    }

    @Test
    @Order(1)
    void testInitialStoreState() {
        // Проверка начального состояния товаров в магазине
        Map<String, Integer> inventory = store.getProductInventory();
        assertEquals(10, inventory.get("Laptop"));
        assertEquals(20, inventory.get("Phone"));
        assertEquals(50, inventory.get("Headphones"));
        assertEquals(0, store.getBalance());

        // Проверка начального состояния клиентов
        assertEquals(5000, client1.getBalance());
        assertEquals(3000, client2.getBalance());
        assertEquals(20000, client3.getBalance());
    }

    @Test
    @Order(2)
    void testPlacingOrders() throws InterruptedException {
        // Размещаем заказы асинхронно
        orderService.placeOrder(client1, "Laptop", 2);
        orderService.placeOrder(client2, "Phone", 2);
        orderService.placeOrder(client3, "Headphones", 10);

        // Даем немного времени на выполнение заказов
        Thread.sleep(2000);

        // Проверка баланса и товаров после выполнения заказов
        assertEquals(2600, client1.getBalance()); // 5000 - (2 * 1200)
        assertEquals(1400, client2.getBalance()); // 3000 - (2 * 800)
        assertEquals(18500, client3.getBalance()); // 20000 - (10 * 150)

        Map<String, Integer> inventory = store.getProductInventory();
        assertEquals(8, inventory.get("Laptop"));
        assertEquals(18, inventory.get("Phone"));
        assertEquals(40, inventory.get("Headphones"));

        // Проверка баланса магазина
        assertEquals(5500, store.getBalance()); // (2 * 1200) + (2 * 800) + (10 * 150)
    }

    @Test
    @Order(3)
    void testPendingOrders() throws InterruptedException {
        // Размещаем заказ на количество товаров больше, чем есть в наличии
        orderService.placeOrder(client3, "Phone", 22); // В наличии только 20
        Thread.sleep(2000);

        // Проверка состояния клиента и заблокированных средств
        assertEquals(900, client3.getBalance());
        assertEquals(3200, client3.getBlockedAmount()); // 800 * 2 (2 товара в бронь)

        List<OrderRequest> pendingOrders = store.getPendingOrders();
        assertEquals(1, pendingOrders.size());
    }

    @Test
    @Order(4)
    void testSupplyProducts() throws InterruptedException {
        // Поставка новых товаров (закрывает незавершенные заказы)
        store.supplyProducts("Phone", 10);
        Thread.sleep(2000);

        // Проверка выполнения отложенных заказов
        List<OrderRequest> pendingOrders = store.getPendingOrders();
        assertTrue(pendingOrders.isEmpty());

        // Проверка баланса клиента после списания замороженных средств
        assertEquals(900, client3.getBalance());
        assertEquals(23100, store.getBalance()); // Баланс магазина с учетом выполненных заказов
    }

    @AfterAll
    static void tearDown() {
        // Завершаем работу Disruptor
        disruptorSetup.shutdown();
    }
}
