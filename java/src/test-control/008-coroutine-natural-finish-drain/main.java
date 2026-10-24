import static com.m8test.script.GlobalVariables.*;

var scope = $coroutines.newScope(null);
var mainReturned = new java.util.concurrent.atomic.AtomicBoolean(false);
var workStarted = new java.util.concurrent.CountDownLatch(1);
var drainComplete = new java.util.concurrent.atomic.AtomicBoolean(false);

scope.launch(
    context -> { context.setDispatcher(dispatchers -> dispatchers.getIO()); return null; },
    null,
    current -> {
        $logger.info("coroutine:java:natural-finish-work-started");
        workStarted.countDown();
        try {
            Thread.sleep(5500L);
        } catch (InterruptedException error) {
            throw new RuntimeException(error);
        }
        if (!mainReturned.get()) throw new IllegalStateException("main script did not return before cleanup");
        $logger.info("coroutine:java:natural-finish-cleanup");
        return null;
    }
).invokeOnCompletion(error -> {
    if (error == null) {
        $logger.info("coroutine:java:natural-finish-not-cancelled");
    } else {
        drainComplete.set(true);
        $logger.info("coroutine:java:natural-finish-cancelled");
    }
});

try {
    if (!workStarted.await(3L, java.util.concurrent.TimeUnit.SECONDS)) {
        throw new IllegalStateException("coroutine work did not start before main script returned");
    }
} catch (InterruptedException error) {
    throw new RuntimeException(error);
}
$logger.info("coroutine:java:natural-finish-main-returning");
mainReturned.set(true);
