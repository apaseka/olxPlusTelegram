package olx.telegram.messanger;


import olx.telegram.Config;
import olx.telegram.parser.ParserService;
import olx.telegram.parser.SearchEntity;
import olx.telegram.parser.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
public class BotService implements BotStartable {

    @Autowired
    private TelegramLongPollingBot telegramLongPollingBot;
    @Autowired
    private ParserService parserService;
    @Autowired
    private SearchRepository searchRepository;


    private Long chatId;

    public void start(Update update) {
        String request = update.getMessage().getText();
        chatId = update.getMessage().getChatId();

        if (request.toLowerCase().contains("http")) {
            final String[] split = request.split(" ");
            for (String s : split) {
                String url = null;
                try {
                    url = java.net.URLDecoder.decode(s, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                final SearchEntity entity = SearchEntity.builder().url(url).build();
                Config.urlToParse.put(url, 0);
                searchRepository.save(entity);
                parserService.parse(url);

            }
        } else if (request.toLowerCase().contains("видалити") || request.toLowerCase().contains("clean")) {
            searchRepository.deleteAll();
            Config.urlToParse.clear();
        } else {
            final String[] split = request.split("[,.;]");
            SearchEntity.SearchEntityBuilder builder = SearchEntity.builder()
                    .name(split[0].trim());
            if (split.length > 2) SearchEntity.priceFrom = (split[1].trim());
            if (split.length == 3) SearchEntity.priceTo = (split[2].trim());
            SearchEntity entity = builder.build();
            String url = "https://www.olx.ua/list/q-" + entity.getName() + "/?search[filter_float_price:from]=" + SearchEntity.priceFrom + "&search[filter_float_price:to]=" + SearchEntity.priceTo + "&view=list";
            entity.setUrl(url);
            searchRepository.save(entity);
            Config.urlToParse.put(url, 0);
            parserService.parse(url);
        }
    }

    public void sendMessage(String p) {
        final SendMessage message = new SendMessage().setChatId(chatId).setText(p);

        try {
            telegramLongPollingBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
