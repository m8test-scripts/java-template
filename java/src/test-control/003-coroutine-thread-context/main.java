import static com.m8test.script.GlobalVariables.*;
var scope = $coroutines.newScope(null);
var timer = $script.getThreads().getMain().getTimer();
scope.launch(context -> {
    context.setName("script-main");
    context.setDispatcher(dispatchers -> dispatchers.getScriptMain());
    if (!"script-main".equals(context.getName())) throw new RuntimeException("coroutine context name mismatch");
    return null;
}, null, s -> {
    if (!$script.getThreads().getCurrent().isCurrent()) throw new RuntimeException("script-main dispatcher mismatch");
    $logger.info("coroutine:java:script-main-verified");
    return null;
});
final String capturedScriptValue = "java-captured";
final boolean[] otherTaskProgress = {false};
timer.setTimeout(params -> { otherTaskProgress[0] = true; $logger.info("coroutine:java:other-task-progressed"); return null; }, 50L);
scope.launch(context -> { context.setDispatcher(dispatchers -> dispatchers.getIO()); return null; }, null, s -> {
    if (!"java-captured".equals(capturedScriptValue)) throw new RuntimeException("captured script value mismatch");
    $logger.info("coroutine:java:io-captured-value-verified");
    return null;
});
scope.delay(200L).onSuccess(value -> {
    if (!"java-captured".equals(capturedScriptValue)) throw new RuntimeException("delayed script value mismatch");
    if (!otherTaskProgress[0]) throw new RuntimeException("other task did not progress during delay");
    $logger.info("coroutine:java:delayed-captured-value-verified");
});
scope.launch(null, null, s -> { $logger.info("coroutine:java:launch"); return null; });
scope._async(null, null, s -> { $logger.info("coroutine:java:async"); return 7; })
    .onSuccess(value -> {
        if (!"7".equals(String.valueOf(value))) throw new RuntimeException("async value mismatch: " + value);
        $logger.info("coroutine:java:async-success");
    });
scope.withContext(context -> null, s -> { $logger.info("coroutine:java:with-context"); return 9; });
$coroutines.getFlows().of(1, 2).map((s, value) -> {
    $logger.info("coroutine:java:flow:" + value);
    return s.resolve(((Number) value).intValue() * 2);
}).toList(scope).onSuccess(values -> {
    if (values.size() != 2 || !"2".equals(String.valueOf(values.get(0))) || !"4".equals(String.valueOf(values.get(1))))
        throw new RuntimeException("flow mapped values mismatch: " + values);
    $logger.info("coroutine:java:flow-complete");
});
scope.delay(5000L).onComplete(() -> $logger.info("coroutine:java:cancelled-work-complete"));
final Object[] recovered = new Object[1];
$coroutines.getFlows().create((s, collector) -> { throw new RuntimeException("flow-boom"); })
    ._catch((s, collector, error) -> s.launch(null, null, ignored -> {
        $logger.info("coroutine:java:flow-catch:" + error.getMessage());
        collector.emit(s, 9);
    }))
    .onCompletion((s, cause) -> s.launch(null, null, ignored ->
        $logger.info("coroutine:java:flow-completion:" + (cause == null))))
    .toList(scope).onSuccess(values -> {
        recovered[0] = values;
        $logger.info("coroutine:java:flow-recovered:" + values);
    });
$coroutines.getFlows().of(1, 2).buffer(0).toList(scope).onSuccess(values -> {
    if (!java.util.Arrays.asList(1, 2).equals(values)) throw new RuntimeException("buffer values mismatch");
    $logger.info("coroutine:java:buffer-ok");
});
$coroutines.getFlows().of(1, 2).debounce(0).toList(scope).onSuccess(values -> {
    if (!java.util.Arrays.asList(1, 2).equals(values)) throw new RuntimeException("debounce values mismatch");
    $logger.info("coroutine:java:debounce-ok");
});
$coroutines.getFlows().of(1, 2, 3).sample(100000).toList(scope).onSuccess(values -> {
    if (!values.isEmpty()) throw new RuntimeException("sample values mismatch");
    $logger.info("coroutine:java:sample-ok");
});
$coroutines.getFlows().interval(0, 1000).timeout(1).toList(scope).onError(error -> {
    if (error.getMessage() == null) throw new RuntimeException("timeout error missing");
    $logger.info("coroutine:java:timeout-observed");
});
final int[] cleanupCount = {0};
$coroutines.getFlows().fromCallback((s, producer) -> s.launch(null, null, ignored -> {
    if (!producer.trySend(1).isSuccess() || !producer.trySend(2).isSuccess())
        throw new RuntimeException("trySend failed");
    producer.awaitClose(() -> { cleanupCount[0]++; $logger.info("coroutine:java:channel-cleanup"); });
    if (!producer.close(null) || !producer.isClosedForSend() || producer.trySend(3).isSuccess())
        throw new RuntimeException("channel close mismatch");
    return null;
})).toList(scope).onSuccess(values -> {
    if (!java.util.Arrays.asList(1, 2).equals(values) || cleanupCount[0] != 1)
        throw new RuntimeException("callback flow cleanup mismatch");
    $logger.info("coroutine:java:callback-flow-ok");
});
timer.setTimeout(params -> { $logger.info("coroutine:java:gate"); scope.cancel("done"); return null; }, 1500L);
