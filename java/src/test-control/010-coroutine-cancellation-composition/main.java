import static com.m8test.script.GlobalVariables.*;

var scope = $coroutines.newScope(null);
var timeoutScope = $coroutines.newScope(null);
var nullableTimeoutScope = $coroutines.newScope(null);
var timedLoopScope = $coroutines.newScope(null);
var normalLoopScope = $coroutines.newScope(null);
var composedWaitScope = $coroutines.newScope(null);
var timer = $script.getThreads().getMain().getTimer();
final boolean[] recoveryCalled = {false};
timeoutScope.withTimeout(75L, s -> s.delay(1000L).then((inner, ignored) -> inner.resolve("late")))
    .catchError((s, error) -> {
        recoveryCalled[0] = true;
        return s.resolve("fallback");
    })
    .onSuccess(value -> { throw new RuntimeException("timeout unexpectedly recovered"); })
    .onError(error -> {
        if (recoveryCalled[0]) throw new RuntimeException("timeout cancellation was recovered");
        $logger.info("coroutine:java:timeout-cancellation-preserved");
    });

var timeoutOrNull = nullableTimeoutScope.withTimeoutOrNull(75L, s -> s.delay(1000L));
timeoutOrNull
    .onSuccess(value -> {
        if (value != null) throw new RuntimeException("withTimeoutOrNull did not return null");
        $logger.info("coroutine:java:timeout-or-null-ok");
    })
    .onError(error -> {
        $logger.info("coroutine:java:timeout-or-null-error:" + error.getMessage());
    })
    .onComplete(() -> {
        $logger.info("coroutine:java:timeout-or-null-complete:" + timeoutOrNull.isCancelled());
    });

final int[] timedLoopCalls = {0};
timedLoopScope.loopWithTimeout(75L, s -> {
    timedLoopCalls[0]++;
    return s.delay(1000L).then((inner, ignored) -> inner.resolve(true));
}).onError(error -> {
    if (timedLoopCalls[0] != 1) throw new RuntimeException("timed loop iteration count mismatch");
    $logger.info("coroutine:java:loop-timeout-cancelled-wait");
});

final int[] loopCalls = {0};
normalLoopScope.loop(s -> {
    loopCalls[0]++;
    $logger.info("coroutine:java:loop-iteration:" + loopCalls[0]);
    return s.resolve(loopCalls[0] < 3);
}).onComplete(() -> {
    $logger.info("coroutine:java:loop-completed-count:" + loopCalls[0]);
    if (loopCalls[0] != 3) throw new RuntimeException("loop did not finish after three iterations: " + loopCalls[0]);
});

var composedWait = composedWaitScope.withTimeoutOrNull(500L, s -> s.delay(25L).then((inner, ignored) -> inner.resolve(23)));
composedWait
    .onSuccess(value -> {
        if (value == null || ((Number) value).intValue() != 23)
            throw new RuntimeException("composed wait returned the wrong value");
        $logger.info("coroutine:java:composed-wait-ok");
    })
    .onError(error -> {
        $logger.info("coroutine:java:composed-wait-error:" + error.getMessage());
    })
    .onComplete(() -> {
        $logger.info("coroutine:java:composed-wait-complete:" + composedWait.isCancelled());
    });

timer.setTimeout(params -> {
    $logger.info("coroutine:java:cancellation-gate");
    scope.cancel("done");
}, 3000L);
