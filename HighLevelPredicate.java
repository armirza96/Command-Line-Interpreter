import java.util.function.Predicate;

public class HighLevelPredicate<input> implements Predicate<String> {

    @Override
    public boolean test(String input) {
        return testForAppendingToFile(input) || testForOverwriting(input);
    }
    
    public boolean testForAppendingToFile(String input) {
        return input.contains(COMMAND_HIGH_LEVEL.values()[0].value.toString().toLowerCase());
    }

    public boolean testForOverwriting(String input) {
        return input.contains(COMMAND_HIGH_LEVEL.values()[1].value.toString().toLowerCase());
    }
}
