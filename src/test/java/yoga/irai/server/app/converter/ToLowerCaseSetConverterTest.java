package yoga.irai.server.app.converter;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ToLowerCaseSetConverterTest {

    private final ToLowerCaseSetConverter converter = new ToLowerCaseSetConverter();

    @Test
    void testConvertSetToLowerCase() {
        Set<String> input = Set.of("ONE", "Two", "three");
        Set<String> result = converter.convert(input);
        assertTrue(result.contains("one"));
        assertTrue(result.contains("two"));
        assertTrue(result.contains("three"));
        assertEquals(3, result.size());
    }
}