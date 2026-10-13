package app.crossfigure

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
        rows = listOf("TRAIT", "H#R#R", "RAISE", "E#S#A", "ELECT"),
        across = mapOf(1 to "Distinguishing quality", 4 to "Lift up", 5 to "Choose by vote"),
        down = mapOf(1 to "One more than two", 2 to "Come up", 3 to "Halloween goody"),
    ),
    Puzzle(
        rows = listOf("CLAIM", "H#N#O", "EAGER", "E#E#A", "RURAL"),
        across = mapOf(1 to "Assert as true", 4 to "Keen to start", 5 to "Of the countryside"),
        down = mapOf(1 to "Shout of support", 2 to "Hot temper", 3 to "Lesson of a fable"),
    ),
    Puzzle(
        rows = listOf("ACTOR", "B#H#E", "OPERA", "V#R#C", "EVENT"),
        across = mapOf(1 to "Stage performer", 4 to "Sung drama", 5 to "Happening"),
        down = mapOf(1 to "Overhead", 2 to "Not here", 3 to "Respond"),
    ),
    Puzzle(
        rows = listOf("MUSIC", "A#H#H", "GRACE", "I#K#C", "CLERK"),
        across = mapOf(1 to "Notes in a tune", 4 to "Elegant ease", 5 to "Store or office worker"),
        down = mapOf(1 to "Rabbit-from-a-hat act", 2 to "Tremble", 3 to "Restaurant bill"),
    ),
    Puzzle(
        rows = listOf("CHOSE", "R#N#N", "EXIST", "A#O#E", "MINOR"),
        across = mapOf(1 to "Picked", 4 to "Be alive", 5 to "Under age"),
        down = mapOf(1 to "Top of the milk", 2 to "Tear-inducing bulb", 3 to "Go in"),
    ),
    Puzzle(
        rows = listOf("STEAM", "T#X#A", "ENTRY", "E#R#B", "PLATE"),
        across = mapOf(1 to "Kettle vapor", 4 to "Way in", 5 to "Dinner dish"),
        down = mapOf(1 to "Sharply sloped", 2 to "More than needed", 3 to "Perhaps"),
    ),
    Puzzle(
        rows = listOf("SMART", "U#G#H", "PLACE", "E#I#M", "RANGE"),
        across = mapOf(1 to "Clever", 4 to "Location", 5 to "Extent or scope"),
        down = mapOf(1 to "Great", 2 to "Once more", 3 to "Central topic"),
    ),
    Puzzle(
        rows = listOf("CLASS", "H#S#P", "ELITE", "S#D#N", "TREND"),
        across = mapOf(1 to "School session", 4 to "The very best", 5 to "Current fad"),
        down = mapOf(1 to "Treasure box", 2 to "Off to the side", 3 to "Pay out"),
    ),
    Puzzle(
        rows = listOf("PASTA", "H#T#N", "ALONG", "S#V#R", "ENEMY"),
        across = mapOf(1 to "Spaghetti, e.g.", 4 to "Together with", 5 to "Foe"),
        down = mapOf(1 to "Stage of the moon", 2 to "Cooking appliance", 3 to "Fuming"),
    ),
    Puzzle(
        rows = listOf("TRACE", "I#V#M", "GROUP", "H#I#T", "TODAY"),
        across = mapOf(1 to "Tiny amount", 4 to "Collection of people", 5 to "This very day"),
        down = mapOf(1 to "Not loose", 2 to "Steer clear of", 3 to "Holding nothing"),
    ),
    Puzzle(
        rows = listOf("STAKE", "P#L#R", "LABOR", "I#U#O", "TIMER"),
        across = mapOf(1 to "Pointed post", 4 to "Hard work", 5 to "Kitchen countdown device"),
        down = mapOf(1 to "Divide", 2 to "Collection of songs", 3 to "Mistake"),
    ),
    Puzzle(
        rows = listOf("ARMOR", "L#E#I", "AWARD", "R#N#E", "MOTOR"),
        across = mapOf(1 to "Knight's protection", 4 to "Prize", 5 to "Engine"),
        down = mapOf(1 to "Wake-up sound", 2 to "Intended", 3 to "Horseback person"),
    ),
)
