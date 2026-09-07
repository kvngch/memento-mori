package fr.kvngch.memento

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class LifeTest {

    private val birth = LocalDate.of(1990, 6, 15)

    @Test
    fun `compte les semaines revolues et le total`() {
        val l = life(birth, birth.plusWeeks(100), 80, Scale.WEEKS)
        assertEquals(100, l.lived)
        assertEquals(4174, l.total)
        assertEquals(4074, l.remaining)
    }

    @Test
    fun `l age de fin change le total`() {
        assertEquals(4696, life(birth, birth, 90, Scale.WEEKS).total)
        assertEquals(3130, life(birth, birth, 60, Scale.WEEKS).total)
    }

    @Test
    fun `une semaine entamee ne compte pas`() {
        assertEquals(3, life(birth, birth.plusDays(27), 80, Scale.WEEKS).lived)
    }

    @Test
    fun `borne au jour de la naissance et a la fin`() {
        assertEquals(0, life(birth, birth.minusYears(1), 80, Scale.WEEKS).lived)
        val fin = life(birth, birth.plusYears(120), 80, Scale.WEEKS)
        assertEquals(fin.total, fin.lived)
        assertEquals(0, fin.remaining)
    }

    @Test
    fun `chaque unite a son propre total`() {
        assertEquals(29220, life(birth, birth, 80, Scale.DAYS).total)
        assertEquals(960, life(birth, birth, 80, Scale.MONTHS).total)
        assertEquals(31, life(birth, birth.plusDays(31), 80, Scale.DAYS).lived)
        assertEquals(1, life(birth, birth.plusDays(31), 80, Scale.MONTHS).lived)
    }

    @Test
    fun `la part vecue suit les semaines`() {
        assertEquals(0.5f, life(birth, birth.plusWeeks(2087), 80, Scale.WEEKS).ratio, 0.001f)
    }
}
