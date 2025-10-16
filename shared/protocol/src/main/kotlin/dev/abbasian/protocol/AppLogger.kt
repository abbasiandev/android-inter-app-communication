package dev.abbasian.protocol

/**
 * Basic logging interface that works in both apps.
 * Keeps our logging the same whether we are in Location App or Internet App.
 */
interface AppLogger {
    /**
     * Logs a debug-level message
     * @param tag Usually the class name where you're logging from
     * @param message What you want to log
     */
    fun d(
        tag: String,
        message: String,
    )

    /**
     * Logs an informational message
     * @param tag Identifies where the log is coming from
     * @param message What you want to log
     */
    fun i(
        tag: String,
        message: String,
    )

    /**
     * Logs a warning - something not quite right but not critical
     * @param tag Where this warning originated
     * @param message Description of the warning
     * @param throwable Any exception that might be related
     */
    fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    /**
     * Logs an error - something actually went wrong
     * @param tag Where this error happened
     * @param message What went wrong
     * @param throwable The exception if there is one
     */
    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )
}

/**
 * Our actual logger that uses Timber under the hood.
 * Remember to call Timber.plant() in your Application.onCreate() or this won't work.
 */
class TimberLogger : AppLogger {
    override fun d(
        tag: String,
        message: String,
    ) {
        timber.log.Timber.tag(tag).d(message)
    }

    override fun i(
        tag: String,
        message: String,
    ) {
        timber.log.Timber.tag(tag).i(message)
    }

    override fun w(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (throwable != null) {
            timber.log.Timber.tag(tag).w(throwable, message)
        } else {
            timber.log.Timber.tag(tag).w(message)
        }
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (throwable != null) {
            timber.log.Timber.tag(tag).e(throwable, message)
        } else {
            timber.log.Timber.tag(tag).e(message)
        }
    }
}

/**
 * Simple console logger for testing - just prints everything to standard output/standard error.
 * Handy for unit tests where you don't want to deal with Timber setup.
 */
class ConsoleLogger : AppLogger {
    override fun d(
        tag: String,
        message: String,
    ) {
        println("DEBUG [$tag]: $message")
    }

    override fun i(
        tag: String,
        message: String,
    ) {
        println("INFO [$tag]: $message")
    }

    override fun w(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        println("WARN [$tag]: $message")
        throwable?.printStackTrace()
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        System.err.println("ERROR [$tag]: $message")
        throwable?.printStackTrace()
    }
}
