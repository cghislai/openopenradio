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
import androidx.compose.runtime.MutableState
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
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.HeartRating
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.charlyghislain.openopenradio.service.OpenOpenRadioPlaybackServiceOpenOpenRadio
import com.charlyghislain.openopenradio.ui.components.MyPlayerView
import com.charlyghislain.openopenradio.ui.model.MainViewModel
import com.charlyghislain.openopenradio.ui.model.NestedNavState
import com.charlyghislain.openopenradio.ui.theme.OpenOpenRadioTheme
import com.google.common.util.concurrent.Futures.immediateFuture
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val sessionTokenReference: MutableState<SessionToken?> = mutableStateOf(null)
    private lateinit var mainViewModel: MainViewModel

    private var controllerFuture by mutableStateOf<ListenableFuture<MediaController>>(
        immediateFuture(null)
    )
    private var browserFuture by mutableStateOf<ListenableFuture<MediaBrowser>>(immediateFuture(null))

    override fun onStart() {
        super.onStart()
        val context = requireContext();
        sessionTokenReference.value =
            SessionToken(context, ComponentName(context, OpenOpenRadioPlaybackServiceOpenOpenRadio::class.java))
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

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

        sessionTokenReference.value?.let { token ->
            controllerFuture = MediaController.Builder(context, token)
                .buildAsync()
            browserFuture = MediaBrowser.Builder(context, token)
                .buildAsync()
        }
    }

    override fun onPause() {
        super.onPause()
        MediaController.releaseFuture(controllerFuture)
        MediaBrowser.releaseFuture(browserFuture)
//        mainViewModel.nestedNavState.value = NestedNavState()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate a ComposeView
        return ComposeView(requireContext()).apply {
            setContent {
                OpenOpenRadioTheme {
                    val nestedNavController = rememberNavController() // For the Compose NavHost
                    mainViewModel.nestedNavigationUpHandler = { nestedNavController.navigateUp() }
                    HomePageContent(
                        controllerFuture,
                        browserFuture,
                        nestedNavState = mainViewModel.nestedNavState,
                        navController = nestedNavController,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}


@Composable
fun HomePageContent(
    controllerFuture: ListenableFuture<MediaController>,
    browserFuture: ListenableFuture<MediaBrowser>,
    nestedNavState: MutableStateFlow<NestedNavState>,
    navController: NavHostController,
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
                        mediaController.setRating(HeartRating(hearth)).get()
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
                    navController = navController,
                    nestedNavState = nestedNavState,
                    reloadEventFlow = reloadEventFlow
                )
            }
        }

    }
}

