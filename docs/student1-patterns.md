# Student 1 - Bekzat Amanbek

Part of the store: product model, product catalog, ready-made setup kits, catalog and product pages.

## Factory Method

- Classes: `factory/ProductFactory`, `LaptopFactory`, `DesktopPCFactory`, `MonitorFactory`, `AccessoryFactory`.
- `ProductFactory.create(spec)` does the shared steps (checks the required attributes, gives the
  product an id). Each subclass makes its own kind of product.
- `ProductFactory.forCategory(category)` picks the factory, so the catalog never calls `new Laptop(...)`.
- See it: every product on `/catalog` is created this way when the store starts.
- Tests: `factory/ProductFactoryTest`.

## Abstract Factory

- Classes: `kit/SetupKitFactory`, `StudyKitFactory`, `GamingKitFactory`, `HomeOfficeKitFactory`.
- Each factory makes a family of products that fit together: a computer, a monitor and peripherals.
- `BundleCatalog` asks each factory for its family and turns it into a ready-made bundle with the kit discount.
- See it: the three kits on `/bundles`.
- Tests: `kit/SetupKitFactoryTest`.

## Builder

- Class: `builder/ProductBuilder`.
- A product has many optional specs (CPU, RAM, screen, refresh rate, connection, ...).
  The builder sets them step by step and `build()` checks the result.
- Example: `ProductBuilder.of(Category.LAPTOP).name("...").cpu("...").ramGb(16).price(389000).build()`.
- Tests: `builder/ProductBuilderTest`.

## How my part connects to the others

The products I create are what Student 2 wraps in services and bundles and what Student 3 sells at checkout.
