package com.charlyghislain.openopenradio.ui.home

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.media3.common.HeartRating
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.charlyghislain.openopenradio.service.media.MediaPlaybackService
import com.charlyghislain.openopenradio.ui.components.MyPlayerView
import com.charlyghislain.openopenradio.ui.theme.OpenOpenRadioTheme
import com.google.common.util.concurrent.Futures.immediateFuture
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var sessionToken: SessionToken
    private lateinit var backNavigationListener: BackNavigationListener

    private var controllerFuture by mutableStateOf<ListenableFuture<MediaController>>(
        immediateFuture(null)
    )
    private var browserFuture by mutableStateOf<ListenableFuture<MediaBrowser>>(immediateFuture(null))

    override fun onStart() {
        super.onStart()
        val context = requireContext();
        sessionToken =
            SessionToken(context, ComponentName(context, MediaPlaybackService::class.java))

        if (
            Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS), /* requestCode= */
                0
            )
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        val context = requireContext();
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        browserFuture = MediaBrowser.Builder(context, sessionToken).buildAsync()
    }

    override fun onPause() {
        super.onPause()
        MediaController.releaseFuture(controllerFuture)
        MediaBrowser.releaseFuture(browserFuture)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BackNavigationListener) {
            backNavigationListener = context
        } else {
            throw RuntimeException("$context must implement BackNavigationListener")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate a ComposeView
        return ComposeView(requireContext()).apply {
            setContent {
                OpenOpenRadioTheme {
                    HomePageContent(
                        controllerFuture,
                        browserFuture,
                        onBackNavigationAvailable = { a ->
                            backNavigationListener.onBackNavigationAvailable(
                                a
                            )
                        },
                        onNavigateUp = { callback ->
                            backNavigationListener.onNavigateUpCallback(callback) // Assign the callback
                        }
                    )
                }
            }
        }
    }

}


@Composable
fun HomePageContent(
    controllerFuture: ListenableFuture<MediaController>,
    browserFuture: ListenableFuture<MediaBrowser>,
    onBackNavigationAvailable: (Boolean) -> Unit,
    onNavigateUp: (() -> Unit) -> Unit // Callback to handle navigation up
) {
    var controller: MediaController? by remember { mutableStateOf(null) }
    var browser: MediaBrowser? by remember { mutableStateOf(null) }
    val reloadEventFlow = MutableSharedFlow<ReloadEvent>()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(controllerFuture) {
        controllerFuture.addListener({
            controller = controllerFuture.get()
        }, MoreExecutors.directExecutor())
    }
    LaunchedEffect(browserFuture) {
        browserFuture.addListener({
            browser = browserFuture.get()
        }, MoreExecutors.directExecutor())
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // Fixed-width component at the top
        controller?.let { mediaController ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                MyPlayerView(
                    player = mediaController,
                    controller = mediaController,
                    onSetRating = { item, hearth ->
                        mediaController.setRating( HeartRating(hearth)).get()
                        coroutineScope.launch { // Launch a coroutine
                            reloadEventFlow.emit(ReloadEvent(item.mediaId))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }

            browser?.let { mediaBrowser ->
                Navigation(
                    browser = mediaBrowser,
                    controller = mediaController,
                    onBackNavigationAvailable = onBackNavigationAvailable,
                    onNavigateUp = { callback -> onNavigateUp(callback) },
                    reloadEventFlow
                )
            }
        }

    }
}

interface BackNavigationListener {
    fun onBackNavigationAvailable(available: Boolean)

    fun onNavigateUpCallback(callback: () -> Unit)
}