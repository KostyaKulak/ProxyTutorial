package util;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.IOException;


public class HARParser {

    public static String getValue(WebDriver driver, String key, String url) {
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(0);
        switch (key) {
            case "headers":
                proxy.enableHarCaptureTypes(CaptureType.RESPONSE_HEADERS);
                break;
            case "body":
                proxy.enableHarCaptureTypes(CaptureType.RESPONSE_CONTENT);
                break;
        }
        proxy.addRequestFilter((httpRequest, httpMessageContents, httpMessageInfo) -> {
            if (httpMessageInfo.getOriginalUrl().endsWith("/some-endpoint-to-intercept")) {
                String messageContents = httpMessageContents.getTextContents();
                String newContents = messageContents.replaceAll("original-string", "my-modified-string");
                httpMessageContents.setTextContents(newContents);
            }
            return null;
        });
        proxy.addResponseFilter((response, contents, messageInfo) -> {
            contents.setTextContents("This message body will appear in all responses!");
        });
        proxy.newHar(url);
        driver.get(url);
        Har har = proxy.getHar();
        File file = new File(".har");
        try {
            har.writeTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = "";
        if (har.getLog().getEntries().isEmpty()) {
            return "This site has empty har entries";
        } else {
            switch (key) {
                case "headers":
                    result = har.getLog().getEntries().get(0).getResponse().getHeaders().toString();
                    break;
                case "body":
                    result = har.getLog().getEntries().get(0).getResponse().getContent().toString();
                    break;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        WebDriver driver = new FirefoxDriver();
        System.out.println(getValue(driver, "headers", "http://www.yahoo.com"));
        driver.quit();
        System.exit(0);
    }
}
