package core.auth;

import java.util.concurrent.ConcurrentHashMap;

public class SessionStore {
    private static SessionStore instance = null;

    private static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    public static SessionStore getInstance() {
        if (instance == null) {
            synchronized (SessionStore.class) {
                instance = new SessionStore();
            }
        }
        return instance;
    }

    private SessionStore() {
    }

    public synchronized String get(String accessToken) {
        return map.get(accessToken);
    }

    public synchronized void set(String accessToken, String userId) {
        map.put(accessToken, userId);
    }

    public synchronized void remove(String accessToken) {
        map.remove(accessToken);
    }

    public synchronized boolean exists(String accessToken) {
        return map.containsKey(accessToken);
    }
}
