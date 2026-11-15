# Student 2 - Ermek Sain

Part of the store: product bundles, add-on services, promo codes, catalog sorting, the cart and bundles pages.

## Decorator

- Classes: `addon/AddOnDecorator`, `WarrantyAddOn`, `GiftWrapAddOn`, `SetupAddOn`.
- A service wraps any `StoreItem` (a product, a bundle or an item that already has services)
  and adds its price. Services stack in any combination without a subclass for each one.
- See it: choose a warranty and gift wrap on a product page, then open `/cart`.
- Tests: `addon/AddOnDecoratorTest`.

## Strategy

- Classes: `pricing/DiscountStrategy` (`PercentageDiscount`, `FixedAmountDiscount`, `NoDiscount`)
  and `sorting/SortStrategy` (`PriceAscendingSort`, `PriceDescendingSort`, `NameSort`, `StockSort`, ...).
- The cart gets its discount from the promo code (`PromoCodes.find`), the catalog gets its sorting
  from the dropdown (`SortStrategies.byCode`). Both are swapped while the store runs.
- See it: promo codes `STUDENT10`, `SALE15`, `CORE5000` in the cart; the sort dropdown on `/catalog`.
- Tests: `pricing/DiscountStrategyTest`, `sorting/SortStrategyTest`.

## Visitor

- Classes: `visitor/StoreItemVisitor`, `DetailsVisitor`, `ContentsVisitor`, `ComputerCheckVisitor`.
- Products, bundles and items with services accept a visitor. New operations on cart items
  (detail lines, list of products, "is there a computer inside?") are added as new visitors,
  without changing the item classes.
- See it: the detail lines under each cart line; software setup is offered only for items with a computer.
- Tests: `visitor/StoreItemVisitorTest`.

## How my part connects to the others

I build on the products from Student 1 (bundles come from his setup kits), and the cart I made
is what Student 3's checkout turns into an order.
