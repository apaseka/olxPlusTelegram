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
import java.util.HashSet;
import java.util.Set;

@Service
@Data
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ParserService {

    public static Set<String> identity = new HashSet<>();

    AdvertisementDashboardRepository dashboardRepository;
    BotService botService;
    public Integer loop;
    private String url;

    @Autowired
    public ParserService(AdvertisementDashboardRepository dashboardRepository, BotService botService) {
        this.dashboardRepository = dashboardRepository;
        this.botService = botService;
    }

    public void parse(String url) {
        this.url = url;
        loop = Config.urlToParse.get(url);
        loop++;
        Config.urlToParse.put(url, loop);

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
                final String location = advertisement.select("p[class].lheight16").text();

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
                        .build();

                if (!identity.contains(externalId)) {
                    identity.add(externalId);
                    dashboardRepository.save(shortAdvertisementFromDashboard);

                    if (loop > 1) {

                        botService.sendMessage("\r\n" + mainImage + "\r\n" + detailsHref + "\r\n" + price + "\r\n" + advTitle);
                        System.out.println(price + "	" + advTitle);

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.printf("%s %d \r\n%s\r\n", LocalDateTime.now(), loop, url);
        System.out.println("list size: " + identity.size() + "\r\n");

    }
}
