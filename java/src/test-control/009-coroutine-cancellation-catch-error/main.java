import static com.m8test.script.GlobalVariables.*;
var scope = $coroutines.newScope(null);
var timer = $script.getThreads().getMain().getTimer();
var source = scope.delay(10000L);
final int[] recoveryCount = {0};
var recovered = source.catchError((s, error) -> {
    recoveryCount[0]++;
    return s.delay(0L);
});
recovered.onComplete(() -> {
    if (!recovered.isCancelled() || recoveryCount[0] != 0)
        throw new RuntimeException("catchError recovered cancellation");
    $logger.info("coroutine:java:catch-error-cancellation-propagated");
});
timer.setTimeout(params -> { source.cancel("cancel source"); return null; }, 100L);
