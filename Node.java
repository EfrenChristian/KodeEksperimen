// Merepresentasikan satu simpul/lokasi dalam graf transportasi
public class Node {
    public enum Role { START, END, TRANSIT }

    private final String id;
    private final Role role;

    public Node(String id, String flag) {
        this.id = id;
        this.role = parseRole(flag);
    }

    // Konversi string flag dari CSV ke enum Role
    private static Role parseRole(String flag) {
        if (flag == null) return Role.TRANSIT;
        return switch (flag.trim().toLowerCase()) {
            case "start" -> Role.START;
            case "end"   -> Role.END;
            default      -> Role.TRANSIT;
        };
    }

    public String getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public boolean isStart() {
        return role == Role.START;
    }

    public boolean isEnd() {
        return role == Role.END;
    }

    public boolean isTransit() {
        return role == Role.TRANSIT;
    }

    @Override
    public String toString() {
        return id + " [" + role + "]";
    }
}
