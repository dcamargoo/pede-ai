package io.github.dcamargoo.pede_ai;

import io.github.dcamargoo.pede_ai.entity.Category;
import io.github.dcamargoo.pede_ai.entity.MenuItem;
import io.github.dcamargoo.pede_ai.entity.Order;
import io.github.dcamargoo.pede_ai.entity.OrderItem;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.Role;
import io.github.dcamargoo.pede_ai.repository.CategoryRepository;
import io.github.dcamargoo.pede_ai.repository.MenuItemRepository;
import io.github.dcamargoo.pede_ai.repository.OrderRepository;
import io.github.dcamargoo.pede_ai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    static {
        postgres.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected MenuItemRepository menuItemRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE tb_order_item, tb_order, tb_menu_item, tb_category, tb_user
                RESTART IDENTITY CASCADE
                """);
    }

    protected User createCustomer(String email) {
        User user = new User();
        user.setName("Marina");
        user.setEmail(email);
        user.setPassword("senha123");
        user.setRole(Role.CUSTOMER);
        return userRepository.save(user);
    }

    protected Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    protected MenuItem createMenuItem(String name, String price, boolean available, Category category) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setPrice(new BigDecimal(price));
        menuItem.setAvailable(available);
        menuItem.setCategory(category);
        return menuItemRepository.save(menuItem);
    }

    protected Order createOrder(User customer, MenuItem menuItem, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItem(menuItem);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(menuItem.getPrice());
        orderItem.setSubtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)));

        Order order = new Order();
        order.setCustomer(customer);
        order.addItem(orderItem);
        order.setTotal(orderItem.getSubtotal());
        return orderRepository.save(order);
    }
}
