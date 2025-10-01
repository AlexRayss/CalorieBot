import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;

public class CalorieBot extends TelegramLongPollingBot {

    private final Map<String, Nutrients> products = new HashMap<>();
    private final Dotenv dotenv;

    public CalorieBot() {
        // Инициализация .env
        dotenv = Dotenv.load();

        // Примеры продуктов
        products.put("Вареные макароны", new Nutrients(150, 5, 1));
        products.put("Сырая курица", new Nutrients(165, 31, 3));
        products.put("Яблоко", new Nutrients(52, 0, 0));
    }

    @Override
    public String getBotUsername() {
        return "YourBotUsername"; // заменить на имя вашего бота
    }

    @Override
    public String getBotToken() {
        return dotenv.get("BOT_TOKEN"); // получаем токен из .env
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText();

            if (text.equals("/start")) {
                sendText(message, "Привет! Введи продукт и граммы в формате:\nПродукт: 100");
                return;
            }

            if (text.contains(":")) {
                String[] parts = text.split(":");
                if (parts.length != 2) {
                    sendText(message, "Неверный формат. Пример: Сырая курица: 150");
                    return;
                }

                String productName = parts[0].trim();
                double grams;
                try {
                    grams = Double.parseDouble(parts[1].trim());
                } catch (NumberFormatException e) {
                    sendText(message, "Неверное количество грамм.");
                    return;
                }

                if (products.containsKey(productName)) {
                    Nutrients n = products.get(productName);
                    double calories = n.calories * grams / 100;
                    double proteins = n.proteins * grams / 100;
                    double fats = n.fats * grams / 100;

                    sendText(message, String.format(
                            "%s — %.2f г:\nКалории: %.2f\nБелки: %.2f г\nЖиры: %.2f г",
                            productName, grams, calories, proteins, fats));
                } else {
                    sendText(message, "Продукт не найден. Попробуй другой.");
                }
            }
        }
    }

    private void sendText(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new CalorieBot());
        System.out.println("CalorieBot запущен!");
    }

    static class Nutrients {
        double calories;
        double proteins;
        double fats;

        Nutrients(double calories, double proteins, double fats) {
            this.calories = calories;
            this.proteins = proteins;
            this.fats = fats;
        }
    }
}
