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
import java.util.Objects;

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
        System.out.println("request: " + request);
        if (request.toLowerCase().contains("http")) {
            System.out.println("Search by http");
            final String[] split = request.split(" ");
            for (String s : split) {
                String url = null;
                try {
                    url = java.net.URLDecoder.decode(s, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                final SearchEntity entity = SearchEntity.builder().url(url).build();
                parserService.parse(url);

                searchRepository.save(entity);
            }
            System.out.println("End http");
        } else if (request.toLowerCase().contains("видалити") || request.toLowerCase().contains("clean")) {
            System.out.println("Delete all");
            searchRepository.deleteAll();
            Config.urlToParse.clear();
        } else if (request.toLowerCase().contains("список") || request.toLowerCase().contains("list")) {
            System.out.println("List");
            final Iterable<SearchEntity> entities = searchRepository.findAllByActiveIsTrue();
            final StringBuilder stringBuilder = new StringBuilder();
            entities.forEach(e -> stringBuilder
                    .append("id=")
                    .append(e.getId())
                    .append(" ")
                    .append(e.getUrl().substring(26)
                            .replace("&view=list", "")
                            .replace("search[filter_float_price:from]=", " ")
                            .replace("/?", "")
                            .replace("&search[filter_float_price:to]=", " - "))
                    .append("\r\n")
            );
            sendMessage(stringBuilder.toString());
        } else if (request.matches("^[0-9]*$]")) {
            System.out.println("Deactivate by id");
            final String[] split = request.split(" ");
            final Long id = Long.parseLong(split[1]);
            Config.urlToParse.remove(searchRepository.findById(id).get().getUrl());
            SearchEntity entity = searchRepository.findById(id).orElseGet(() -> {
                sendMessage("wrong id " + request);
                return null;
            });
            if (!Objects.isNull(entity)) {
                entity.setActive(false);
                searchRepository.save(entity);
                sendMessage("deactivated by id " + request);
            }
        } else if (request.toLowerCase().matches("^[a-zA-zа-яА-Я0-9.,; ]*$")) {
            System.out.println("Search by name");
            final String[] split = request.split("[,.;]");
            SearchEntity.SearchEntityBuilder builder = SearchEntity.builder()
                    .name(split[0].trim());
            if (split.length > 2) SearchEntity.priceFrom = (split[1].trim());
            if (split.length == 3) SearchEntity.priceTo = (split[2].trim());
            SearchEntity entity = builder.build();
            String url = "https://www.olx.ua/list/q-" + entity.getName() + "/?search[filter_float_price:from]=" + SearchEntity.priceFrom + "&search[filter_float_price:to]=" + SearchEntity.priceTo + "&view=list";
            entity.setUrl(url);
            parserService.parse(url);
            searchRepository.save(entity);

        }

        System.out.println("End: " + request);
    }

    public void sendMessage(String p) {
        final SendMessage message = new SendMessage().setChatId(570931981L).setText(p);

        try {
            telegramLongPollingBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
