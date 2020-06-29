package olx.telegram.parser;

import org.springframework.stereotype.Service;

@Service
public class PriceAdapter {

    public static Integer adapt(String price) {
        if (price.contains("$")) {
            return (int) ((Double.parseDouble(price.replaceAll(" ", "")
                    .replace("$", "")
                    .replace("Договорная", ""))) * (RateService.getUsdRate()));
        } else if (price.contains("грн")) {
            return (int) Double.parseDouble(price
                    .replaceAll(" ", "")
                    .replace("грн.", "")
                    .replace("Договорная", ""));
        } else if (price.contains("€")) {
            return (int) (Double.parseDouble(price.replaceAll(" ", "")
                    .replace("€", "")
                    .replace("Договорная", "")) * (RateService.getEuroRate()));
        } else {
            return 123456;
        }
    }
}
