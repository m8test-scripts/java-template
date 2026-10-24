import static com.m8test.script.GlobalVariables.*;

var scope = $coroutines.newScope(null);
var timer = $script.getThreads().getMain().getTimer();

scope.launch(
    context -> { context.setDispatcher(dispatchers -> dispatchers.getIO()); return null; },
    null,
    current -> {
        $logger.info("coroutine:java:interrupt-work-started");
        try { Thread.sleep(5500L); } catch (InterruptedException error) { throw new RuntimeException(error); }
        $logger.info("coroutine:java:interrupt-cleanup");
        return null;
    }
).invokeOnCompletion(error -> $logger.info("coroutine:java:interrupt-completion"));

timer.setTimeout(params -> {
    $script.interrupt();
    try {
        $coroutines.newScope(null);
        $logger.info("coroutine:java:interrupt-new-scope-accepted");
    } catch (Throwable expected) {
        $logger.info("coroutine:java:interrupt-new-scope-rejected");
    }
    try {
        scope.launch(null, null, current -> $logger.info("coroutine:java:unexpected-post-stop-work"));
        $logger.info("coroutine:java:interrupt-new-job-accepted");
    } catch (Throwable expected) {
        $logger.info("coroutine:java:interrupt-new-job-rejected");
    }
    try {
        $coroutines.getFlows().empty().launchIn(scope);
        $logger.info("coroutine:java:interrupt-new-flow-accepted");
    } catch (Throwable expected) {
        $logger.info("coroutine:java:interrupt-new-flow-rejected");
    }
    $logger.info("coroutine:java:interrupt-returned");
}, 100L);
