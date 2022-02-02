public enum COMMAND {
    ECHO("echo"),
    DATETIME("datetime"),
    APPEND("->>"),
    WRITE("->"),
    BACKGROUND("&");

    String value;

    COMMAND(String value) {
        this.value = value;
    }
}


