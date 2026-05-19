package com.careloop.security;

import com.careloop.model.Role;

/**
 * Holds the current logged-in user info (set per request by the filter).
 */
public class UserContext {

    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<Role> role = new ThreadLocal<>();

    public static void set(Long id, Role r) {
        userId.set(id);
        role.set(r);
    }

    public static Long getUserId() {
        return userId.get();
    }

    public static Role getRole() {
        return role.get();
    }

    public static void clear() {
        userId.remove();
        role.remove();
    }
}
