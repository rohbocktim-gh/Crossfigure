package app.crossfigure

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {
    private var adsReady by mutableStateOf(false)
    private val adsStarted = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFAFAFA)) {
                    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                        Box(modifier = Modifier.weight(1f)) {
                            GameHost()
                        }
                        if (adsReady) {
                            BannerAd(BuildConfig.ADMOB_BANNER_ID)
                        }
                    }
                }
            }
        }
        setUpAds()
    }

    private fun setUpAds() {
        val consentInfo = UserMessagingPlatform.getConsentInformation(this)
        val params = ConsentRequestParameters.Builder().build()
        consentInfo.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) {
                    if (consentInfo.canRequestAds()) startAds()
                }
            },
            {
                if (consentInfo.canRequestAds()) startAds()
            },
        )
        if (consentInfo.canRequestAds()) startAds()
    }

    private fun startAds() {
        if (!adsStarted.compareAndSet(false, true)) return
        MobileAds.initialize(this) {
            runOnUiThread { adsReady = true }
        }
    }
}

@Composable
fun BannerAd(unitId: String) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}

@Composable
fun GameHost() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("crossfigure", Context.MODE_PRIVATE) }
    var index by remember {
        mutableIntStateOf(prefs.getInt("puzzle", 0).coerceIn(0, PUZZLES.size - 1))
    }
    key(index) {
        GameScreen(
            puzzle = PUZZLES[index],
            number = index + 1,
            total = PUZZLES.size,
            onPrev = {
                if (index > 0) {
                    index--
                    prefs.edit().putInt("puzzle", index).apply()
                }
            },
            onNext = {
                if (index < PUZZLES.size - 1) {
                    index++
                    prefs.edit().putInt("puzzle", index).apply()
                }
            },
        )
    }
}

@Composable
fun GameScreen(
    puzzle: Puzzle,
    number: Int,
    total: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val n = puzzle.size
    val entries = remember {
        mutableStateListOf<Char>().apply { repeat(n * n) { add(' ') } }
    }
    var selR by remember { mutableIntStateOf(0) }
    var selC by remember { mutableIntStateOf(0) }
    var acrossDir by remember { mutableStateOf(true) }
    var showChecks by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    fun checkMessage(): String {
        var wrong = 0
        var empty = 0
        for (r in 0 until n) {
            for (c in 0 until n) {
                if (puzzle.isBlock(r, c)) continue
                val e = entries[r * n + c]
                if (e == ' ') {
                    empty++
                } else if (e != puzzle.solution(r, c)) {
                    wrong++
                }
            }
        }
        return when {
            wrong == 0 && empty == 0 -> "Solved! Nice work. Tap Next for another puzzle."
            wrong > 0 -> "Red letters are wrong."
            else -> "No mistakes so far, keep going."
        }
    }

    fun selectCell(r: Int, c: Int) {
        if (puzzle.isBlock(r, c)) return
        if (r == selR && c == selC) {
            if (puzzle.hasWord(r, c, !acrossDir)) acrossDir = !acrossDir
        } else {
            selR = r
            selC = c
            if (!puzzle.hasWord(r, c, acrossDir)) acrossDir = !acrossDir
        }
        message = ""
    }

    fun typeLetter(ch: Char) {
        entries[selR * n + selC] = ch
        showChecks = false
        message = ""
        val cells = puzzle.cellsOfWord(selR, selC, acrossDir)
        val i = cells.indexOf(selR to selC)
        if (i >= 0 && i < cells.size - 1) {
            selR = cells[i + 1].first
            selC = cells[i + 1].second
        }
    }

    fun backspace() {
        showChecks = false
        message = ""
        val idx = selR * n + selC
        if (entries[idx] != ' ') {
            entries[idx] = ' '
        } else {
            val cells = puzzle.cellsOfWord(selR, selC, acrossDir)
            val i = cells.indexOf(selR to selC)
            if (i > 0) {
                selR = cells[i - 1].first
                selC = cells[i - 1].second
                entries[selR * n + selC] = ' '
            }
        }
    }

    val activeCells = puzzle.cellsOfWord(selR, selC, acrossDir)
    val ink = Color(0xFF111111)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Crossfigure", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ink)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onPrev, enabled = number > 1) { Text("Prev") }
            Text("Puzzle " + number + " of " + total, fontSize = 16.sp, color = ink)
            Button(onClick = onNext, enabled = number < total) { Text("Next") }
        }
        Spacer(Modifier.height(12.dp))

        for (r in 0 until n) {
            Row {
                for (c in 0 until n) {
                    val block = puzzle.isBlock(r, c)
                    val letter = entries[r * n + c]
                    Cell(
                        letter = letter,
                        number = puzzle.numbers[r to c],
                        block = block,
                        selected = r == selR && c == selC,
                        inWord = activeCells.contains(r to c),
                        wrong = showChecks && !block && letter != ' ' && letter != puzzle.solution(r, c),
                        onClick = { selectCell(r, c) },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            puzzle.clueFor(selR, selC, acrossDir),
            fontSize = 18.sp,
            color = ink,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
        if (message.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(message, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ink)
        }

        Spacer(Modifier.height(12.dp))
        val keyRows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
        keyRows.forEachIndexed { index, keys ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                keys.forEach { k ->
                    Key(label = k.toString(), weight = 1f, onClick = { typeLetter(k) })
                }
                if (index == 2) {
                    Key(label = "DEL", weight = 1.6f, onClick = { backspace() })
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                showChecks = true
                message = checkMessage()
            }) { Text("Check") }
            Button(onClick = {
                for (r in 0 until n) {
                    for (c in 0 until n) {
                        if (!puzzle.isBlock(r, c)) entries[r * n + c] = puzzle.solution(r, c)
                    }
                }
                showChecks = false
                message = ""
            }) { Text("Reveal") }
            Button(onClick = {
                for (i in 0 until n * n) entries[i] = ' '
                showChecks = false
                message = ""
            }) { Text("Clear") }
        }

        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Across", fontWeight = FontWeight.Bold, color = ink)
            puzzle.across.forEach { (num, clue) ->
                Text(num.toString() + ". " + clue, color = ink)
            }
            Spacer(Modifier.height(8.dp))
            Text("Down", fontWeight = FontWeight.Bold, color = ink)
            puzzle.down.forEach { (num, clue) ->
                Text(num.toString() + ". " + clue, color = ink)
            }
        }
    }
}

@Composable
fun Cell(
    letter: Char,
    number: Int?,
    block: Boolean,
    selected: Boolean,
    inWord: Boolean,
    wrong: Boolean,
    onClick: () -> Unit,
) {
    val bg = when {
        block -> Color(0xFF1B1B1F)
        selected -> Color(0xFFFFE08A)
        inWord -> Color(0xFFD6E4FF)
        else -> Color.White
    }
    val base = Modifier
        .size(56.dp)
        .background(bg)
        .border(BorderStroke(1.dp, Color(0xFF444444)))
    Box(modifier = if (block) base else base.clickable(onClick = onClick)) {
        if (!block) {
            if (number != null) {
                Text(
                    number.toString(),
                    fontSize = 10.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 3.dp, top = 1.dp),
                )
            }
            if (letter != ' ') {
                Text(
                    letter.toString(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (wrong) Color(0xFFC62828) else Color(0xFF111111),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
fun RowScope.Key(label: String, weight: Float, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(2.dp)
            .height(46.dp)
            .background(Color(0xFFE3E3E8), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = if (label.length > 1) 12.sp else 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111111),
        )
    }
}
