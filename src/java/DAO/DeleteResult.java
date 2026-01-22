package DAO;

public class DeleteResult {
    public final boolean ok;
    public final String message;

    private DeleteResult(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public static DeleteResult ok(String message) {
        return new DeleteResult(true, message);
    }

    public static DeleteResult fail(String message) {
        return new DeleteResult(false, message);
    }
}

