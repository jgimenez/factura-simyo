package cat.mobilejazz.utilities.system;

/** Helps detect the runtime that is running the application. 
 */
public class AndroidFlavors {

    // http://stackoverflow.com/questions/8309624/detect-app-is-running-on-kindle-fire
    public static boolean isKindleFire() {
        return android.os.Build.MANUFACTURER.equals("Amazon")
        && (android.os.Build.MODEL.equals("Kindle Fire")
            || android.os.Build.MODEL.startsWith("KF"));
    }
    
    // http://supportforums.blackberry.com/t5/Android-Runtime-Development/How-to-detect-if-the-Android-app-is-the-Blackberry-repackaged/td-p/2380029
    public static boolean isBlackBerry() {
        return java.lang.System.getProperty("os.name").equals("qnx");
    }

}
