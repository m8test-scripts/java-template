import static com.m8test.script.GlobalVariables.*;
var scope = $coroutines.newScope(null);
var timer = $script.getThreads().getMain().getTimer();
final String captured = "java-nested";
final boolean[] otherTaskProgress = {false};
timer.setTimeout(params -> { otherTaskProgress[0] = true; $logger.info("nest:java:other-task-progressed"); return null; }, 50L);
scope.launch(context -> { context.setDispatcher(dispatchers -> dispatchers.getIO()); return null; }, null, outer -> {
    if (!"java-nested".equals(captured)) throw new RuntimeException("outer capture unavailable");
    $logger.info("nest:java:outer-started");
    scope.launch(context -> {
        context.setDispatcher(dispatchers -> dispatchers.getScriptMain());
        return null;
    }, null, inner -> {
        if (!"java-nested".equals(captured)) throw new RuntimeException("nested capture unavailable");
        $logger.info("nest:java:inner-callback");
        return null;
    });
    if (!"java-nested".equals(captured)) throw new RuntimeException("outer capture not restored");
    $logger.info("nest:java:outer-resumed");
    return null;
});
scope.delay(200L).onSuccess(value -> {
    if (!otherTaskProgress[0] || !"java-nested".equals(captured)) throw new RuntimeException("delay state mismatch");
    $logger.info("nest:java:delay-resumed");
});
timer.setTimeout(params -> { $logger.info("nest:java:gate"); scope.cancel("done"); return null; }, 1500L);
