package master.infant.gpscamera.preview;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public final class CameraLogger {

    public final static int LEVEL_VERBOSE = 0;
    public final static int LEVEL_INFO    = 1;
    public final static int LEVEL_WARNING = 2;
    public final static int LEVEL_ERROR   = 3;

    static String sLastTag;
    static String sLastMessage;
    static int sLevel;
    static List<Logger> sLoggers;

    private String mTag;

    static {
        setLogLevel(LEVEL_ERROR);
        sLoggers = new ArrayList<>();

        Logger androidLogger = new Logger() {
            @Override
            public void log(int level, String tag, String msg, @Nullable Throwable throwable) {
                switch (level) {
                    case LEVEL_VERBOSE: Log.v(tag, msg, throwable); break;
                    case LEVEL_INFO: Log.i(tag, msg, throwable); break;
                    case LEVEL_WARNING: Log.w(tag, msg, throwable); break;
                    case LEVEL_ERROR: Log.e(tag, msg, throwable); break;
                }
            }
        };
        sLoggers.add(androidLogger);
    }

    @IntDef({LEVEL_VERBOSE, LEVEL_INFO, LEVEL_WARNING, LEVEL_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    @interface LogLevel {}

    interface Logger {
        void log(@LogLevel int level, String tag, String msg, @Nullable Throwable throwable);
    }

    static CameraLogger create(String tag) {
        return new CameraLogger(tag);
    }

    public static void setLogLevel(@LogLevel int logLevel) {
        sLevel = logLevel;
    }

    public static void addLogger(Logger logger) {
        sLoggers.add(logger);
    }

    public static void removeLogger(Logger logger) {
        sLoggers.remove(logger);
    }

    private CameraLogger(String tag) {
        mTag = tag;
    }

    private boolean canLog(@LogLevel int level) {
        return sLevel <= level && sLoggers.size() > 0;
    }

    private void log(@LogLevel int level, Object... data) {
        if (!canLog(level)) return;

        StringBuilder sb = new StringBuilder();
        Throwable throwable = null;
        for (Object dataItem : data) {
            sb.append(String.valueOf(dataItem)).append(" ");
        }

        String message = sb.toString();
        for (Logger logger : sLoggers) {
            logger.log(level, mTag, message, throwable);
        }

        sLastTag = mTag;
        sLastMessage = message;
    }

    void v(Object... data) {
        log(LEVEL_VERBOSE, data);
    }

    void i(Object... data) {
        log(LEVEL_INFO, data);
    }

    void w(Object... data) {
        log(LEVEL_WARNING, data);
    }

    void e(Object... data) {
        log(LEVEL_ERROR, data);
    }
}
