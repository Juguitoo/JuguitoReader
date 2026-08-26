package com.juguito.juguitoreader.domain.exception

import androidx.annotation.StringRes

class JuguitoException(
    @StringRes val resId: Int,
    vararg val args: Any
) : Exception()
