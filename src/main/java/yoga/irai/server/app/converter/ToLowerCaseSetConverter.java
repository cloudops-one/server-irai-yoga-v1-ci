package yoga.irai.server.app.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.Set;
import java.util.TreeSet;

public class ToLowerCaseSetConverter extends StdConverter<Set<String>, Set<String>> {

    @Override
    public Set<String> convert(Set<String> input) {
        Set<String> output = new TreeSet<>();
        for (String string : input) {
            output.add(string.trim().toLowerCase());
        }
        return output;
    }
}
