public enum COMMAND_HIGH_LEVEL {
    APPEND("->>"),
    OVERWRITE("->"),
    CONSOLE("console");
    
    String value;

    COMMAND_HIGH_LEVEL(String value) {
        this.value = value;
    }
}