package com.charlyghislain.openopenradio

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import com.charlyghislain.openopenradio.service.radio.RadioService
import com.charlyghislain.openopenradio.service.radio.RadioService.IWebRadioService
import com.charlyghislain.openopenradio.service.radio.dao.RadioDatabase
import com.charlyghislain.openopenradio.service.radio.model.RadioGenre
import com.charlyghislain.openopenradio.ui.theme.OpenOpenRadioTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    lateinit var mRadioService: IWebRadioService;
    private var mBound: Boolean = false
    private val isServiceBound = mutableStateOf(false)

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as IWebRadioService
            mRadioService = binder;
            mBound = true
            isServiceBound.value = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
            isServiceBound.value = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            OpenOpenRadioTheme {
                val isBound by isServiceBound
                var radioService by remember { mutableStateOf<IWebRadioService?>(null) }

                LaunchedEffect(key1 = isBound) {
                    if (isBound) {
                        radioService = mRadioService;
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    radioService?.genres?.let { genres ->
                        GenreList(
                            genres = genres,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart();
        // Bind to LocalService
        Intent(this, RadioService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun GenreList(genres: LiveData<List<String>>, modifier: Modifier = Modifier) {
    val genreList by genres.observeAsState(initial = emptyList())
    LazyColumn(
        modifier = modifier
    ) {
        items(genreList) { genre ->
            Text(text = genre)
            HorizontalDivider()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OpenOpenRadioTheme {
        Greeting("Android")
    }
}