package com.charlyghislain.openopenradio.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.charlyghislain.openopenradio.R
import com.charlyghislain.openopenradio.ui.components.awaitResult
import com.charlyghislain.openopenradio.ui.model.NestedNavState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val ROUTE_MEDIA_ITEM = "mediaItem"

@Composable
fun Navigation(
    browser: MediaBrowser,
    controller: MediaController,
    navController: NavHostController,
    nestedNavState: MutableStateFlow<NestedNavState>,
    reloadEventFlow: MutableSharedFlow<ReloadEvent>
) {
    val rootRoute = stringResource(R.string.nav_home_root)
    val appname = stringResource(id = R.string.app_name)
    var browserConnected: Boolean by remember {
        mutableStateOf(browser.isConnected)
    }

    val scope = rememberCoroutineScope()
    DisposableEffect(navController, browser) {
        browserConnected = browser.isConnected

        val listener = NavController.OnDestinationChangedListener { _, destination, arguments ->
            val isBackStackEmpty = navController.previousBackStackEntry == null
            nestedNavState.value = NestedNavState(
                isBackStackEmpty = isBackStackEmpty,
                currentTitle = appname
            )
            val mediaId = arguments?.getString("mediaItemId")
            if (destination.route.toString().startsWith(ROUTE_MEDIA_ITEM) && mediaId != null) {
                scope.launch {
                    val title = try {
                        browser.getItem(mediaId).awaitResult().value?.mediaMetadata?.title?.toString()
                    } catch (e: Exception) {
                        null
                    }
                    if (title != null && navController.currentBackStackEntry?.arguments?.getString("mediaItemId") == mediaId) {
                        nestedNavState.value = NestedNavState(isBackStackEmpty, title)
                    }
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    if (browserConnected) {
        NavHost(
            navController = navController,
            startDestination = rootRoute
        ) {
            composable(route = rootRoute) {
                MenuRootScreen(
                    navController,
                    browser,
                    controller,
                    reloadEventFlow
                )
            }
            composable(
                "${ROUTE_MEDIA_ITEM}/{mediaItemId}",
                arguments = listOf(navArgument("mediaItemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val mediaItemId = backStackEntry.arguments?.getString("mediaItemId")
                mediaItemId?.let {
                    MenuScreen(navController, browser, controller, it, reloadEventFlow)
                }
            }
        }
    }
}

@Composable
fun MenuRootScreen(
    navController: NavController,
    browser: MediaBrowser,
    controller: MediaController,
    reloadEventFlow: MutableSharedFlow<ReloadEvent>
) {
    var rootItem: MediaItem? by remember { mutableStateOf(null) }
    LaunchedEffect(browser, controller) {
        controller.prepare()
        rootItem = try {
            browser.getLibraryRoot(null).awaitResult().value
        } catch (e: Exception) {
            null
        }
    }

    rootItem?.let { item ->
        MenuScreen(
            navController, browser, controller, item.mediaId,
            reloadEventFlow = reloadEventFlow
        )
    }
}


@Composable
fun MenuScreen(
    navController: NavController,
    browser: MediaBrowser,
    controller: MediaController,
    itemId: String,
    reloadEventFlow: MutableSharedFlow<ReloadEvent>
) {
    val pager = remember {
        Pager<Int, MediaItem>(
            config = PagingConfig(pageSize = 50, initialLoadSize = 50),
            pagingSourceFactory = { MediaItemPagingSource(browser, itemId, reloadEventFlow) },
        )
    }
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()


    LazyColumn(modifier = Modifier.fillMaxWidth(1f)) {
        items(
            count = lazyPagingItems.itemCount,
            key = { index -> lazyPagingItems[index]?.mediaId.toString() },
        ) { index ->
            val item = lazyPagingItems[index]
            item?.let {
                ListMediaItem(item, navController, controller)
            }
        }
    }
}

@Composable
private fun ListMediaItem(
    item: MediaItem,
    navController: NavController,
    controller: MediaController
) {
    MenuItem(
        label = item.mediaMetadata.title.toString(),
        icon = Icons.Filled.Folder,
        iconUri = item.mediaMetadata.artworkUri?.toString(),
        onClick = {
            if (item.mediaMetadata.isBrowsable == true) {
                navController.navigate("${ROUTE_MEDIA_ITEM}/${item.mediaId}")
            } else if (item.mediaMetadata.isPlayable == true) {
                controller.setMediaItem(item)
                controller.play()
            }
        }
    )
}

@Composable
fun MenuItem(
    label: String,
    icon: ImageVector?,
    iconUri: String?,
    onClick: () -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DynamicIcon(
            imageUri = iconUri,
            icon = icon,
            label = label,
            modifier = Modifier.size(24.dp) // Adjust icon size as needed
        )
        Spacer(modifier = Modifier.width(8.dp)) // Add spacing between icon and text
        Text(text = label)
    }
}

@Composable
fun DynamicIcon(
    imageUri: String?, // URI of the image to load, null if using a vector icon
    icon: ImageVector? = null, // Vector icon to use, null if loading from URI
    label: String,
    modifier: Modifier = Modifier
) {
    if (imageUri != null) {
        // Load image from URI using Coil
        AsyncImage(
            model = imageUri,
            contentDescription = label,
            modifier = modifier
        )
    } else if (icon != null) {
        // Use vector icon
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = modifier
        )
    }
}

class MediaItemPagingSource(
    private val browser: MediaBrowser,
    private val parentId: String,
    reloadEventFlow: MutableSharedFlow<ReloadEvent>,
) : PagingSource<Int, MediaItem>() {
    init {
        // Launch a coroutine to collect from the flow
        if (parentId == "[favorites]") {
            CoroutineScope(Dispatchers.Main).launch { // Use Dispatchers.Main for UI thread safety
                reloadEventFlow.first() // Collect only the firstevent
                invalidate()
            }
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> {
        val pageToken = (params.key) ?: 0
        val pageOffset = Math.floor((pageToken / params.loadSize).toDouble()).toInt()

        return try {
            val childrenResults = browser.getChildren(
                parentId, pageOffset, params.loadSize,
                null
            ).awaitResult()
            val items = childrenResults?.value ?: emptyList()
            var nextKey: Int? = pageToken + params.loadSize
            var prevKey: Int? = pageToken - params.loadSize
            if (items.size < params.loadSize) {
                nextKey = null
            }
            if (pageToken == 0) {
                prevKey = null
            }
            LoadResult.Page(
                data = items,
                prevKey = prevKey, // No previous page in this case
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MediaItem>): Int? {
        return null
    }
}

data class ReloadEvent(val parentId: String)
