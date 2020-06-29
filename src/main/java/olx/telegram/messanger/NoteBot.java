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
        return "";
    }

    @Override
    public String getBotToken() {
        return "";
    }
}
