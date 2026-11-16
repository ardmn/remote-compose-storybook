package rcstorybook.catalog

import rcstorybook.stories.*

object Catalog {
    val components: List<Component> =
        layoutComponents +
            contentComponents +
            canvasComponents +
            modifierComponents +
            interactionComponents +
            animationComponents +
            stateComponents
}
