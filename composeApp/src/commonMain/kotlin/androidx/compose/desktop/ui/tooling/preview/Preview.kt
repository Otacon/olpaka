package androidx.compose.desktop.ui.tooling.preview

// Little workaround to make the Compose preview work even in the commonMain package
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
expect annotation class Preview()