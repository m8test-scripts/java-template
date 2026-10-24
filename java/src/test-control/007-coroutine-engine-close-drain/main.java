import static com.m8test.script.GlobalVariables.*;

var scope = $coroutines.newScope(null);
var timer = $script.getThreads().getMain().getTimer();

scope.launch(
    context -> { context.setDispatcher(dispatchers -> dispatchers.getIO()); return null; },
    null,
    current -> {
        $logger.info("coroutine:java:engine-close-work-started");
        try { Thread.sleep(5500L); } catch (InterruptedException error) { throw new RuntimeException(error); }
        $logger.info("coroutine:java:engine-close-cleanup");
        return null;
    }
).invokeOnCompletion(error -> $logger.info("coroutine:java:engine-close-completion"));

timer.setTimeout(params -> {
    $script.getEngine().close();
    $logger.info("coroutine:java:engine-close-returned");
}, 100L);
