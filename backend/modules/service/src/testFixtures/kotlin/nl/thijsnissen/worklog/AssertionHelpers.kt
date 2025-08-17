package nl.thijsnissen.worklog

import org.junit.jupiter.api.Assertions.assertEquals

fun <T : Collection<*>> assertSameElements(expected: T, actual: T) {
    assertEquals(
        expected.groupingBy { it }.eachCount().toSortedMap(compareBy { it.toString() }),
        actual.groupingBy { it }.eachCount().toSortedMap(compareBy { it.toString() }),
    ) {
        "Expected <$expected>, \nactual <$actual>."
    }
}

fun <T : Collection<*>> assertContainsElements(expected: T, actual: T) {
    val expected = expected.groupingBy { it }.eachCount()
    val actual = actual.groupingBy { it }.eachCount().filterKeys { it in expected }

    assertEquals(
        expected.toSortedMap(compareBy { it.toString() }),
        actual.toSortedMap(compareBy { it.toString() }),
    ) {
        "Expected <$expected> to be contained in, \nactual <$actual>."
    }
}
