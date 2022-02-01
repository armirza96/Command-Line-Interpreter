public enum COMMAND_LOW_LEVEL {

    ECHO("echo"), 
    DATETIME("datetime"),
    EXECUTABLE("executable");
    String value;

    COMMAND_LOW_LEVEL(String value) {
        this.value = value;
    }
}
