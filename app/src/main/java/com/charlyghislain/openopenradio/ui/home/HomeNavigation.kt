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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.google.common.util.concurrent.MoreExecutors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val ROUTE_MEDIA_ITEM = "mediaItem"
private const val ROOT_ROUTE = "root"

@Composable
fun Navigation(
    browser: MediaBrowser, controller: MediaController,
    onBackNavigationAvailable: (Boolean) -> Unit,
    onNavigateUp: (() -> Unit) -> Unit // Callback to handle navigation up
) {
    val navController = rememberNavController()
    var canNavigateBack by remember { mutableStateOf(false) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            canNavigateBack = entry.destination.route != ROOT_ROUTE
        }
    }
    LaunchedEffect(canNavigateBack) {
        onBackNavigationAvailable(canNavigateBack)
    }
    LaunchedEffect(onNavigateUp) {
        onNavigateUp({ navController.navigateUp() })
    }

    NavHost(navController = navController, startDestination = ROOT_ROUTE) {
        composable(ROOT_ROUTE) { MenuRootScreen(navController, browser, controller) }
        composable(
            "${ROUTE_MEDIA_ITEM}/{mediaItemId}",
            arguments = listOf(navArgument("mediaItemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaItemId = backStackEntry.arguments?.getString("mediaItemId")
            mediaItemId?.let {
                MenuScreen(navController, browser, controller, it)
            }
        }
    }
}

@Composable
fun MenuRootScreen(
    navController: NavController,
    browser: MediaBrowser,
    controller: MediaController
) {
    controller.prepare()
    browser.prepare()

    var rootItem: MediaItem? by remember { mutableStateOf(null) }
    val rootItemFuture = browser.getLibraryRoot(null)
    LaunchedEffect(rootItemFuture) {
        val rootItemResult = rootItemFuture.get();
        if (rootItemResult != null && rootItemResult.value != null) {
            rootItem = rootItemResult.value
        } else {
            rootItem = null
        }
    }

    rootItem?.let { item ->
        MenuScreen(navController, browser, controller, item.mediaId)
    }
}


@Composable
fun MenuScreen(
    navController: NavController,
    browser: MediaBrowser,
    controller: MediaController,
    itemId: String
) {
    val pager = remember {
        Pager<Int, MediaItem>(
            config = PagingConfig(pageSize = 50, initialLoadSize = 50),
            pagingSourceFactory = { MediaItemPagingSource(browser, itemId) }
        )
    }
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxWidth(1f)) {
        items(lazyPagingItems.itemCount) { index ->
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
    private val parentId: String
) : PagingSource<Int, MediaItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> {
        val pageToken = (params.key) ?: 0
        val pageOffset = Math.floor((pageToken / params.loadSize).toDouble()).toInt()

        val childrenFuture = browser.getChildren(
            parentId, pageOffset, params.loadSize,
            null
        )
        return try {
            val childrenResults = suspendCoroutine { continuation ->
                childrenFuture.addListener({
                    val children = childrenFuture.get()
                    continuation.resume(children)
                }, MoreExecutors.directExecutor())
            }
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