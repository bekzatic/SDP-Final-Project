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

All data is kept in memory: the catalog is filled on start, orders disappear after a restart.
Payment is a demo: no card data is asked for.

## Pages

| Address | Page |
|---|---|
| `/`, `/catalog` | catalog with category filter and sorting |
| `/product/{id}` | product page: characteristics, warranty and services, "add to cart" |
| `/bundles` | ready-made bundles, some with a nested bundle |
| `/cart` | cart: quantities, promo codes (`STUDENT10`, `SALE15`, `CORE5000`), "Checkout" |
| `/checkout` | checkout form: contacts, pickup or courier, pay now or later |
| `/orders`, `/orders/{number}` | my orders: status, history, emails, pay or cancel |
| `/admin` | staff page (link in the footer): orders and their actions, stock, warehouse alerts |

Demo path: add a laptop with a warranty and a bundle to the cart, apply `STUDENT10`, check out
with "pay later", pay on the order page, then open `/admin` and move the order through
assembly, shipping and delivery. The order page shows every status change and email.

## Team plan

| Student | Patterns | Part of the store |
|---|---|---|
| Student 1 | Singleton, Factory Method, Builder | project setup, product model, product catalog, site design, catalog and product pages |
| Student 2 | Composite, Decorator, Strategy | product bundles, add-on services (warranty, gift wrap, software setup), promo-code discounts and catalog sorting, cart and bundles pages |
| Student 3 | Facade, State, Observer | checkout (`CheckoutFacade`), order statuses, notifications, checkout, my orders and admin pages |

## Patterns

| Pattern | Classes | What it does |
|---|---|---|
| Singleton | `catalog/ProductCatalog` | one shared catalog and stock for the whole store |
| Factory Method | `factory/ProductFactory`, `LaptopFactory`, `MonitorFactory`, ... | creates the right kind of product |
| Builder | `builder/ProductBuilder` | builds a product with many optional specs step by step |
| Composite | `bundle/StoreItem`, `ProductItem`, `ProductBundle` | a bundle of products (even of bundles) behaves like one product |
| Decorator | `addon/AddOnDecorator`, `WarrantyAddOn`, `GiftWrapAddOn`, `SetupAddOn` | paid services wrapped around a product or bundle |
| Strategy | `pricing/DiscountStrategy`, `sorting/SortStrategy` | promo-code discounts and catalog sorting, chosen while the store runs |
| Facade | `checkout/CheckoutFacade` | checkout in one call: form, stock, order, payment, notifications, cart |
| State | `order/OrderState`, `NewState`, `PaidState`, `AssemblingState`, `ShippedState`, `DeliveredState`, `CancelledState` | order status and what is allowed in it |
| Observer | `order/OrderObserver`, `notification/EmailNotifier`, `StockAlert`, `StockRestorer` | emails, low-stock alerts and stock return on order events |

Order statuses:

```
NEW (waiting for payment) -> PAID -> ASSEMBLING -> SHIPPED -> DELIVERED
   |                          |          |
   +--------------------------+----------+--> CANCELLED (money and stock go back)
```

Work order: Student 1, then Student 2 (builds on the products), then Student 3 (builds on the cart).
Each part is committed separately.
