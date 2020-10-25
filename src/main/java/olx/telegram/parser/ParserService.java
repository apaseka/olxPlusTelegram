package olx.telegram.parser;

import lombok.Data;
import olx.telegram.Config;
import olx.telegram.messanger.BotService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Data
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ParserService {

    public static Map<String, List<AdvertisementDashboard>> identity = new HashMap<>();
    public Integer loop;
    AdvertisementDashboardRepository dashboardRepository;
    BotService botService;
    private String url;

    @Autowired
    public ParserService(AdvertisementDashboardRepository dashboardRepository, BotService botService) {
        this.dashboardRepository = dashboardRepository;
        this.botService = botService;
    }

    public void parse(String url) {
        this.url = url;
        loop = Config.urlToParse.get(url) == null ? 0 : Config.urlToParse.get(url);
        loop++;

        try {
            Document doc = Jsoup.connect(this.url).get();

            System.out.println(loop);

            final Element offers_table = doc.getElementById("offers_table");
            final Elements advertisements = offers_table.select("tr[class].wrap");

            for (Element advertisement : advertisements) {

                final String externalId = advertisement.select("table[data-id]").first().attr("data-id");
                String price = advertisement.select("p[class].price").first().text();
                final String detailsHref = advertisement.select("a[href].detailsLink").first().attr("href");
                final String advTitle = advertisement.select("a[class].link.linkWithHash.detailsLink").first().text();
                final String mainImage = advertisement.select("img[class].fleft").attr("src");
                final Elements select = advertisement.select("p[class].lheight16");
                final String time = select.select("span").get(1).text();
                final String location = select.select("span").get(0).text();

                final AdvertisementDashboard shortAdvertisementFromDashboard = AdvertisementDashboard.builder()
                        .advTitle(advTitle)
                        .externalId(externalId)
                        .price(price)
                        .priceUAH(PriceAdapter.adapt(price))
                        .detailsUrl(detailsHref)
                        .loop(loop)
                        .parsingDate(LocalDateTime.now())
                        .searchUrl(this.url)
                        .imageUrl(mainImage)
                        .location(location)
                        .time(time)
                        .build();

                boolean matchPrice = false;
                if (!identity.containsKey(externalId) || (identity.containsKey(externalId) && (matchPrice = checkPrice(price, identity.get(externalId))))) {
                    if (loop > 1) {
                        if (matchPrice) {
                            botService.sendMessage("\r\n" + mainImage + "\r\n" + detailsHref + "\r\n" + price + "	ЦІНУ ЗМІНЕНО" + "\r\n" + advTitle + "\r\n" + location + "\r\n" + time);
                        } else {
                            botService.sendMessage("\r\n" + mainImage + "\r\n" + detailsHref + "\r\n" + price + "\r\n" + advTitle + "\r\n" + location + "\r\n" + time);
                        }
                    }
                    ParserService.identity.compute(externalId, (k, v) -> (v == null) ? new ArrayList<>() : v)
                            .add(shortAdvertisementFromDashboard);
                    dashboardRepository.save(shortAdvertisementFromDashboard);
                    System.out.println(price + "	" + advTitle);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.printf("%s %d \r\n%s\r\n", LocalDateTime.now(), loop, url);
        System.out.println("list size: " + identity.size() + "\r\n");
        Config.urlToParse.put(url, loop);

    }

    private boolean checkPrice(String price, List<AdvertisementDashboard> advertisementDashboardList) {
        return advertisementDashboardList.stream().noneMatch(e -> e.getPrice().equals(price));
    }
}
