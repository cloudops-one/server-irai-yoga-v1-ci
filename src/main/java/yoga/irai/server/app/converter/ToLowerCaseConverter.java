package yoga.irai.server.app.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.commons.lang3.StringUtils;

public class ToLowerCaseConverter extends StdConverter<String, String> {

    @Override
    public String convert(String input) {
        if (!StringUtils.isEmpty(input)) {
            return input.trim().toLowerCase();
        } else {
            return null;
        }
    }
}
