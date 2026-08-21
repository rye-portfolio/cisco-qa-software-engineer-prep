# WebApp Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the `WebApp` Spring Boot sample application (users/stock/orders with permission-gated pages, SQLite storage, seed data) and the three-tier GitHub Actions CI/CD pipeline described in the spec.

**Architecture:** A single-module Maven Spring Boot 3 app under `WebApp/`, layered as `model` (JPA entities) → `repository` (Spring Data) → `service` (business logic, incl. the atomic stock-decrement transaction) → `web` (Thymeleaf MVC controllers), with Spring Security handling session-based form login and permission-flag-derived authorities. Two GitHub Actions workflows scaffold the smoke/system test gates around a Maven build that publishes the runnable jar as a workflow artifact.

**Tech Stack:** Java 21, Spring Boot 3.3.4, Maven, Spring Data JPA + Hibernate (`hibernate-community-dialects` for SQLite), `org.xerial:sqlite-jdbc`, Spring Security (BCrypt, form login), Thymeleaf (+ `thymeleaf-extras-springsecurity6`), JUnit 5 + AssertJ + Mockito + `spring-security-test`.

**Spec:** `docs/superpowers/specs/2026-08-21-webapp-design.md`

## Global Constraints

- Java version: 21. Spring Boot version: 3.3.4 (pin `hibernate-community-dialects` to the same Hibernate line Boot 3.3.4 resolves — verify via `mvn dependency:tree | grep hibernate-core` if the build fails to resolve it).
- Build tool: Maven only, single module at `WebApp/` (repo root is one level up).
- Database: SQLite file `webapp.db` in the working directory at runtime (gitignored). `spring.datasource.hikari.maximum-pool-size=1` in both main and test config, to avoid SQLite's single-writer limitation causing spurious "database is locked" errors.
- Password hashing: BCrypt via Spring Security's `BCryptPasswordEncoder`. All seeded demo users get password `password`.
- Permission flags (`manageUsers`, `manageStock`, `viewAllOrders`) live directly on `User` as booleans and are exposed as Spring Security authorities `MANAGE_USERS`, `MANAGE_STOCK`, `VIEW_ALL_ORDERS` — no separate role entities.
- No Selenium/system-test code is written in this project — only unit/integration tests (JUnit/MockMvc) for TDD, and CI placeholders for the future Selenium framework.
- Every test class that boots a full/partial Spring context must use `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)` so it uses the real SQLite driver against `src/test/resources/application.properties`, not an auto-substituted embedded DB.
- Tests sharing a `@SpringBootTest` context must not assert on absolute row counts (seed data or other tests' rows may already be present in that shared context) — assert on specific created entities/ids instead.

---

## File Structure

```
WebApp/
  pom.xml
  src/main/java/com/example/webapp/
    WebApp.java
    model/
      User.java
      StockItem.java
      Order.java
      OrderLine.java
      OrderStatus.java
    repository/
      UserRepository.java
      StockItemRepository.java
      OrderRepository.java
    security/
      SecurityConfig.java
      AppUserDetailsService.java
    service/
      OrderService.java
      OrderRequestLine.java
      InsufficientStockException.java
      StockService.java
      UserService.java
    web/
      LoginController.java
      OrdersController.java
      StockController.java
      UsersController.java
    config/
      SeedDataRunner.java
  src/main/resources/
    application.properties
    templates/
      login.html
      orders.html
      stock.html
      users.html
  src/test/resources/
    application.properties
  src/test/java/com/example/webapp/
    WebAppTests.java
    repository/RepositoryPersistenceTest.java
    security/AppUserDetailsServiceTest.java
    service/OrderServiceTest.java
    service/StockServiceTest.java
    service/UserServiceTest.java
    web/PermissionGatingTest.java
    config/SeedDataRunnerTest.java
  README.md
.github/workflows/
  ci.yml
  release.yml
.gitignore   (modified, repo root)
```

---

### Task 1: Project scaffold, config, and context-load test

**Files:**
- Create: `WebApp/pom.xml`
- Create: `WebApp/src/main/java/com/example/webapp/WebApp.java`
- Create: `WebApp/src/main/resources/application.properties`
- Create: `WebApp/src/test/resources/application.properties`
- Create: `WebApp/src/test/java/com/example/webapp/WebAppTests.java`
- Modify: `.gitignore` (repo root)

**Interfaces:**
- Produces: `com.example.webapp.WebApp` (the `@SpringBootApplication` main class) — every later component is scanned under `com.example.webapp`.

- [ ] **Step 1: Write `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>webapp</artifactId>
    <version>0.1.0</version>
    <name>webapp</name>
    <description>Sample QA-automation System Under Test</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.thymeleaf.extras</groupId>
            <artifactId>thymeleaf-extras-springsecurity6</artifactId>
        </dependency>
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.46.1.3</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-community-dialects</artifactId>
            <version>6.5.2.Final</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

If `mvn` (Step 5 below) fails to resolve `hibernate-community-dialects:6.5.2.Final`, run
`mvn dependency:tree | grep hibernate-core` to see the Hibernate version Boot 3.3.4 actually
resolved, and change the `hibernate-community-dialects` version to match it exactly.

- [ ] **Step 2: Write the main application class**

`WebApp/src/main/java/com/example/webapp/WebApp.java`:

```java
package com.example.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebApp {

    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }
}
```

- [ ] **Step 3: Write main and test `application.properties`**

`WebApp/src/main/resources/application.properties`:

```properties
spring.application.name=webapp
spring.datasource.url=jdbc:sqlite:webapp.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.hikari.maximum-pool-size=1
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.thymeleaf.cache=false
server.port=8080
```

`WebApp/src/test/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlite::memory:
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.hikari.maximum-pool-size=1
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.open-in-view=false
```

- [ ] **Step 4: Write the failing context-load test**

`WebApp/src/test/java/com/example/webapp/WebAppTests.java`:

```java
package com.example.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WebAppTests {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 5: Run the test to verify it fails**

Run (from `WebApp/`): `mvn -q test`
Expected: FAIL — compilation error or missing dependency, since the app has just been scaffolded and this is the first build.

- [ ] **Step 6: Fix until the build compiles and the test passes**

Run: `mvn -q test`
Expected: PASS — `WebAppTests.contextLoads` succeeds (Spring context boots against the in-memory SQLite test datasource).

- [ ] **Step 7: Add the SQLite runtime file to `.gitignore`**

Append to the repo-root `.gitignore`:

```
# SQLite runtime database
*.db
*.db-journal
```

- [ ] **Step 8: Commit**

```bash
git add WebApp/pom.xml WebApp/src/main/java/com/example/webapp/WebApp.java \
        WebApp/src/main/resources/application.properties \
        WebApp/src/test/resources/application.properties \
        WebApp/src/test/java/com/example/webapp/WebAppTests.java .gitignore
git commit -m "Scaffold WebApp Spring Boot project"
```

---

### Task 2: Domain entities and repositories

**Files:**
- Create: `WebApp/src/main/java/com/example/webapp/model/OrderStatus.java`
- Create: `WebApp/src/main/java/com/example/webapp/model/User.java`
- Create: `WebApp/src/main/java/com/example/webapp/model/StockItem.java`
- Create: `WebApp/src/main/java/com/example/webapp/model/Order.java`
- Create: `WebApp/src/main/java/com/example/webapp/model/OrderLine.java`
- Create: `WebApp/src/main/java/com/example/webapp/repository/UserRepository.java`
- Create: `WebApp/src/main/java/com/example/webapp/repository/StockItemRepository.java`
- Create: `WebApp/src/main/java/com/example/webapp/repository/OrderRepository.java`
- Test: `WebApp/src/test/java/com/example/webapp/repository/RepositoryPersistenceTest.java`

**Interfaces:**
- Consumes: nothing beyond Task 1's project scaffold.
- Produces: `User(String username, String passwordHash, boolean manageUsers, boolean manageStock, boolean viewAllOrders)` with getters `getId/getUsername/getPasswordHash/isManageUsers/isManageStock/isViewAllOrders` and setters `setPasswordHash/setManageUsers/setManageStock/setViewAllOrders`; `StockItem(String name, int quantity)` with `getId/getName/setName/getQuantity/setQuantity`; `Order(User creator, Instant createdAt, OrderStatus status)` with `getId/getCreator/getCreatedAt/getStatus/getLines/addLine(OrderLine)`; `OrderLine(StockItem stockItem, int quantity)` with `getId/getOrder/getStockItem/getQuantity`; `UserRepository.findByUsername(String): Optional<User>`; `OrderRepository.findByCreator(User): List<Order>`; `StockItemRepository` (plain `JpaRepository<StockItem, Long>`).

- [ ] **Step 1: Write the failing persistence test**

`WebApp/src/test/java/com/example/webapp/repository/RepositoryPersistenceTest.java`:

```java
package com.example.webapp.repository;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderLine;
import com.example.webapp.model.OrderStatus;
import com.example.webapp.model.StockItem;
import com.example.webapp.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void savesAndFindsUserByUsername() {
        userRepository.save(new User("alice", "hash", true, false, false));

        Optional<User> found = userRepository.findByUsername("alice");

        assertThat(found).isPresent();
        assertThat(found.get().isManageUsers()).isTrue();
        assertThat(found.get().isManageStock()).isFalse();
    }

    @Test
    void savesOrderWithLinesAndCascadesPersistence() {
        User creator = userRepository.save(new User("bob", "hash", false, false, false));
        StockItem widget = stockItemRepository.save(new StockItem("Widget", 10));

        Order order = new Order(creator, Instant.now(), OrderStatus.COMPLETED);
        order.addLine(new OrderLine(widget, 3));
        Order saved = orderRepository.save(order);

        List<Order> found = orderRepository.findByCreator(creator);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getLines()).hasSize(1);
        assertThat(found.get(0).getLines().get(0).getQuantity()).isEqualTo(3);
        assertThat(saved.getId()).isNotNull();
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q test -Dtest=RepositoryPersistenceTest`
Expected: FAIL with compilation errors (the model/repository classes don't exist yet).

- [ ] **Step 3: Write the enum and entities**

`WebApp/src/main/java/com/example/webapp/model/OrderStatus.java`:

```java
package com.example.webapp.model;

public enum OrderStatus {
    COMPLETED
}
```

`WebApp/src/main/java/com/example/webapp/model/User.java`:

```java
package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean manageUsers;

    @Column(nullable = false)
    private boolean manageStock;

    @Column(nullable = false)
    private boolean viewAllOrders;

    protected User() {
    }

    public User(String username, String passwordHash, boolean manageUsers, boolean manageStock, boolean viewAllOrders) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.manageUsers = manageUsers;
        this.manageStock = manageStock;
        this.viewAllOrders = viewAllOrders;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isManageUsers() { return manageUsers; }
    public void setManageUsers(boolean manageUsers) { this.manageUsers = manageUsers; }
    public boolean isManageStock() { return manageStock; }
    public void setManageStock(boolean manageStock) { this.manageStock = manageStock; }
    public boolean isViewAllOrders() { return viewAllOrders; }
    public void setViewAllOrders(boolean viewAllOrders) { this.viewAllOrders = viewAllOrders; }
}
```

`WebApp/src/main/java/com/example/webapp/model/StockItem.java`:

```java
package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int quantity;

    protected StockItem() {
    }

    public StockItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
```

`WebApp/src/main/java/com/example/webapp/model/Order.java`:

```java
package com.example.webapp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    protected Order() {
    }

    public Order(User creator, Instant createdAt, OrderStatus status) {
        this.creator = creator;
        this.createdAt = createdAt;
        this.status = status;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
        line.setOrder(this);
    }

    public Long getId() { return id; }
    public User getCreator() { return creator; }
    public Instant getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
    public List<OrderLine> getLines() { return lines; }
}
```

`WebApp/src/main/java/com/example/webapp/model/OrderLine.java`:

```java
package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @Column(nullable = false)
    private int quantity;

    protected OrderLine() {
    }

    public OrderLine(StockItem stockItem, int quantity) {
        this.stockItem = stockItem;
        this.quantity = quantity;
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public StockItem getStockItem() { return stockItem; }
    public int getQuantity() { return quantity; }
}
```

- [ ] **Step 4: Write the repositories**

`WebApp/src/main/java/com/example/webapp/repository/UserRepository.java`:

```java
package com.example.webapp.repository;

import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

`WebApp/src/main/java/com/example/webapp/repository/StockItemRepository.java`:

```java
package com.example.webapp.repository;

import com.example.webapp.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
}
```

`WebApp/src/main/java/com/example/webapp/repository/OrderRepository.java`:

```java
package com.example.webapp.repository;

import com.example.webapp.model.Order;
import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCreator(User creator);
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `mvn -q test -Dtest=RepositoryPersistenceTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add WebApp/src/main/java/com/example/webapp/model WebApp/src/main/java/com/example/webapp/repository \
        WebApp/src/test/java/com/example/webapp/repository
git commit -m "Add domain entities and repositories"
```

---

### Task 3: Security config and user-details mapping

**Files:**
- Create: `WebApp/src/main/java/com/example/webapp/security/AppUserDetailsService.java`
- Create: `WebApp/src/main/java/com/example/webapp/security/SecurityConfig.java`
- Test: `WebApp/src/test/java/com/example/webapp/security/AppUserDetailsServiceTest.java`

**Interfaces:**
- Consumes: `UserRepository.findByUsername` (Task 2).
- Produces: `SecurityConfig` beans `PasswordEncoder passwordEncoder()` and `SecurityFilterChain filterChain(HttpSecurity)`; `AppUserDetailsService implements UserDetailsService`, mapping `User` booleans to authorities `"MANAGE_USERS"`, `"MANAGE_STOCK"`, `"VIEW_ALL_ORDERS"` (present only when the corresponding flag is `true`).

- [ ] **Step 1: Write the failing test**

`WebApp/src/test/java/com/example/webapp/security/AppUserDetailsServiceTest.java`:

```java
package com.example.webapp.security;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AppUserDetailsService service = new AppUserDetailsService(userRepository);

    @Test
    void mapsAllPermissionFlagsToAuthorities() {
        User admin = new User("admin", "hash", true, true, true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("MANAGE_USERS", "MANAGE_STOCK", "VIEW_ALL_ORDERS");
    }

    @Test
    void mapsNoAuthoritiesWhenNoPermissionsGranted() {
        User plain = new User("user", "hash", false, false, false);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(plain));

        UserDetails details = service.loadUserByUsername("user");

        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void throwsWhenUsernameNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q test -Dtest=AppUserDetailsServiceTest`
Expected: FAIL (compilation error — `AppUserDetailsService` doesn't exist yet).

- [ ] **Step 3: Write `AppUserDetailsService`**

`WebApp/src/main/java/com/example/webapp/security/AppUserDetailsService.java`:

```java
package com.example.webapp.security;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such user: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.isManageUsers()) {
            authorities.add(new SimpleGrantedAuthority("MANAGE_USERS"));
        }
        if (user.isManageStock()) {
            authorities.add(new SimpleGrantedAuthority("MANAGE_STOCK"));
        }
        if (user.isViewAllOrders()) {
            authorities.add(new SimpleGrantedAuthority("VIEW_ALL_ORDERS"));
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}
```

- [ ] **Step 4: Write `SecurityConfig`**

`WebApp/src/main/java/com/example/webapp/security/SecurityConfig.java`:

```java
package com.example.webapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/orders", true)
                .permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"));

        return http.build();
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `mvn -q test -Dtest=AppUserDetailsServiceTest`
Expected: PASS

- [ ] **Step 6: Run the full test suite to confirm nothing else broke**

Run: `mvn -q test`
Expected: PASS (`WebAppTests`, `RepositoryPersistenceTest`, `AppUserDetailsServiceTest` all green — the app now requires authentication for most routes, but no controllers exist yet so this doesn't affect existing tests).

- [ ] **Step 7: Commit**

```bash
git add WebApp/src/main/java/com/example/webapp/security WebApp/src/test/java/com/example/webapp/security
git commit -m "Add Spring Security config and permission-flag-based UserDetailsService"
```

---

### Task 4: OrderService with atomic stock-decrement

**Files:**
- Create: `WebApp/src/main/java/com/example/webapp/service/InsufficientStockException.java`
- Create: `WebApp/src/main/java/com/example/webapp/service/OrderRequestLine.java`
- Create: `WebApp/src/main/java/com/example/webapp/service/OrderService.java`
- Test: `WebApp/src/test/java/com/example/webapp/service/OrderServiceTest.java`

**Interfaces:**
- Consumes: `OrderRepository`, `StockItemRepository` (Task 2).
- Produces: `OrderRequestLine(Long stockItemId, int quantity)` (record); `InsufficientStockException extends RuntimeException`; `OrderService.createOrder(User creator, List<OrderRequestLine> lines): Order` (throws `InsufficientStockException`, `@Transactional`, validates all lines before decrementing any); `OrderService.findOrdersByCreator(User): List<Order>`; `OrderService.findAllOrders(): List<Order>`.

- [ ] **Step 1: Write the failing test**

`WebApp/src/test/java/com/example/webapp/service/OrderServiceTest.java`:

```java
package com.example.webapp.service;

import com.example.webapp.model.StockItem;
import com.example.webapp.model.User;
import com.example.webapp.repository.StockItemRepository;
import com.example.webapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    private User creator;
    private StockItem widget;
    private StockItem gadget;

    @BeforeEach
    void setUp() {
        creator = userRepository.save(new User("creator", "hash", false, false, false));
        widget = stockItemRepository.save(new StockItem("Widget", 5));
        gadget = stockItemRepository.save(new StockItem("Gadget", 2));
    }

    @Test
    void decrementsStockOnSuccessfulOrder() {
        orderService.createOrder(creator, List.of(new OrderRequestLine(widget.getId(), 3)));

        StockItem updated = stockItemRepository.findById(widget.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(2);
    }

    @Test
    void failsWithoutPartialDecrementWhenAnyLineHasInsufficientStock() {
        assertThatThrownBy(() -> orderService.createOrder(creator, List.of(
                new OrderRequestLine(widget.getId(), 3),
                new OrderRequestLine(gadget.getId(), 10)
        ))).isInstanceOf(InsufficientStockException.class);

        StockItem widgetAfter = stockItemRepository.findById(widget.getId()).orElseThrow();
        StockItem gadgetAfter = stockItemRepository.findById(gadget.getId()).orElseThrow();
        assertThat(widgetAfter.getQuantity()).isEqualTo(5);
        assertThat(gadgetAfter.getQuantity()).isEqualTo(2);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q test -Dtest=OrderServiceTest`
Expected: FAIL (compilation error — `OrderService`/`OrderRequestLine`/`InsufficientStockException` don't exist yet).

- [ ] **Step 3: Write the exception, request-line record, and `OrderService`**

`WebApp/src/main/java/com/example/webapp/service/InsufficientStockException.java`:

```java
package com.example.webapp.service;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```

`WebApp/src/main/java/com/example/webapp/service/OrderRequestLine.java`:

```java
package com.example.webapp.service;

public record OrderRequestLine(Long stockItemId, int quantity) {
}
```

`WebApp/src/main/java/com/example/webapp/service/OrderService.java`:

```java
package com.example.webapp.service;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderLine;
import com.example.webapp.model.OrderStatus;
import com.example.webapp.model.StockItem;
import com.example.webapp.model.User;
import com.example.webapp.repository.OrderRepository;
import com.example.webapp.repository.StockItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockItemRepository stockItemRepository;

    public OrderService(OrderRepository orderRepository, StockItemRepository stockItemRepository) {
        this.orderRepository = orderRepository;
        this.stockItemRepository = stockItemRepository;
    }

    @Transactional
    public Order createOrder(User creator, List<OrderRequestLine> requestLines) {
        for (OrderRequestLine requestLine : requestLines) {
            StockItem item = stockItemRepository.findById(requestLine.stockItemId())
                    .orElseThrow(() -> new InsufficientStockException(
                            "Unknown stock item: " + requestLine.stockItemId()));
            if (item.getQuantity() < requestLine.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for " + item.getName()
                                + ": requested " + requestLine.quantity()
                                + ", available " + item.getQuantity());
            }
        }

        Order order = new Order(creator, Instant.now(), OrderStatus.COMPLETED);
        for (OrderRequestLine requestLine : requestLines) {
            StockItem item = stockItemRepository.findById(requestLine.stockItemId()).orElseThrow();
            item.setQuantity(item.getQuantity() - requestLine.quantity());
            order.addLine(new OrderLine(item, requestLine.quantity()));
        }

        return orderRepository.save(order);
    }

    public List<Order> findOrdersByCreator(User creator) {
        return orderRepository.findByCreator(creator);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn -q test -Dtest=OrderServiceTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add WebApp/src/main/java/com/example/webapp/service/InsufficientStockException.java \
        WebApp/src/main/java/com/example/webapp/service/OrderRequestLine.java \
        WebApp/src/main/java/com/example/webapp/service/OrderService.java \
        WebApp/src/test/java/com/example/webapp/service/OrderServiceTest.java
git commit -m "Add OrderService with atomic stock-decrement transaction"
```

---

### Task 5: StockService and UserService

**Files:**
- Create: `WebApp/src/main/java/com/example/webapp/service/StockService.java`
- Create: `WebApp/src/main/java/com/example/webapp/service/UserService.java`
- Test: `WebApp/src/test/java/com/example/webapp/service/StockServiceTest.java`
- Test: `WebApp/src/test/java/com/example/webapp/service/UserServiceTest.java`

**Interfaces:**
- Consumes: `StockItemRepository`, `UserRepository` (Task 2), `PasswordEncoder` (Task 3).
- Produces: `StockService.findAll(): List<StockItem>`, `StockService.createItem(String name, int quantity): StockItem`, `StockService.updateQuantity(Long id, int quantity): StockItem` (throws `IllegalArgumentException` if unknown id); `UserService.findAll(): List<User>`, `UserService.createUser(String username, String rawPassword, boolean manageUsers, boolean manageStock, boolean viewAllOrders): User` (stores a BCrypt hash, never the raw password), `UserService.updatePermissions(Long id, boolean manageUsers, boolean manageStock, boolean viewAllOrders): User`.

- [ ] **Step 1: Write the failing tests**

`WebApp/src/test/java/com/example/webapp/service/StockServiceTest.java`:

```java
package com.example.webapp.service;

import com.example.webapp.model.StockItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Test
    void createItemPersistsNewStockItem() {
        StockItem created = stockService.createItem("Thingamajig", 7);

        assertThat(created.getId()).isNotNull();
        assertThat(stockService.findAll()).extracting(StockItem::getName).contains("Thingamajig");
    }

    @Test
    void updateQuantityChangesExistingItem() {
        StockItem created = stockService.createItem("Doohickey", 3);

        StockItem updated = stockService.updateQuantity(created.getId(), 9);

        assertThat(updated.getQuantity()).isEqualTo(9);
    }
}
```

`WebApp/src/test/java/com/example/webapp/service/UserServiceTest.java`:

```java
package com.example.webapp.service;

import com.example.webapp.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createUserHashesPassword() {
        User created = userService.createUser("newperson", "s3cret", false, true, false);

        assertThat(created.getPasswordHash()).isNotEqualTo("s3cret");
        assertThat(passwordEncoder.matches("s3cret", created.getPasswordHash())).isTrue();
        assertThat(created.isManageStock()).isTrue();
        assertThat(created.isManageUsers()).isFalse();
    }

    @Test
    void updatePermissionsChangesFlags() {
        User created = userService.createUser("anotherperson", "pw", false, false, false);

        User updated = userService.updatePermissions(created.getId(), true, true, true);

        assertThat(updated.isManageUsers()).isTrue();
        assertThat(updated.isManageStock()).isTrue();
        assertThat(updated.isViewAllOrders()).isTrue();
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn -q test -Dtest=StockServiceTest,UserServiceTest`
Expected: FAIL (compilation error — `StockService`/`UserService` don't exist yet).

- [ ] **Step 3: Write `StockService` and `UserService`**

`WebApp/src/main/java/com/example/webapp/service/StockService.java`:

```java
package com.example.webapp.service;

import com.example.webapp.model.StockItem;
import com.example.webapp.repository.StockItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockItemRepository stockItemRepository;

    public StockService(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    public List<StockItem> findAll() {
        return stockItemRepository.findAll();
    }

    public StockItem createItem(String name, int quantity) {
        return stockItemRepository.save(new StockItem(name, quantity));
    }

    public StockItem updateQuantity(Long id, int quantity) {
        StockItem item = stockItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown stock item: " + id));
        item.setQuantity(quantity);
        return stockItemRepository.save(item);
    }
}
```

`WebApp/src/main/java/com/example/webapp/service/UserService.java`:

```java
package com.example.webapp.service;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User createUser(String username, String rawPassword, boolean manageUsers, boolean manageStock, boolean viewAllOrders) {
        User user = new User(username, passwordEncoder.encode(rawPassword), manageUsers, manageStock, viewAllOrders);
        return userRepository.save(user);
    }

    public User updatePermissions(Long id, boolean manageUsers, boolean manageStock, boolean viewAllOrders) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + id));
        user.setManageUsers(manageUsers);
        user.setManageStock(manageStock);
        user.setViewAllOrders(viewAllOrders);
        return userRepository.save(user);
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn -q test -Dtest=StockServiceTest,UserServiceTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add WebApp/src/main/java/com/example/webapp/service/StockService.java \
        WebApp/src/main/java/com/example/webapp/service/UserService.java \
        WebApp/src/test/java/com/example/webapp/service/StockServiceTest.java \
        WebApp/src/test/java/com/example/webapp/service/UserServiceTest.java
git commit -m "Add StockService and UserService"
```

---

### Task 6: Web controllers, Thymeleaf templates, and permission-gating test

**Files:**
- Create: `WebApp/src/main/java/com/example/webapp/web/LoginController.java`
- Create: `WebApp/src/main/java/com/example/webapp/web/OrdersController.java`
- Create: `WebApp/src/main/java/com/example/webapp/web/StockController.java`
- Create: `WebApp/src/main/java/com/example/webapp/web/UsersController.java`
- Create: `WebApp/src/main/resources/templates/login.html`
- Create: `WebApp/src/main/resources/templates/orders.html`
- Create: `WebApp/src/main/resources/templates/stock.html`
- Create: `WebApp/src/main/resources/templates/users.html`
- Test: `WebApp/src/test/java/com/example/webapp/web/PermissionGatingTest.java`

**Interfaces:**
- Consumes: `OrderService`, `StockService`, `UserService` (Tasks 4-5), `UserRepository` (Task 2).
- Produces: routes `GET/POST /orders`, `GET /stock`, `POST /stock/create`, `POST /stock/update` (`@PreAuthorize("hasAuthority('MANAGE_STOCK')")`), `GET /users`, `POST /users/create`, `POST /users/update` (class-level `@PreAuthorize("hasAuthority('MANAGE_USERS')")`), `GET /login`.

- [ ] **Step 1: Write the failing test**

`WebApp/src/test/java/com/example/webapp/web/PermissionGatingTest.java`:

```java
package com.example.webapp.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PermissionGatingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "plain")
    void anyLoggedInUserCanViewStock() throws Exception {
        mockMvc.perform(get("/stock")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "plain")
    void creatingStockItemRequiresManageStock() throws Exception {
        mockMvc.perform(post("/stock/create").with(csrf())
                        .param("name", "Thing").param("quantity", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", authorities = "MANAGE_STOCK")
    void creatingStockItemSucceedsWithManageStock() throws Exception {
        mockMvc.perform(post("/stock/create").with(csrf())
                        .param("name", "Thing").param("quantity", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "plain")
    void usersPageRequiresManageUsers() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "MANAGE_USERS")
    void usersPageAccessibleWithManageUsers() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q test -Dtest=PermissionGatingTest`
Expected: FAIL (compilation error / 404s — controllers and templates don't exist yet).

- [ ] **Step 3: Write the controllers**

`WebApp/src/main/java/com/example/webapp/web/LoginController.java`:

```java
package com.example.webapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
```

`WebApp/src/main/java/com/example/webapp/web/OrdersController.java`:

```java
package com.example.webapp.web;

import com.example.webapp.model.Order;
import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.InsufficientStockException;
import com.example.webapp.service.OrderRequestLine;
import com.example.webapp.service.OrderService;
import com.example.webapp.service.StockService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.IntStream;

@Controller
public class OrdersController {

    private final OrderService orderService;
    private final StockService stockService;
    private final UserRepository userRepository;

    public OrdersController(OrderService orderService, StockService stockService, UserRepository userRepository) {
        this.orderService = orderService;
        this.stockService = stockService;
        this.userRepository = userRepository;
    }

    @GetMapping("/orders")
    public String listOrders(@RequestParam(name = "all", required = false, defaultValue = "false") boolean all,
                              Authentication authentication, Model model) {
        User currentUser = currentUser(authentication);
        boolean canViewAll = hasAuthority(authentication, "VIEW_ALL_ORDERS");
        boolean viewingAll = all && canViewAll;

        List<Order> orders = viewingAll ? orderService.findAllOrders() : orderService.findOrdersByCreator(currentUser);

        model.addAttribute("orders", orders);
        model.addAttribute("viewingAll", viewingAll);
        model.addAttribute("canViewAll", canViewAll);
        model.addAttribute("stockItems", stockService.findAll());
        return "orders";
    }

    @PostMapping("/orders")
    public String createOrder(@RequestParam("stockItemId") List<Long> stockItemIds,
                               @RequestParam("quantity") List<Integer> quantities,
                               Authentication authentication, Model model) {
        User currentUser = currentUser(authentication);
        List<OrderRequestLine> lines = buildLines(stockItemIds, quantities);

        try {
            orderService.createOrder(currentUser, lines);
        } catch (InsufficientStockException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("orders", orderService.findOrdersByCreator(currentUser));
            model.addAttribute("viewingAll", false);
            model.addAttribute("canViewAll", hasAuthority(authentication, "VIEW_ALL_ORDERS"));
            model.addAttribute("stockItems", stockService.findAll());
            return "orders";
        }

        return "redirect:/orders";
    }

    private List<OrderRequestLine> buildLines(List<Long> stockItemIds, List<Integer> quantities) {
        return IntStream.range(0, stockItemIds.size())
                .filter(i -> quantities.get(i) != null && quantities.get(i) > 0)
                .mapToObj(i -> new OrderRequestLine(stockItemIds.get(i), quantities.get(i)))
                .toList();
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName()).orElseThrow();
    }
}
```

`WebApp/src/main/java/com/example/webapp/web/StockController.java`:

```java
package com.example.webapp.web;

import com.example.webapp.service.StockService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/stock")
    public String listStock(Model model) {
        model.addAttribute("stockItems", stockService.findAll());
        return "stock";
    }

    @PostMapping("/stock/create")
    @PreAuthorize("hasAuthority('MANAGE_STOCK')")
    public String createItem(@RequestParam String name, @RequestParam int quantity) {
        stockService.createItem(name, quantity);
        return "redirect:/stock";
    }

    @PostMapping("/stock/update")
    @PreAuthorize("hasAuthority('MANAGE_STOCK')")
    public String updateQuantity(@RequestParam Long id, @RequestParam int quantity) {
        stockService.updateQuantity(id, quantity);
        return "redirect:/stock";
    }
}
```

`WebApp/src/main/java/com/example/webapp/web/UsersController.java`:

```java
package com.example.webapp.web;

import com.example.webapp.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasAuthority('MANAGE_USERS')")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String username, @RequestParam String password,
                              @RequestParam(required = false, defaultValue = "false") boolean manageUsers,
                              @RequestParam(required = false, defaultValue = "false") boolean manageStock,
                              @RequestParam(required = false, defaultValue = "false") boolean viewAllOrders) {
        userService.createUser(username, password, manageUsers, manageStock, viewAllOrders);
        return "redirect:/users";
    }

    @PostMapping("/users/update")
    public String updatePermissions(@RequestParam Long id,
                                     @RequestParam(required = false, defaultValue = "false") boolean manageUsers,
                                     @RequestParam(required = false, defaultValue = "false") boolean manageStock,
                                     @RequestParam(required = false, defaultValue = "false") boolean viewAllOrders) {
        userService.updatePermissions(id, manageUsers, manageStock, viewAllOrders);
        return "redirect:/users";
    }
}
```

- [ ] **Step 4: Write the templates**

`WebApp/src/main/resources/templates/login.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Login</title></head>
<body>
<h1>Login</h1>
<p th:if="${param.error}" id="login-error">Invalid username or password.</p>
<form th:action="@{/login}" method="post">
    <label for="username">Username</label>
    <input type="text" id="username" name="username"/>
    <label for="password">Password</label>
    <input type="password" id="password" name="password"/>
    <button type="submit" id="login-submit">Log in</button>
</form>
</body>
</html>
```

`WebApp/src/main/resources/templates/orders.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head><title>Orders</title></head>
<body>
<nav>
    <a href="/orders">Orders</a> | <a href="/stock">Stock</a> |
    <a href="/users" sec:authorize="hasAuthority('MANAGE_USERS')">Users</a> |
    <form th:action="@{/logout}" method="post" style="display:inline"><button type="submit">Log out</button></form>
</nav>
<h1>Orders</h1>
<p th:if="${error}" id="order-error" th:text="${error}"></p>

<a th:if="${canViewAll}" id="view-all-toggle"
   th:href="${viewingAll} ? '/orders' : '/orders?all=true'"
   th:text="${viewingAll} ? 'View my orders' : 'View all orders'"></a>

<table id="orders-table">
    <thead><tr><th>Id</th><th>Creator</th><th>When</th><th>Status</th><th>Items</th></tr></thead>
    <tbody>
    <tr th:each="order : ${orders}" class="order-row">
        <td th:text="${order.id}"></td>
        <td th:text="${order.creator.username}"></td>
        <td th:text="${order.createdAt}"></td>
        <td th:text="${order.status}"></td>
        <td>
            <span th:each="line : ${order.lines}" th:text="${line.stockItem.name} + ' x' + ${line.quantity} + ' '"></span>
        </td>
    </tr>
    </tbody>
</table>

<h2>New Order</h2>
<form th:action="@{/orders}" method="post" id="new-order-form">
    <div th:each="item : ${stockItems}">
        <input type="hidden" th:name="stockItemId" th:value="${item.id}"/>
        <label th:for="${'quantity-' + item.id}" th:text="${item.name} + ' (available: ' + ${item.quantity} + ')'"></label>
        <input type="number" th:name="quantity" th:id="${'quantity-' + item.id}" min="0" value="0"/>
    </div>
    <button type="submit" id="create-order-submit">Create Order</button>
</form>
</body>
</html>
```

`WebApp/src/main/resources/templates/stock.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head><title>Stock</title></head>
<body>
<nav>
    <a href="/orders">Orders</a> | <a href="/stock">Stock</a> |
    <a href="/users" sec:authorize="hasAuthority('MANAGE_USERS')">Users</a>
</nav>
<h1>Stock</h1>
<table id="stock-table">
    <thead><tr><th>Id</th><th>Name</th><th>Quantity</th></tr></thead>
    <tbody>
    <tr th:each="item : ${stockItems}" class="stock-row">
        <td th:text="${item.id}"></td>
        <td th:text="${item.name}"></td>
        <td th:text="${item.quantity}"></td>
    </tr>
    </tbody>
</table>

<div sec:authorize="hasAuthority('MANAGE_STOCK')">
    <h2>Add Stock Item</h2>
    <form th:action="@{/stock/create}" method="post" id="create-stock-form">
        <label for="new-item-name">Name</label>
        <input type="text" id="new-item-name" name="name"/>
        <label for="new-item-quantity">Quantity</label>
        <input type="number" id="new-item-quantity" name="quantity" min="0"/>
        <button type="submit" id="create-stock-submit">Add Item</button>
    </form>

    <h2>Update Quantity</h2>
    <form th:action="@{/stock/update}" method="post" id="update-stock-form">
        <label for="update-item-id">Item Id</label>
        <input type="number" id="update-item-id" name="id"/>
        <label for="update-item-quantity">New Quantity</label>
        <input type="number" id="update-item-quantity" name="quantity" min="0"/>
        <button type="submit" id="update-stock-submit">Update</button>
    </form>
</div>
</body>
</html>
```

`WebApp/src/main/resources/templates/users.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Users</title></head>
<body>
<nav>
    <a href="/orders">Orders</a> | <a href="/stock">Stock</a> | <a href="/users">Users</a>
</nav>
<h1>Users</h1>
<table id="users-table">
    <thead><tr><th>Id</th><th>Username</th><th>Manage Users</th><th>Manage Stock</th><th>View All Orders</th></tr></thead>
    <tbody>
    <tr th:each="u : ${users}" class="user-row">
        <td th:text="${u.id}"></td>
        <td th:text="${u.username}"></td>
        <td th:text="${u.manageUsers}"></td>
        <td th:text="${u.manageStock}"></td>
        <td th:text="${u.viewAllOrders}"></td>
    </tr>
    </tbody>
</table>

<h2>Create User</h2>
<form th:action="@{/users/create}" method="post" id="create-user-form">
    <label for="new-username">Username</label>
    <input type="text" id="new-username" name="username"/>
    <label for="new-password">Password</label>
    <input type="password" id="new-password" name="password"/>
    <label for="new-manageUsers">Manage Users</label>
    <input type="checkbox" id="new-manageUsers" name="manageUsers"/>
    <label for="new-manageStock">Manage Stock</label>
    <input type="checkbox" id="new-manageStock" name="manageStock"/>
    <label for="new-viewAllOrders">View All Orders</label>
    <input type="checkbox" id="new-viewAllOrders" name="viewAllOrders"/>
    <button type="submit" id="create-user-submit">Create</button>
</form>

<h2>Update Permissions</h2>
<form th:action="@{/users/update}" method="post" id="update-user-form">
    <label for="update-user-id">User Id</label>
    <input type="number" id="update-user-id" name="id"/>
    <label for="update-manageUsers">Manage Users</label>
    <input type="checkbox" id="update-manageUsers" name="manageUsers"/>
    <label for="update-manageStock">Manage Stock</label>
    <input type="checkbox" id="update-manageStock" name="manageStock"/>
    <label for="update-viewAllOrders">View All Orders</label>
    <input type="checkbox" id="update-viewAllOrders" name="viewAllOrders"/>
    <button type="submit" id="update-user-submit">Update</button>
</form>
</body>
</html>
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `mvn -q test -Dtest=PermissionGatingTest`
Expected: PASS

- [ ] **Step 6: Run the full suite**

Run: `mvn -q test`
Expected: PASS (all tests from Tasks 1-6 green).

- [ ] **Step 7: Commit**

```bash
git add WebApp/src/main/java/com/example/webapp/web WebApp/src/main/resources/templates \
        WebApp/src/test/java/com/example/webapp/web
git commit -m "Add web controllers, Thymeleaf pages, and permission-gating test"
```

---

### Task 7: Seed data

**Files:**
- Create: `WebApp/src/main/java/com/example/webapp/config/SeedDataRunner.java`
- Test: `WebApp/src/test/java/com/example/webapp/config/SeedDataRunnerTest.java`

**Interfaces:**
- Consumes: `UserRepository`, `StockItemRepository` (Task 2), `UserService` (Task 5).
- Produces: `SeedDataRunner implements CommandLineRunner` — a no-arg-constructible `@Component` that seeds 4 users and 4 stock items only when `UserRepository.count() == 0`.

- [ ] **Step 1: Write the failing test**

`WebApp/src/test/java/com/example/webapp/config/SeedDataRunnerTest.java`:

```java
package com.example.webapp.config;

import com.example.webapp.model.User;
import com.example.webapp.repository.StockItemRepository;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SeedDataRunnerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    private SeedDataRunner runner() {
        UserService userService = new UserService(userRepository, new BCryptPasswordEncoder());
        return new SeedDataRunner(userRepository, userService, stockItemRepository);
    }

    @Test
    void seedsFourUsersWithExpectedPermissionsWhenTableEmpty() throws Exception {
        runner().run();

        assertThat(userRepository.findByUsername("admin").orElseThrow().isManageUsers()).isTrue();
        assertThat(userRepository.findByUsername("stockmanager").orElseThrow().isManageStock()).isTrue();
        assertThat(userRepository.findByUsername("stockmanager").orElseThrow().isManageUsers()).isFalse();
        assertThat(userRepository.findByUsername("orderviewer").orElseThrow().isViewAllOrders()).isTrue();
        assertThat(stockItemRepository.count()).isEqualTo(4);
    }

    @Test
    void doesNotReseedWhenUsersAlreadyExist() throws Exception {
        userRepository.save(new User("existing", "hash", false, false, false));

        runner().run();

        assertThat(userRepository.count()).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q test -Dtest=SeedDataRunnerTest`
Expected: FAIL (compilation error — `SeedDataRunner` doesn't exist yet).

- [ ] **Step 3: Write `SeedDataRunner`**

`WebApp/src/main/java/com/example/webapp/config/SeedDataRunner.java`:

```java
package com.example.webapp.config;

import com.example.webapp.model.StockItem;
import com.example.webapp.repository.StockItemRepository;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final StockItemRepository stockItemRepository;

    public SeedDataRunner(UserRepository userRepository, UserService userService, StockItemRepository stockItemRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.stockItemRepository = stockItemRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        userService.createUser("admin", "password", true, true, true);
        userService.createUser("user", "password", false, false, false);
        userService.createUser("stockmanager", "password", false, true, false);
        userService.createUser("orderviewer", "password", false, false, true);

        stockItemRepository.save(new StockItem("Widget", 50));
        stockItemRepository.save(new StockItem("Gadget", 20));
        stockItemRepository.save(new StockItem("Gizmo", 5));
        stockItemRepository.save(new StockItem("Sprocket", 0));
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn -q test -Dtest=SeedDataRunnerTest`
Expected: PASS

- [ ] **Step 5: Run the full suite**

Run: `mvn -q test`
Expected: PASS. (`SeedDataRunner` now also runs automatically inside every `@SpringBootTest`-context test from Tasks 4-6 at context startup — those tests already assert on specific created entities/ids rather than absolute counts, per the Global Constraints, so this is safe.)

- [ ] **Step 6: Commit**

```bash
git add WebApp/src/main/java/com/example/webapp/config WebApp/src/test/java/com/example/webapp/config
git commit -m "Add startup seed data for demo users and stock items"
```

---

### Task 8: GitHub Actions CI/CD workflows and README

**Files:**
- Create: `.github/workflows/ci.yml`
- Create: `.github/workflows/release.yml`
- Create: `WebApp/README.md`

**Interfaces:**
- Consumes: `WebApp/pom.xml` (Task 1) — both workflows run `mvn` with `working-directory: WebApp`.
- Produces: nothing consumed by later tasks (this is the final task).

- [ ] **Step 1: Write `ci.yml`**

`.github/workflows/ci.yml`:

```yaml
name: CI

on:
  pull_request:
    branches: [main]
  push:
    branches: [main]

jobs:
  smoke-tests:
    name: Smoke Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      # TODO: replace with the real Selenium/Java smoke test framework once available.
      # Convention: smoke test classes are named *SmokeTest and picked up by this pattern.
      - name: Run smoke tests
        working-directory: WebApp
        run: mvn -B test -Dtest='*SmokeTest' -DfailIfNoTests=false

  system-tests:
    name: System Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      # TODO: replace with the real Selenium/Java system test framework once available.
      # Convention: system test classes are named *SystemTest and picked up by this pattern.
      - name: Run system tests
        working-directory: WebApp
        run: mvn -B test -Dtest='*SystemTest' -DfailIfNoTests=false
      - name: Upload system test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: system-test-results
          path: WebApp/target/surefire-reports
          if-no-files-found: ignore

  build:
    name: Build
    needs: [smoke-tests, system-tests]
    runs-on: ubuntu-latest
    if: |
      always() &&
      needs.smoke-tests.result == 'success' &&
      (github.event_name != 'pull_request' || needs.system-tests.result == 'success')
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - name: Package
        working-directory: WebApp
        run: mvn -B -DskipTests package
      - name: Upload jar artifact
        uses: actions/upload-artifact@v4
        with:
          name: webapp-jar
          path: WebApp/target/*.jar
```

This gives: PRs require both `smoke-tests` and `system-tests` to succeed before `build` runs (and thus before the PR can merge, once branch protection requires the `build` check). Pushes to `main` only require `smoke-tests` to succeed; `system-tests` still runs and always uploads its Surefire report as a workflow artifact, but a failure there does not block `build` on a push.

- [ ] **Step 2: Write `release.yml`**

`.github/workflows/release.yml`:

```yaml
name: Release

on:
  push:
    tags:
      - 'v*.*.*'

jobs:
  smoke-tests:
    name: Smoke Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      # TODO: replace with the real Selenium/Java smoke test framework once available.
      - name: Run smoke tests
        working-directory: WebApp
        run: mvn -B test -Dtest='*SmokeTest' -DfailIfNoTests=false

  system-tests:
    name: System Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      # TODO: replace with the real Selenium/Java system test framework once available.
      - name: Run system tests
        working-directory: WebApp
        run: mvn -B test -Dtest='*SystemTest' -DfailIfNoTests=false

  build:
    name: Build Release Artifact
    needs: [smoke-tests, system-tests]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - name: Package
        working-directory: WebApp
        run: mvn -B -DskipTests package
      - name: Upload release jar artifact
        uses: actions/upload-artifact@v4
        with:
          name: webapp-release-${{ github.ref_name }}
          path: WebApp/target/*.jar
```

Here `build` uses the default (unconditioned) `needs: [smoke-tests, system-tests]`, so both suites must succeed with zero failures before the release jar is built and published — no bypass.

- [ ] **Step 3: Write the README**

`WebApp/README.md`:

```markdown
# WebApp

A small Spring Boot sample application used as the System Under Test for a
Selenium/Java QA automation portfolio project.

## Running locally

```bash
mvn spring-boot:run
```

The app listens on `http://localhost:8080` and stores its data in a
SQLite file, `webapp.db`, created in the working directory on first run.
Delete that file to reset all data back to the seeded defaults.

## Seed users

All seeded users share the password `password`:

| username | Manage Users | Manage Stock | View All Orders |
|---|---|---|---|
| admin | yes | yes | yes |
| user | no | no | no |
| stockmanager | no | yes | no |
| orderviewer | no | no | yes |

## Pages

- `/login` - form login
- `/orders` - view your own orders (or all orders, if you have View All Orders) and create a new order
- `/stock` - view stock items; create/update items if you have Manage Stock
- `/users` - create users and manage their permissions (requires Manage Users)
```

- [ ] **Step 4: Verify the full build one last time**

Run (from `WebApp/`): `mvn -q -B test` then `mvn -q -B -DskipTests package`
Expected: tests pass, and `WebApp/target/webapp-0.1.0.jar` is produced.

- [ ] **Step 5: Commit**

```bash
git add .github/workflows/ci.yml .github/workflows/release.yml WebApp/README.md
git commit -m "Add GitHub Actions CI/release workflows and README"
```

---

## Self-Review Notes

- **Spec coverage:** stack/build tool (Task 1), domain model (Task 2), auth/permission flags (Task 3), order creation + atomic stock decrement + insufficient-stock error (Task 4), stock/user management services (Task 5), all four pages with their access rules (Task 6), seed data with the four named permission combinations (Task 7), the three-tier PR/push/tag CI gating with placeholder smoke/system jobs and jar-artifact publishing (Task 8) — every spec section maps to a task.
- **Placeholder scan:** no TBD/TODO markers except the two intentional `# TODO` comments in the workflow files, which are part of the spec's design (marking where the future Selenium framework's test classes plug in), not unfinished plan content.
- **Type consistency:** `OrderRequestLine`, `InsufficientStockException`, `OrderService`, `StockService`, `UserService`, `SeedDataRunner` constructor signatures are used identically wherever referenced across Tasks 4-7.
