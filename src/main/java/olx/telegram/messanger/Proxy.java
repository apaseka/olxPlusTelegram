package olx.telegram.messanger;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Component
public class Proxy {
    private static final String proxyEnable = "";
    private static final String proxyHost = "91.196.150.19";
    private static final int proxyPort = 30043;

    private HttpHost getProxyHttpHost() {
        return new HttpHost(proxyHost, proxyPort);
    }

    public DefaultBotOptions buildBotOptions() {
        DefaultBotOptions options = new DefaultBotOptions();
        RequestConfig config;
        if (proxyEnable.equals("true")) {
            config = RequestConfig.custom().setProxy(getProxyHttpHost()).build();
        } else {
            config = RequestConfig.custom().build();
        }
        options.setRequestConfig(config);
        return options;
    }
}
