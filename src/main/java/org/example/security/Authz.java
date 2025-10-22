package org.example.security;

import com.auth0.jwt.interfaces.DecodedJWT;

public class Authz {
    public static void requireRoleAtLeast(DecodedJWT jwt, String required) {
        String role = jwt.getClaim("role").asString();
        int level = levelOf(role);
        int need  = levelOf(required);
        if (level < need) throw new SecurityException("Permiso insuficiente. Requiere rol " + required);
    }
    private static int levelOf(String r){
        if ("ADMIN".equals(r)) return 3;
        if ("MOD".equals(r))   return 2;
        return 1; // USER o null
    }
}



/* uso: var jwt = JwtUtils.verify(token);
Authz.requireRoleAtLeast(jwt, "ADMIN");
*/