import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class TestRunner {

    public static void runTests(Class<?> c) {
        Method beforeSuite = null;
        Method afterSuite = null;
        List<Method> beforeTests = new ArrayList<>();
        List<Method> afterTests = new ArrayList<>();
        List<Method> tests = new ArrayList<>();

        for (Method m : c.getDeclaredMethods()) {
            if (m.isAnnotationPresent(BeforeSuite.class)) {
                if (beforeSuite != null) throw new RuntimeException("Only one @BeforeSuite allowed");
                if (!Modifier.isStatic(m.getModifiers())) throw new RuntimeException("@BeforeSuite must be static: " + m);
                beforeSuite = m;
            }
            if (m.isAnnotationPresent(AfterSuite.class)) {
                if (afterSuite != null) throw new RuntimeException("Only one @AfterSuite allowed");
                if (!Modifier.isStatic(m.getModifiers())) throw new RuntimeException("@AfterSuite must be static: " + m);
                afterSuite = m;
            }
            if (m.isAnnotationPresent(BeforeTest.class)) beforeTests.add(m);
            if (m.isAnnotationPresent(AfterTest.class)) afterTests.add(m);
            if (m.isAnnotationPresent(Test.class)) tests.add(m);
        }

        // Sort tests by priority descending
        tests.sort((m1, m2) -> Integer.compare(m2.getAnnotation(Test.class).priority(),
                                               m1.getAnnotation(Test.class).priority()));

        Object instance = null;
        try {
            if (hasInstanceMethods(beforeTests, afterTests, tests)) {
                Constructor<?> ctor = c.getDeclaredConstructor();
                ctor.setAccessible(true);
                instance = ctor.newInstance();
            }

            System.out.println("===== RUNNING TESTS FOR: " + c.getSimpleName() + " =====");
            if (beforeSuite != null) invokeStatic(beforeSuite);

            int passed = 0;
            int failed = 0;

            for (Method test : tests) {
                for (Method bt : beforeTests) invokeMethod(instance, bt);

                System.out.println("[TEST] " + test.getName() + " (priority " + test.getAnnotation(Test.class).priority() + ")");
                try {
                    runSingleTest(instance, test);
                    System.out.println("  -> PASSED");
                    passed++;
                } catch (Throwable e) {
                    System.out.println("  -> FAILED: " + e.getMessage());
                    failed++;
                }

                for (Method at : afterTests) invokeMethod(instance, at);
            }

            if (afterSuite != null) invokeStatic(afterSuite);

            System.out.println("\n===== TEST SUMMARY =====");
            System.out.println("PASSED: " + passed);
            System.out.println("FAILED: " + failed);
            System.out.println("=========================");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasInstanceMethods(List<Method> beforeTests, List<Method> afterTests, List<Method> tests) {
        return Stream.of(beforeTests, afterTests, tests)
                .flatMap(Collection::stream)
                .anyMatch(m -> !Modifier.isStatic(m.getModifiers()));
    }

    private static void runSingleTest(Object instance, Method test) throws Exception {
        test.setAccessible(true);
        CsvSource csv = test.getAnnotation(CsvSource.class);
        if (csv != null) {
            String[] parts = csv.value().split(",");
            Parameter[] params = test.getParameters();
            if (parts.length != params.length) {
                throw new RuntimeException("CsvSource values count doesn't match parameters for " + test.getName());
            }
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                args[i] = convert(parts[i].trim(), params[i].getType());
            }
            invokeMethod(instance, test, args);
        } else {
            if (test.getParameterCount() > 0)
                throw new RuntimeException("Parameters found but no @CsvSource for " + test.getName());
            invokeMethod(instance, test);
        }
    }

    private static void invokeMethod(Object instance, Method m, Object... args) throws Exception {
        m.setAccessible(true);
        if (Modifier.isStatic(m.getModifiers())) m.invoke(null, args);
        else m.invoke(instance, args);
    }

    private static void invokeStatic(Method m) throws Exception {
        m.setAccessible(true);
        m.invoke(null);
    }

    private static Object convert(String token, Class<?> type) {
        if (type == String.class) return token;
        if (type == int.class || type == Integer.class) return Integer.parseInt(token);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(token);
        if (type == double.class || type == Double.class) return Double.parseDouble(token);
        if (type == char.class || type == Character.class) return token.charAt(0);
        return token;
    }
}
