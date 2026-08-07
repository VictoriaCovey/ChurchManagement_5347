package covey.prayer.security;

/**
 * Holds the caller's bearer token for the lifetime of the current request thread, so it can be
 * re-attached to outbound calls this service makes to other services (see
 * {@link BearerTokenPropagationInterceptor}).
 */
public final class BearerTokenHolder {

    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    private BearerTokenHolder() {
    }

    public static void set(String bearerHeaderValue) {
        TOKEN.set(bearerHeaderValue);
    }

    public static String get() {
        return TOKEN.get();
    }

    public static void clear() {
        TOKEN.remove();
    }
}
