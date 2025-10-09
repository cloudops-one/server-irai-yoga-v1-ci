package yoga.irai.server.app.converter;

import org.junit.jupiter.api.Test;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class Base64ConverterTest {

    private final Base64Converter converter = new Base64Converter();

    @Test
    void testEncodeDecode() {
        String original = "Hello World!";
        String encoded = converter.convertToDatabaseColumn(original);
        assertEquals(Base64.getEncoder().encodeToString(original.getBytes()), encoded);

        String decoded = converter.convertToEntityAttribute(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void testEncodeDecode_Null() {
        String encoded = converter.convertToDatabaseColumn(null);
        assertNull(encoded);
        String decoded = converter.convertToEntityAttribute(null);
        assertNull(decoded);
    }
}