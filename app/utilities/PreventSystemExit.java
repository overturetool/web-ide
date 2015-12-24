package utilities;

import java.security.Permission;

public class PreventSystemExit {
    private static final String exitVM0 = "exitVM.0";
    private static final String exitVM3 = "exitVM.3";

    public static class ExitTrappedException extends SecurityException { }

    public static void disableSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission( Permission permission ) {
                if(exitVM0.equals(permission.getName()) || exitVM3.equals(permission.getName())) {
                    throw new ExitTrappedException();
                }
            }
        };

        System.setSecurityManager(securityManager);
    }

    public static void enableSystemExitCall() {
        System.setSecurityManager(null);
    }
}
