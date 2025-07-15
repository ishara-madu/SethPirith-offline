import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.pixeleye.sethpirith.data.PirithDataProvider.pirithList
import com.pixeleye.sethpirith.ui.util.PirithPrefs

@Composable
fun PlayingTitle():String {
    val context = LocalContext.current
    val playingId by PirithPrefs.playingIdFlow(context).collectAsState(initial = PirithPrefs.getLastAudioId(context))
    val playingTitle = stringResource(id = pirithList[playingId].titleResId)
    return playingTitle
}