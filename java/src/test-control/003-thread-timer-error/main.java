import static com.m8test.script.GlobalVariables.*;
com.m8test.script.core.api.thread.Timer timer = $script.getThreads().getMain().getTimer();
timer.setImmediate(params -> {
    $logger.info("thread-context:java:timer-error");
    throw new RuntimeException("expected timer callback failure");
});
