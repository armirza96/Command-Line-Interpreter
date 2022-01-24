
public class User {
    private String name;
    private String host;
    private String[] paths;

    public User(String name, String host, String[] paths) {
        this.name = name;
        this.host = host;
        this.paths = paths;
    }

    public String getName() {
        return this.name;
    }

    public String getHost() {
        return this.host;
    }

    public String[] getPaths() {
        return this.paths;
    }

    public String getUserNameWithHost() {
        return this.name + "@" + this.host + "$"; // username@hostname$
    }
}
