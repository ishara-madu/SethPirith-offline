package com.pixeleye.sethpirith.data

import com.pixeleye.sethpirith.R


object PirithDataProvider {

    val pirithList = listOf(
        Pirith(
            id = 0,
            titleResId = R.string.karaniya_title,
            audioResId = R.raw.karaniya,
            lyricsResId = R.string.karaniya_lyrics
        ),
        Pirith(
            id = 1,
            titleResId = R.string.rathana_title,
            audioResId = R.raw.rathana,
            lyricsResId = R.string.rathana_lyrics
        ),
        Pirith(
            id = 2,
            titleResId = R.string.mangala_title,
            audioResId = R.raw.mangala,
            lyricsResId = R.string.mangala_lyrics
        )
        // Add more Piriths here...
    )
}
