public class SampleTests {

    @BeforeSuite
    public static void beforeAll() {
        System.out.println("=== BEFORE SUITE ===");
    }

    @AfterSuite
    public static void afterAll() {
        System.out.println("=== AFTER SUITE ===");
    }

    @BeforeTest
    public void beforeEach() {
        System.out.println("-- before test --");
    }

    @AfterTest
    public void afterEach() {
        System.out.println("-- after test --");
    }

    @Test(priority = 8)
    public void highPriorityTest() {
        System.out.println("Running high priority test...");
    }

    @Test(priority = 5)
    @CsvSource("10, Hello, 20, true, A")
    public void paramTest(int a, String b, int c, boolean d, char e) {
        System.out.println("paramTest: " + a + "," + b + "," + c + "," + d + "," + e);
    }

    @Test(priority = 3)
    public void failingTest() {
        throw new RuntimeException("Intentional failure");
    }
}
