package io.github.joaovitorleal.securecapita.service.implementation;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.joaovitorleal.securecapita.service.NotificationService;

public class SmsServiceImpl implements NotificationService {
    public static final String FROM_NUMBER = "71988960906";
    public static final String TWILIO_ACCOUNT_SID = "";
    public static final String TWILIO_AUTH_TOKEN = "";

    /**
     * @param to Phone number recipient
     * @param message body of the message
     */
    public void sendMessage(String to, String message) {
        Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
        Message smsMessage = Message.creator(new PhoneNumber("+55" + to),  new PhoneNumber("+55" + FROM_NUMBER), message ).create();
    }
}
