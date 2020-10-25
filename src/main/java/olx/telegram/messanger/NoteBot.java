package olx.telegram.messanger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class NoteBot extends TelegramLongPollingBot {


    @Autowired
    private BotStartable botStartable;

    @Autowired
    public NoteBot(Proxy connect) {
        super(connect.buildBotOptions());
    }

    @Override
    public void onUpdateReceived(Update update) {
        botStartable.start(update);
    }

    @Override
    public String getBotUsername() {
        String bot = "alex_test2_bot";
//        String bot="vovka_olx_bot";
        System.out.println(bot);
        return bot;
    }

    @Override
    public String getBotToken() {
        return "870428956:AAHpZ6laLcR-08a-S2sQCLHEgwLDKPiN8zw";
//        return "1325049711:AAHH7Ta15qGEIzMn8rbfPYvgGSWApN6itrE";
    }
}
