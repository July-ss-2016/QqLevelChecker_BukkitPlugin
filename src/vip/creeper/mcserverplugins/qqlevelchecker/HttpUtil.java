package vip.creeper.mcserverplugins.qqlevelchecker;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by July_ on 2017/10/14.
 */
public class HttpUtil {
    public static String sendGet(String url, String cookie) {
        org.apache.http.client.HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        String response = null;

        httpGet.setHeader("cookie", cookie);

        try {
            response = EntityUtils.toString(httpClient.execute(httpGet).getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}
