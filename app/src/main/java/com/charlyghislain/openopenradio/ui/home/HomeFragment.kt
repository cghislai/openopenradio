package com.charlyghislain.openopenradio.ui.home

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.charlyghislain.openopenradio.service.media.MediaPlaybackService
import com.charlyghislain.openopenradio.ui.theme.OpenOpenRadioTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class HomeFragment : Fragment() {

    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    override fun onStart() {
        super.onStart()

        val context = requireContext();
        val sessionToken =
            SessionToken(context, ComponentName(context, MediaPlaybackService::class.java))

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            // MediaController is available here with controllerFuture.get()
        }, MoreExecutors.directExecutor())

        browserFuture = MediaBrowser.Builder(context, sessionToken).buildAsync()
        browserFuture.addListener({

            val mediaBrowser = browserFuture.get()

            // Get the library root to start browsing the library tree.
            val rootFuture = mediaBrowser.getLibraryRoot(/* params= */ null)
            rootFuture.addListener({
                val rootMediaItem = rootFuture.get().value
                if (rootMediaItem != null) {
                    val childrenFuture =
                        mediaBrowser.getChildren(rootMediaItem.mediaId, 0, Int.MAX_VALUE, null)
                    childrenFuture.addListener({
                        // List of children MediaItem nodes is available here with
                        // childrenFuture.get().value
                    }, MoreExecutors.directExecutor())
                }
            }, MoreExecutors.directExecutor())

        }, MoreExecutors.directExecutor())


    }

    override fun onStop() {
        super.onStop()
        MediaController.releaseFuture(controllerFuture)
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
                    HomePageContent() // Your Composable function
                }
            }
        }
    }
}


@Composable
fun HomePageContent() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Fixed-width component at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Fixed-Width Component")
        }

        Navigation()
    }
}

