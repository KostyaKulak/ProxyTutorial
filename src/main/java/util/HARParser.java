package util;

import com.google.gson.*;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;


public class HARParser {

    public static String getValue(WebDriver driver, String key, String url) {
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(0);
        proxy.newHar(url);
        driver.get(url);
        Har har = proxy.endHar();
        File file = new File(".har");
        try {
            har.writeTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner scanner = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            scanner = new Scanner(file);
            while (scanner.hasNext()) {
                stringBuilder.append(scanner.nextLine()).append('\n');
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String jsonString = stringBuilder.toString();
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        String result = "";
        JsonElement object = jsonObject.get("log");
        while (result.equals("")) {
            if (object.isJsonObject()) {
                for (Map.Entry<String, JsonElement> str : object.getAsJsonObject().entrySet()) {
                    if (str.getKey().equals(key)) {
                        result = str.getValue().toString();
                        break;
                    }
                }
            } else {
                result = ((JsonObject) object).get(key).getAsString();
            }
        }
        return result;
    }

    public static void main(String[] args) {
        WebDriver driver = new FirefoxDriver();
        System.out.println(getValue(driver, "httpVersion", "https://www.igvita.com"));
        driver.quit();
        System.exit(0);
    }
}
