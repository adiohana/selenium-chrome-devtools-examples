import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.Console;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.network.Network;
import org.openqa.selenium.devtools.network.model.BlockedReason;
import org.openqa.selenium.devtools.network.model.InterceptionStage;
import org.openqa.selenium.devtools.network.model.RequestPattern;
import org.openqa.selenium.devtools.network.model.ResourceType;
import org.openqa.selenium.devtools.security.Security;

import java.util.Optional;

/**
 * Created by aohana
 */
public class ChromeDevToolsTest {

    private static ChromeDriver chromeDriver;
    private static DevTools chromeDevTools;

    @BeforeClass
    public static void initDriverAndDevTools() {

        chromeDriver = new ChromeDriver();

        chromeDevTools = chromeDriver.getDevTools();
        chromeDevTools.createSession();

    }

    @Test
    public void filterUrls() {

        //enable Network
        chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        //set blocked URL patterns
        chromeDevTools.send(Network.setBlockedURLs(ImmutableList.of("*.css", "*.png")));

        //add event listener to verify that css and png are blocked
        chromeDevTools.addListener(Network.loadingFailed(), loadingFailed -> {

            if (loadingFailed.getResourceType().equals(ResourceType.Stylesheet)) {
                Assert.assertEquals(loadingFailed.getBlockedReason(), BlockedReason.inspector);
            } else if (loadingFailed.getResourceType().equals(ResourceType.Image)) {
                Assert.assertEquals(loadingFailed.getBlockedReason(), BlockedReason.inspector);
            }

        });

        chromeDriver.get("https://apache.org");

    }

    @Test
    public void addCustomHeaders() {

        //enable Network
        chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        //set custom header
        chromeDevTools.send(Network.setExtraHTTPHeaders(ImmutableMap.of("customHeaderName", "customHeaderValue")));

        //add event listener to verify that requests are sending with the custom header
        chromeDevTools.addListener(Network.requestWillBeSent(), requestWillBeSent -> Assert
                .assertEquals(requestWillBeSent.getRequest().getHeaders().get("customHeaderName"),
                        "customHeaderValue"));

        chromeDriver.get("https://apache.org");

    }

    @Test
    public void interceptRequestAndContinue() {

        //enable Network
        chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        //add listener to intercept request and continue
        chromeDevTools.addListener(Network.requestIntercepted(),
                requestIntercepted -> chromeDevTools.send(
                        Network.continueInterceptedRequest(requestIntercepted.getInterceptionId(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(), Optional.empty(),
                                Optional.empty(),
                                Optional.empty(), Optional.empty())));

        //set request interception only for css requests
        RequestPattern requestPattern = new RequestPattern("*.css", ResourceType.Stylesheet, InterceptionStage.HeadersReceived);
        chromeDevTools.send(Network.setRequestInterception(ImmutableList.of(requestPattern)));

        chromeDriver.get("https://apache.org");

    }

    @Test
    public void verifyConsoleMessageAdded() {

        String consoleMessage = "Hello Selenium 4";

        //enable Console
        chromeDevTools.send(Console.enable());

        //add listener to verify the console message
        chromeDevTools.addListener(Console.messageAdded(), consoleMessageFromDevTools ->
                Assert.assertEquals(true, consoleMessageFromDevTools.getText().equals(consoleMessage)));

        chromeDriver.get("https://apache.org");

        //execute JS - write console message
        chromeDriver.executeScript("console.log('" + consoleMessage + "');");

    }

    @Test
    public void loadInsecureWebsite() {

        //enable Security
        chromeDevTools.send(Security.enable());

        //set ignore certificate errors
        chromeDevTools.send(Security.setIgnoreCertificateErrors(true));

        //load insecure website
        chromeDriver.get("https://expired.badssl.com/");

        //verify that the pge was loaded
        Assert.assertEquals(true, chromeDriver.getPageSource().contains("expired"));

    }

//    @Test
    public void genericGetBrowserVersion() {
        chromeDevTools.send(new Command<>("Browser.crash", ImmutableMap.of()));
    }

    @AfterClass
    public static void driverQuit() {
        chromeDriver.quit();
    }

}
