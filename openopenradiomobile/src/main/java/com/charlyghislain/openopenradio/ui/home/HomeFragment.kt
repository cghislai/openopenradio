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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.charlyghislain.openopenradio.service.OpenOpenRadioPlaybackServiceOpenOpenRadio
import com.charlyghislain.openopenradio.ui.components.MyPlayerView
import com.charlyghislain.openopenradio.ui.components.RadioControllerViewModel
import com.charlyghislain.openopenradio.ui.model.MainViewModel
import com.charlyghislain.openopenradio.ui.model.NestedNavState
import com.charlyghislain.openopenradio.ui.theme.OpenOpenRadioTheme
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

class HomeFragment : Fragment() {

    private lateinit var token: SessionToken
    private lateinit var mainViewModel: MainViewModel
    private lateinit var radioViewModel: RadioControllerViewModel

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var browserFuture: ListenableFuture<MediaBrowser>? = null
    private val controller = mutableStateOf<MediaController?>(null)
    private var browser = mutableStateOf<MediaBrowser?>(null)

    override fun onStart() {
        super.onStart()
        val context = requireContext();

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        radioViewModel =
            ViewModelProvider(requireActivity()).get(RadioControllerViewModel::class.java)
        token = SessionToken(
            context,
            ComponentName(context, OpenOpenRadioPlaybackServiceOpenOpenRadio::class.java)
        )

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
        radioViewModel.setController(controller.value)
        reconnectMedia(context, token)
    }

    override fun onPause() {
        super.onPause()
        disconnectController()
        disconnectBrowser()
    }

    private fun reconnectMedia(
        context: Context,
        token: SessionToken
    ) {
        reconnectController(context, token)
        reconnectBrowser(context, token)
    }

    private fun reconnectController(
        context: Context,
        token: SessionToken
    ) {
//        disconnectController()
        controllerFuture = MediaController.Builder(context, token)
            .buildAsync()
        controllerFuture!!.addListener({
            try {
                controller.value = controllerFuture?.get()
            } catch (e: Exception) {
                controller.value = null
            }
            radioViewModel.setController(controller.value)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun disconnectController() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        controller.value = null
    }

    private fun reconnectBrowser(
        context: Context,
        token: SessionToken
    ) {
//        disconnectBrowser()
        browserFuture = MediaBrowser.Builder(context, token)
            .buildAsync()
        browserFuture!!.addListener({
            try {
                browser.value = browserFuture?.get()
            } catch (e: Exception) {
                browser.value = null
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun disconnectBrowser() {
        browserFuture?.let {
            MediaBrowser.releaseFuture(it)
        }
        browser.value = null
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
                    val mediaBrowser by remember { browser }

                    HomePageContent(
                        radioViewModel,
                        mediaBrowser,
                        nestedNavState = mainViewModel.nestedNavState,
                        navController = nestedNavController,
                    )
                }
            }
        }
    }

}


@Composable
fun HomePageContent(
    viewModel: RadioControllerViewModel,
    browser: MediaBrowser?,
    nestedNavState: MutableStateFlow<NestedNavState>,
    navController: NavHostController,
) {
    var controller by remember {
        mutableStateOf(viewModel.controller.value)
    }
    LaunchedEffect(key1 = viewModel.controller) {
        viewModel.controller.collect { c -> controller = c }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        controller?.let { controller ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                MyPlayerView(
                    viewModel = viewModel
                )
            }

            browser?.let { mediaBrowser ->
                Navigation(
                    browser = mediaBrowser,
                    controller = controller,
                    navController = navController,
                    nestedNavState = nestedNavState,
                    reloadEventFlow = viewModel.reloadEventFlow
                )
            }
        }
    }
}
