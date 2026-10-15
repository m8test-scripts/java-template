import static com.m8test.script.GlobalVariables.*;
import java.lang.StringBuilder;
import com.m8test.script.wrapper.api.IntArrayWrapper;

StringBuilder builder = new StringBuilder("M8Test");
builder.append(" Java");
if (!"M8Test Java".equals(builder.toString())) throw new AssertionError("JVM StringBuilder");
int[] raw = $arrays.intArrayOf(3, 1, 2);
IntArrayWrapper values = $objectWrappers.wrapIntArray(raw);
if (values.getSize() != 3) throw new AssertionError("wrapped size");
values.set(0, 7);
if (!values.contains(7)) throw new AssertionError("wrapped array mutation");
final boolean[] callbackCalled = new boolean[] { false };
$logger.withContext(context -> { callbackCalled[0] = context != null; return null; });
if (!callbackCalled[0]) throw new AssertionError("Kotlin function callback");
java.util.List<String> list = $iterables.listOf("first", "second");
if (!"second".equals(list.get(1))) throw new AssertionError("host List factory");
$logger.info("java-template-jvm-interop:passed");
