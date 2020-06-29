package olx.telegram;

import olx.telegram.parser.AdvertisementDashboardRepository;
import olx.telegram.parser.ParserService;
import olx.telegram.parser.SearchRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication
public class TelegramApplication {

    private static TelegramLongPollingBot telegramLongPollingBot;
    private static AdvertisementDashboardRepository dashboardRepository;
    private static SearchRepository searchRepository;

    public static void main(String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(TelegramApplication.class, args);

        telegramLongPollingBot = context.getBean(TelegramLongPollingBot.class);
        dashboardRepository = context.getBean(AdvertisementDashboardRepository.class);
        searchRepository = context.getBean(SearchRepository.class);
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            System.out.println("RegisterBot Start");
            botsApi.registerBot(telegramLongPollingBot);
            System.out.println("RegisterBot OK");

            dashboardRepository.findAll().forEach(e -> {
                ParserService.identity.add(e.getExternalId());
                ParserService.identity.add(e.getAdvTitle());
            });

            searchRepository.findAll().forEach(e -> Config.urlToParse.put(e.getUrl(), 0));

        } catch (Exception te) {
            System.out.println("RegisterBot FAIL");
            te.printStackTrace();
        }
        System.out.println("Bot Ok");
    }
}
