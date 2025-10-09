package yoga.irai.server.app.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToLowerCaseConverterTest {

    private final ToLowerCaseConverter converter = new ToLowerCaseConverter();

    @Test
    void testConvertToLowerCase() {
        assertEquals("abc", converter.convert("ABC"));
        assertEquals("test", converter.convert("TeSt"));
        assertNull(converter.convert(null));
    }
}