# LDLib2 — Configurable (in-game inspector)

Package `com.lowdragmc.lowdraglib2.configurator`. Lets you annotate a data object's fields and have LDLib2 auto-build an editor UI (rows of configurators) inside an `Inspector`. The object does not need to know about the editor.

## `IConfigurable` + `@Configurable`

```kotlin
import com.lowdragmc.lowdraglib2.configurator.IConfigurable
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber

class ShopEntry : IConfigurable {
    @Configurable(name = "Display Name")
    var displayName = "Apple"

    @Configurable(name = "Price", tips = "Cost paid by the player")
    @ConfigNumber(range = [0, 9999], wheel = 1)
    var price = 10

    @Configurable(name = "Enabled")
    var enabled = true
}
```

Default `buildConfigurator(group)` scans `@Configurable` fields and creates a row per field. Show it:

```kotlin
val entry = ShopEntry()
editor.inspectorView.inspect(entry)      // editor = an Editor instance (in-game editor)
// or standalone:
val inspector = Inspector(); inspector.inspect(entry)
```

## Manual configurators

Override `buildConfigurator(father: ConfiguratorGroup)` for custom layout / generated rows / controls that don't map to a field:

```kotlin
class RuntimeInfo : IConfigurable {
    override fun buildConfigurator(father: ConfiguratorGroup) {
        father.addConfigurator(StringConfigurator(
            "Status", { "Running" }, { }, "Running", false
        ))
    }
}
```

Mix both styles:

```kotlin
override fun buildConfigurator(father: ConfiguratorGroup) {
    super.buildConfigurator(father)                       // scan @Configurable fields
    father.addConfigurator(HeaderConfigurator("Runtime", 5))
}
```

Once you bypass field parsing you own setter calls, model updates, and change notifications.

## Annotations (overview)

Full list at `en/ldlib2/configurable/annotations.html`. Common ones:

- `@Configurable(name=, tips=, requiresRestart=, ...)` — the base field annotation.
- `@ConfigNumber(range=[min,max], wheel=N)` — numeric field with range + scroll-wheel step.
- `@ConfigBoolean` — boolean toggle.
- `@ConfigString(multiline=)` — string (optionally multiline).
- `@ConfigEnum` — enum dropdown.
- `@ConfigColor` — color picker.
- `@ConfigTag` — tag (resource location) field.
- `@ConfigList` — list editor (element type inferred).
- `@ConfigNested` — recurse into a nested `IConfigurable`.

Accessors (`en/ldlib2/configurable/accessors.html`) let you expose computed or non-field-backed values to the inspector.

## Inspector & history

`Inspector` displays the configurators built from an `IConfigurable`. The editor tracks an **undo/redo history** of edits made through the inspector — see `en/ldlib2/configurable/inspector-and-history.html` for the history API and how to integrate custom configurators with it.

## Source examples

`en/ldlib2/configurable/examples.html` has end-to-end examples (annotation-driven, manual, nested, custom accessor). The repo also ships `src/main/java/com/lowdragmc/lowdraglib2/test/ui/TestConfigurators.java` — a good runnable reference.

## When to use Configurable vs UI data bindings

- **Configurable** = edit an in-memory data object (config, recipe, editor document) via an auto-generated form. Best for editors and config screens.
- **UI data bindings** (see `references/ui.md`) = build a custom game-facing GUI synced with server state. Best for machine / container UIs.

They compose: an `Inspector` is itself a `UIElement`, so you can embed one inside a larger `ModularUI`.