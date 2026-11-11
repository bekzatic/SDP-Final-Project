# CoreMart - online computer store

Final project for the Software Design Patterns course. CoreMart is a web store for computer
hardware: laptops, desktop PCs, monitors and accessories. Customers browse the catalog, open a
product page, add services, put products and bundles into the cart, check out and track their
orders; staff manage stock and order statuses in an admin page.

Built with Java 17 and Spring Boot 3 (Thymeleaf pages). Nine design patterns, three per student.

## Run

Requires JDK 17+. Maven is not needed: the Maven wrapper downloads it.

```bash
./mvnw spring-boot:run        # macOS / Linux
mvnw.cmd spring-boot:run      # Windows
```

Then open http://localhost:8080. Run the tests with `./mvnw test`.

## Team plan

| Student | Patterns | Part of the store |
|---|---|---|
| Student 1 | Singleton, Factory Method, Builder | project setup, product model, product catalog, site design, catalog and product pages |
| Student 2 | Composite, Decorator, Strategy | product bundles, add-on services (warranty, gift wrap, software setup), promo-code discounts and catalog sorting, cart and bundles pages |
| Student 3 | Facade, State, Observer | checkout (`CheckoutFacade`), order statuses, notifications, checkout, my orders and admin pages |

| Pattern | Class | What it does |
|---|---|---|
| Singleton | `ProductCatalog` | one shared catalog and stock for the whole store |
| Factory Method | `LaptopFactory`, `MonitorFactory`, ... | creates the right kind of product |
| Builder | `ProductBuilder` | builds a product with many optional specs step by step |
| Composite | `ProductBundle` | a bundle of products behaves like one product |
| Decorator | `WarrantyAddOn`, `GiftWrapAddOn`, `SetupAddOn` | paid services wrapped around a product |
| Strategy | `DiscountStrategy`, `SortStrategy` | promo-code discounts and catalog sorting |
| Facade | `CheckoutFacade` | checkout in one call: stock, payment, order, notifications |
| State | `PaidState`, `AssemblingState`, ... | order status and what is allowed in it |
| Observer | `EmailNotifier`, `StockAlert`, ... | notifications on status and stock changes |

Work order: Student 1, then Student 2 (builds on the products), then Student 3 (builds on the cart).
Each part is committed separately.
