import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;

/**
 * Created by aohana
 */
public class ChromeDevToolsTest {

    ChromeDriver chromeDriver;
    DevTools chromeDevTools;

    @BeforeClass
    public void initDriverAndDevTools() {

        chromeDriver = new ChromeDriver();

        chromeDevTools = chromeDriver.getDevTools();
        chromeDevTools.createSession();

    }

    @Test
    public void filterUrls() {

    }
}
