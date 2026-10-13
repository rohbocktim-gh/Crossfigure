package app.crossfigure

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFAFAFA)) {
                    CrossfigureScreen()
                }
            }
        }
    }
}

class Puzzle(
    val rows: List<String>,
    val across: Map<Int, String>,
    val down: Map<Int, String>,
) {
    val size: Int = rows.size
    val numbers: Map<Pair<Int, Int>, Int> = buildNumbers()

    fun isBlock(r: Int, c: Int): Boolean = rows[r][c] == '#'

    fun solution(r: Int, c: Int): Char = rows[r][c]

    private fun isOpen(r: Int, c: Int): Boolean =
        r in 0 until size && c in 0 until size && rows[r][c] != '#'

    private fun startsAcross(r: Int, c: Int): Boolean =
        isOpen(r, c) && !isOpen(r, c - 1) && isOpen(r, c + 1)

    private fun startsDown(r: Int, c: Int): Boolean =
        isOpen(r, c) && !isOpen(r - 1, c) && isOpen(r + 1, c)

    private fun buildNumbers(): Map<Pair<Int, Int>, Int> {
        val result = mutableMapOf<Pair<Int, Int>, Int>()
        var next = 1
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (startsAcross(r, c) || startsDown(r, c)) {
                    result[r to c] = next
                    next++
                }
            }
        }
        return result
    }

    fun wordStart(r: Int, c: Int, acrossDir: Boolean): Pair<Int, Int> {
        var rr = r
        var cc = c
        if (acrossDir) {
            while (isOpen(rr, cc - 1)) cc--
        } else {
            while (isOpen(rr - 1, cc)) rr--
        }
        return rr to cc
    }

    fun cellsOfWord(r: Int, c: Int, acrossDir: Boolean): List<Pair<Int, Int>> {
        val start = wordStart(r, c, acrossDir)
        val out = mutableListOf<Pair<Int, Int>>()
        var rr = start.first
        var cc = start.second
        while (isOpen(rr, cc)) {
            out.add(rr to cc)
            if (acrossDir) cc++ else rr++
        }
        return out
    }

    fun hasWord(r: Int, c: Int, acrossDir: Boolean): Boolean =
        cellsOfWord(r, c, acrossDir).size > 1

    fun clueFor(r: Int, c: Int, acrossDir: Boolean): String {
        val start = wordStart(r, c, acrossDir)
        val num = numbers[start] ?: return ""
        val text = (if (acrossDir) across else down)[num] ?: ""
        return num.toString() + (if (acrossDir) "A" else "D") + ": " + text
    }
}

val PUZZLES = listOf(
    Puzzle(
        rows = listOf("STARE", "H#R#V", "OLIVE", "R#S#N", "ELECT"),
        across = mapOf(
            1 to "Look intently",
            4 to "Martini garnish",
            5 to "Choose by ballot",
        ),
        down = mapOf(
            1 to "Where waves meet land",
            2 to "Get out of bed",
            3 to "Race or ceremony",
        ),
    ),
)

@Composable
fun CrossfigureScreen() {
    val puzzle = remember { PUZZLES[0] }
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
            wrong == 0 && empty == 0 -> "Solved! Nice work."
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
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Crossfigure", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ink)
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
