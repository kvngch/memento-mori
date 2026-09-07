package fr.kvngch.memento

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class LifeTest {

    private val birth = LocalDate.of(1990, 6, 15)

    @Test
    fun `compte les semaines revolues et le total`() {
        val l = life(birth, birth.plusWeeks(100))
        assertEquals(100, l.lived)
        assertEquals(4174, l.total)
        assertEquals(4074, l.remaining)
    }

    @Test
    fun `une semaine entamee ne compte pas`() {
        assertEquals(3, life(birth, birth.plusDays(27)).lived)
    }

    @Test
    fun `borne au jour de la naissance et a la fin`() {
        assertEquals(0, life(birth, birth.minusYears(1)).lived)
        val fin = life(birth, birth.plusYears(120))
        assertEquals(fin.total, fin.lived)
        assertEquals(0, fin.remaining)
    }

    @Test
    fun `la part vecue suit les semaines`() {
        assertEquals(0.5f, life(birth, birth.plusWeeks(2087)).ratio, 0.001f)
    }
}
