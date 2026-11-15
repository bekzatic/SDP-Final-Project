# Student 3 - Batyrkhan Uvayev

Part of the store: checkout, order statuses, notifications, bank payments, checkout, my orders and admin pages.

## Facade

- Class: `checkout/CheckoutFacade`.
- One call `placeOrder` hides the whole checkout: it checks the form and stock, creates the order,
  takes the payment, tells the observers and clears the cart. The controller knows nothing else.
- See it: "Checkout" on `/cart`, then the form on `/checkout`.
- Tests: `checkout/CheckoutFacadeTest`, `web/CheckoutPageTest`.

## Observer

- Classes: `order/OrderObserver`, `notification/EmailNotifier`, `StockAlert`, `StockRestorer`.
- An order tells its observers when it is placed and when its status changes.
  Emails, low-stock alerts and returning stock after a cancel are separate observers,
  so a new reaction is a new class, not a change in `Order`.
- See it: the emails on `/orders/{number}` and the warehouse alerts on `/admin`.
- Tests: `notification/OrderObserverTest`, `order/OrderStatusTest`.

## Adapter

- Classes: `payment/PaymentGateway`, `BankPaymentAdapter`, `DemoBankApi`.
- The checkout pays through `PaymentGateway.charge(orderNumber, amountTenge)`. The bank API has
  a different interface (amount in minor units, currency code, error codes), and
  `BankPaymentAdapter` translates between them.
- See it: "Paid, payment TX-..." after paying an order.
- Tests: `payment/BankPaymentAdapterTest`.

## How my part connects to the others

Checkout takes the cart from Student 2 (with its decorators and discount) and the products
from Student 1, and turns them into an order that moves through its statuses.
