package org.jetbrains.kotlinx.dataframe.jupyter

import org.jetbrains.kotlinx.dataframe.io.*
import org.jetbrains.kotlinx.dataframe.jupyter.KotlinNotebookPluginUtils.convertToDataFrame
import org.jetbrains.kotlinx.dataframe.nrow
import org.jetbrains.kotlinx.jupyter.api.*
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration

/** Starting from this version, dataframe integration will respond with additional data for rendering in Kotlin Notebooks plugin. */
private const val MIN_KERNEL_VERSION_FOR_NEW_TABLES_UI = "0.11.0.311"

internal class JupyterHtmlRenderer(
    val display: DisplayConfiguration,
    val builder: JupyterIntegration.Builder,
)

internal inline fun <reified T : Any> JupyterHtmlRenderer.render(
    noinline getFooter: (T) -> String,
    crossinline modifyConfig: T.(DisplayConfiguration) -> DisplayConfiguration = { it },
    applyRowsLimit: Boolean = true,
) = builder.renderWithHost<T> { host, value ->
    val contextRenderer = JupyterCellRenderer(this.notebook, host)
    val reifiedDisplayConfiguration = value.modifyConfig(display)
    val footer = getFooter(value)

    val df = convertToDataFrame(value)

    val limit = if (applyRowsLimit) {
        reifiedDisplayConfiguration.rowsLimit ?: df.nrow
    } else {
        df.nrow
    }

    val html = DataFrameHtmlData.tableDefinitions(
        includeJs = reifiedDisplayConfiguration.isolatedOutputs,
        includeCss = true,
    ).plus(
        df.toHTML(
            // is added later to make sure it's put outside of potential iFrames
            configuration = reifiedDisplayConfiguration.copy(enableFallbackStaticTables = false),
            cellRenderer = contextRenderer,
        ) { footer }
    ).toJupyterHtmlData()

    // Generates a static version of the table which can be displayed in GitHub previews etc.
    val staticHtml = df.toStaticHtml(reifiedDisplayConfiguration, DefaultCellRenderer).toJupyterHtmlData()

    if (notebook.kernelVersion >= KotlinKernelVersion.from(MIN_KERNEL_VERSION_FOR_NEW_TABLES_UI)!!) {
        val jsonEncodedDf = df.toJsonWithMetadata(limit, reifiedDisplayConfiguration.rowsLimit)
        notebook.renderAsIFrameAsNeeded(html, staticHtml, jsonEncodedDf.toJsonString())
    } else {
        notebook.renderHtmlAsIFrameIfNeeded(html)
    }
}

internal fun Notebook.renderAsIFrameAsNeeded(
    data: HtmlData,
    staticData: HtmlData,
    jsonEncodedDf: String
): MimeTypedResult {
    val textHtml = if (jupyterClientType == JupyterClientType.KOTLIN_NOTEBOOK) {
        data.generateIframePlaneText(currentColorScheme) +
            staticData.toString(currentColorScheme)
    } else {
        (data + staticData).toString(currentColorScheme)
    }

    return mimeResult(
        "text/html" to textHtml,
        "application/kotlindataframe+json" to jsonEncodedDf
    ).also { it.isolatedHtml = false }
}

internal fun DataFrameHtmlData.toJupyterHtmlData() = HtmlData(style, body, script)
