package android.annotation;

/** JVM-only annotation adapter; the APK uses Android's annotation. */
public @interface SuppressLint {
    String[] value();
}
