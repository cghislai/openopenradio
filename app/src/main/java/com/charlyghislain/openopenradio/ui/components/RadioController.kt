package com.charlyghislain.openopenradio.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun RadioController(
    onClick: (String) -> Unit,
    sessionToken: SessionToken,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

}