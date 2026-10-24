import static com.m8test.script.GlobalVariables.*;

var scope = $coroutines.newScope(null);
var timer = $script.getThreads().getMain().getTimer();
var closeReturned = new java.util.concurrent.atomic.AtomicBoolean(false);

scope.launch(
    context -> { context.setDispatcher(dispatchers -> dispatchers.getIO()); return null; },
    null,
    current -> {
        $logger.info("coroutine:java:shutdown-work-started");
        try {
            Thread.sleep(5500L);
        } catch (InterruptedException error) {
            throw new RuntimeException(error);
        }
        if (!closeReturned.get()) throw new IllegalStateException("manager close did not return before cleanup");
        $logger.info("coroutine:java:shutdown-cleanup-after-close");
        return null;
    }
).invokeOnCompletion(error -> $logger.info("coroutine:java:shutdown-completion-after-close"));

timer.setTimeout(params -> {
    $coroutines.close();
    closeReturned.set(true);
    $logger.info("coroutine:java:shutdown-close-returned");
}, 100L);
