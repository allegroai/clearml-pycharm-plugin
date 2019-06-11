class OsUtil {

    private OsUtil() {

    }

    final static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    final static boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac os");
}
