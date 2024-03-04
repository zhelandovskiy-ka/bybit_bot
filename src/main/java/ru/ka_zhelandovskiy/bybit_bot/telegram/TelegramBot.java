package ru.ka_zhelandovskiy.bybit_bot.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {

    public TelegramBot(String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return "";
    }

    @Override
    public void onUpdateReceived(Update update) {

    }

    public void sendMsg(String id, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText(text);
        sendMessage.disableWebPagePreview();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendScreen(String fileName, String id, String text) {
        SendPhoto sf = new SendPhoto();
        sf.setChatId(id);
        sf.setPhoto(new InputFile(fileName));

        if (!text.isEmpty())
            sf.setCaption(text);
        try {
            execute(sf);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            sf.setPhoto(new InputFile("error.png"));
            try {
                execute(sf);
            } catch (TelegramApiException ex) {
                e.printStackTrace();
            }
        }
    }
}
