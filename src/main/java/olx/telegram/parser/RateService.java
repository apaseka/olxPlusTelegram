package olx.telegram.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;


public class RateService {

    private static Double usdRate = 27d;
    private static Double euroRate = (29d);

    public static void downloadRates() {
        try {
            Document doc = Jsoup.connect("https://finance.ua/").get();
            final String textUsd = doc.select("a[class].fua-xrates__row").attr("data-vr-contentbox-url", "//tables.finance.ua/ru/currency/official/-/1").first().children().get(1).children().first().textNodes().get(0).text();
            final String textEuro = doc.select("a[class].fua-xrates__row").attr("data-vr-contentbox-url", "//tables.finance.ua/ru/currency/official/-/1").get(1).children().get(1).children().first().textNodes().get(0).text();
            System.out.println("usd " + (usdRate = (Double.parseDouble(textUsd))));
            System.out.println("euro " + (euroRate = (Double.parseDouble(textEuro))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Double getUsdRate() {
        return usdRate;
    }

    public static void setUsdRate(Double usdRate) {
        RateService.usdRate = usdRate;
    }

    public static Double getEuroRate() {
        return euroRate;
    }

    public static void setEuroRate(Double euroRate) {
        RateService.euroRate = euroRate;
    }
}
