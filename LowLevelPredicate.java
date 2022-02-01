import java.util.function.Predicate;

public class LowLevelPredicate<input> implements Predicate<String> {

    @Override
    public boolean test(String input) {
        return testForEcho(input) || testForDateTime(input);
    }
    
    public boolean testForEcho(String input) {
        return input.contains(COMMAND_LOW_LEVEL.values()[0].value.toString().toLowerCase());
    }

    public boolean testForDateTime(String input) {
        return input.contains(COMMAND_LOW_LEVEL.values()[1].value.toString().toLowerCase());
    }

    public boolean testForExecutable(String input) {
        return input.contains("./");
    }
}
