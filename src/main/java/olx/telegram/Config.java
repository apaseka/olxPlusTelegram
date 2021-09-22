package olx.telegram;

import olx.telegram.parser.ParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;

@EnableScheduling
@Configuration
public class Config {

    public static Map<String, Integer> urlToParse = new HashMap();
    @Autowired
    ParserService parserService;

    @Scheduled(fixedDelay = 1000 * 60 * 3)
    public void doSearch() {
        if (!urlToParse.isEmpty())
            urlToParse.forEach((k, v) ->
                    parserService.parse(k)
            );
    }

}
