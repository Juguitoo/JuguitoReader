package com.juguito.juguitoreader.ui.changelog

import androidx.annotation.ArrayRes
import com.juguito.juguitoreader.R

data class ChangelogUiRelease(val versionName: String, @ArrayRes val highlightsRes: Int)

object ChangelogUiCatalog {
    val newestFirst = listOf(
        ChangelogUiRelease("1.2.1", R.array.changelog_1_2_1),
        ChangelogUiRelease("1.2.0", R.array.changelog_1_2_0),
        ChangelogUiRelease("1.1.0", R.array.changelog_1_1_0),
        ChangelogUiRelease("1.0.1", R.array.changelog_1_0_1),
        ChangelogUiRelease("1.0.0", R.array.changelog_1_0_0),
    )
}
