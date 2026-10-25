import static com.m8test.script.GlobalVariables.*;
com.m8test.script.core.api.thread.Timer timer = $script.getThreads().getMain().getTimer();
final boolean[] timeoutArgs = {false}; final boolean[] nested = {false};
final boolean[] worker = {false}; final boolean[] afterException = {false};
final int[] count = {0}; final int[] interval = {0};
final com.m8test.script.core.api.thread.NewThread[] workerThread = {null};
final boolean[] workerTimerDone = {false};
Object nestedResult = timer.runWithThreadContext(() -> timer.runWithThreadContext(() -> "nested"));
if (!"nested".equals(nestedResult)) throw new RuntimeException("nested context result mismatch");
timer.setImmediate(params -> { nested[0] = true; $logger.info("thread-context:java:immediate"); return null; });
timer.setTimeout(params -> {
    if (params.length != 2 || !"payload".equals(params[0]) || params[1] != null)
        throw new RuntimeException("timeout arguments mismatch");
    timeoutArgs[0] = true;
    $logger.info("thread-context:java:timeout");
    return null;
}, 100L, new Object[] {"payload", null});
int cancelledTimeout = timer.setTimeout(params -> { throw new RuntimeException("cleared timeout ran"); }, 1000L);
timer.clearTimeout(cancelledTimeout);
int cancelledImmediate = timer.setImmediate(params -> { throw new RuntimeException("cleared immediate ran"); });
timer.clearImmediate(cancelledImmediate);
interval[0] = timer.setInterval(params -> {
    if (params.length != 1 || !"tick".equals(params[0])) throw new RuntimeException("interval arguments mismatch");
    if (++count[0] == 2) { timer.clearInterval(interval[0]); $logger.info("thread-context:java:interval"); }
    return null;
}, 75L, new Object[] {"tick"});
$script.getThreads().onStart(thread -> {
    if (!thread.isCurrent() || thread.isMain() || !thread.isAlive() || thread.isInterrupted() || !"thread-context-worker".equals(thread.getName()))
        throw new RuntimeException("thread start context mismatch");
    $logger.info("thread-context:java:thread-start");
});
$script.getThreads().onExit(thread -> {
    if (!thread.isCurrent() || thread.isMain() || !thread.isAlive() || thread.isInterrupted() || !"thread-context-worker".equals(thread.getName()))
        throw new RuntimeException("thread exit context mismatch");
    $logger.info("thread-context:java:thread-exit");
});
workerThread[0] = $script.getThreads().start("thread-context-worker", thread -> {
    thread.setBackground(true);
    if (!thread.isBackground()) throw new RuntimeException("background state was not set");
    thread.getTimer().setImmediate(params -> { workerTimerDone[0] = true; $logger.info("thread-context:java:worker-timer"); });
    worker[0] = true;
    $logger.info("thread-context:java:thread");
});
timer.setTimeout(params -> {
    if (!timeoutArgs[0] || !nested[0] || !worker[0] || !workerTimerDone[0] || count[0] != 2)
        throw new RuntimeException("timer callback state mismatch");
    if (workerThread[0] == null || !workerThread[0].isAlive()) throw new RuntimeException("worker should remain alive in background");
    workerThread[0].interrupt();
    if (!workerThread[0].isInterrupted()) throw new RuntimeException("interrupt state was not observable");
    workerThread[0].setBackground(false);
    if (workerThread[0].isBackground()) throw new RuntimeException("background state was not cleared");
    $logger.info("thread-context:java:thread-interrupt");
    $logger.info("thread-context:java:verified");
    return null;
}, 500L);
