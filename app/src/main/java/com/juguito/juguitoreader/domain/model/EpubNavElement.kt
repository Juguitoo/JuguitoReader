package com.juguito.juguitoreader.domain.model

data class EpubNavElement (
    val title: String = "",
    val href: String = "",
    val children: List<EpubNavElement> = emptyList()
)
