package au.com.invitro.take5.data

object Risk {
    val likelihoods = listOf("Rare", "Unlikely", "Possible", "Likely", "Almost certain")
    val consequences = listOf("Insignificant", "Minor", "Moderate", "Major", "Catastrophic")

    enum class Level(val label: String, val argb: Long) {
        LOW("Low", 0xFF738D00),
        MEDIUM("Medium", 0xFFF89406),
        HIGH("High", 0xFFE0561B),
        EXTREME("Extreme", 0xFFC10841),
    }

    fun level(likelihood: Int, consequence: Int): Level? {
        if (likelihood !in 1..5 || consequence !in 1..5) return null
        val score = likelihood * consequence
        return when {
            score <= 4 -> Level.LOW
            score <= 9 -> Level.MEDIUM
            score <= 16 -> Level.HIGH
            else -> Level.EXTREME
        }
    }

    fun requiresSupervisor(level: Level?) = level == Level.HIGH || level == Level.EXTREME

    fun describe(likelihood: Int, consequence: Int): String {
        val l = likelihoods.getOrNull(likelihood - 1) ?: "—"
        val c = consequences.getOrNull(consequence - 1) ?: "—"
        return "Likelihood: $l  ·  Consequence: $c"
    }
}
