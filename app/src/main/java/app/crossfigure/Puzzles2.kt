package app.crossfigure

// Builds a puzzle from six words: three across (rows 1, 3, 5) and three down
// (columns 1, 3, 5). Clues are given in order: across 1, 4, 5, then down 1, 2, 3.
private fun xp(
    a0: String, a2: String, a4: String,
    d0: String, d2: String, d4: String,
    clues: String,
): Puzzle {
    val c = clues.split("|")
    fun mid(i: Int): String =
        d0[i].uppercaseChar().toString() + "#" + d2[i].uppercaseChar() + "#" + d4[i].uppercaseChar()
    return Puzzle(
        rows = listOf(a0.uppercase(), mid(1), a2.uppercase(), mid(3), a4.uppercase()),
        across = mapOf(1 to c[0], 4 to c[1], 5 to c[2]),
        down = mapOf(1 to c[3], 2 to c[4], 3 to c[5]),
    )
}

val EXTRA_PUZZLES: List<Puzzle> = listOf(
    xp("royal", "fluid", "eagle", "rifle", "young", "lodge",
        "Kingly|Flowing substance|Bald national bird|Rummage through|Not old|Cabin in the mountains"),
    xp("woman", "input", "every", "write", "maple", "nutty",
        "Adult female|Data going in|Each and all|Put pen to paper|Syrup tree|Like an almond"),
    xp("climb", "mango", "large", "camel", "inner", "broke",
        "Go up a mountain|Tropical fruit|Big|Desert animal with humps|On the inside|Out of money"),
    xp("swift", "exact", "piece", "sheep", "image", "title",
        "Fast|Precisely correct|Part of a puzzle|Wool producer|Picture|Name of a book"),
    xp("plain", "later", "earth", "pulse", "after", "north",
        "Not fancy|Not now|Our planet|Heartbeat|Later than|Compass direction up"),
    xp("flute", "otter", "ready", "flour", "ultra", "early",
        "Woodwind instrument|Playful river swimmer|Prepared|Ground wheat|Beyond the usual|Ahead of time"),
    xp("tulip", "nacho", "rhyme", "tuner", "lucky", "phone",
        "Spring bulb flower|Chip with cheese|Poem sound match|Piano adjuster|Fortunate|Device for calls"),
    xp("urban", "prior", "ridge", "upper", "blind", "nerve",
        "Of the city|Earlier|Mountain crest|Higher|Window cover|Courage"),
    xp("floor", "cheap", "sunny", "focus", "ocean", "reply",
        "What you walk on indoors|Low in price|Bright with sunshine|Concentrate|Vast salt water|Answer"),
    xp("thorn", "outer", "hence", "tooth", "often", "nurse",
        "Rose prickle|On the outside|From here on|Dentist's concern|Frequently|Hospital caregiver"),
    xp("pilot", "never", "allow", "panda", "level", "threw",
        "Airplane flyer|Not ever|Let happen|Bamboo eater|Even or flat|Tossed"),
    xp("dough", "asset", "total", "draft", "upset", "hotel",
        "Bread before baking|Valuable thing to own|Sum|First version|Tip over|Place to stay overnight"),
    xp("stuff", "order", "hurry", "sloth", "under", "forty",
        "Cram|Command|Rush|Slow tree dweller|Below|Four tens"),
    xp("trial", "wheel", "relic", "tower", "ideal", "lilac",
        "Court proceeding|Round roller|Ancient artifact|Tall structure|Perfect|Purple flowering shrub"),
    xp("coach", "union", "bunny", "crumb", "alien", "honey",
        "Team trainer|Coming together|Easter animal|Bit of bread|Visitor from another planet|Bee product"),
    xp("glass", "olive", "yield", "glory", "alike", "speed",
        "Window material|Pizza topping|Give way|Great honor|Similar|How fast"),
    xp("solid", "river", "party", "strip", "lover", "dirty",
        "Not liquid or gas|Flowing waterway|Birthday gathering|Long narrow piece|Sweetheart|Not clean"),
    xp("watch", "drain", "water", "widow", "toast", "honor",
        "Wrist timepiece|Sink outlet|H2O|Surviving spouse|Bread from the toaster|Great respect"),
    xp("orbit", "swing", "sugar", "oasis", "bring", "tiger",
        "Path around a planet|Playground seat|Sweet crystals|Desert water spot|Carry here|Striped big cat"),
    xp("catch", "realm", "other", "cargo", "teach", "humor",
        "Grab a ball|Kingdom|Not this one|Ship's freight|Instruct|Comedy"),
    xp("marsh", "cider", "alone", "mocha", "radio", "horse",
        "Wet lowland|Apple drink|By oneself|Chocolate coffee|Car music source|Stable animal"),
    xp("towel", "unity", "baker", "thumb", "whisk", "layer",
        "Dries you off|Oneness|Bread maker|Short digit|Kitchen beater|One level in a cake"),
    xp("label", "grass", "clear", "logic", "blade", "laser",
        "Tag on clothing|Lawn cover|Easy to see through|Reasoning|Knife edge|Focused beam of light"),
    xp("press", "usual", "delay", "pound", "equal", "silly",
        "Push down|Normal|Hold up|Sixteen ounces|The same in value|Goofy"),
)

val ALL_PUZZLES: List<Puzzle> = PUZZLES + EXTRA_PUZZLES
