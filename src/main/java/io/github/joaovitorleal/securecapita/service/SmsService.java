package io.github.joaovitorleal.securecapita.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Deprecated(since = "1.1.0")
@Service("smsService")
public class SmsService implements NotificationService {

    @Value("${FROM_NUMBER}")
    private String fromNumber;

    @Value("${TWILIO_ACCOUNT_SID}")
    private String twilioAccountSid;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String twilioAuthToken;

    /**
     * Enviar mensagem de notificações genéricas.
     *
     * @param to Número de telefone de destino.
     * @param message corpo da mensagem.
     */
    public void sendMessage(String to, String message) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Message smsMessage = Message.creator(
                new PhoneNumber("+55" + to),
                new PhoneNumber("+55" + fromNumber),
                message
        ).create();
    }

    /**
     * Enviar de código de autenticação Multifator.
     *
     * @param userFirstName Primeiro nome do usuário.
     * @param to Número de telefone de destino.
     * @param mfaCode Código de autenticação multifator (Two-Factor Authentication)
     */
    @Override
    public void sendMfaCode(String userFirstName, String to, String mfaCode) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Message smsMessage = Message.creator(
                new PhoneNumber("+55" + to),
                new PhoneNumber("+55" + fromNumber),
                mfaCode
        ).create();
        // TODO
    }

    /**
     * @param userFirstName
     * @param to
     * @param verificationUrl
     */
    @Override
    public void sendResetPasswordUrl(String userFirstName, String to, String verificationUrl) {
        // ...
    }

    /**
     * @param userFirstName
     * @param to
     */
    @Override
    public void sendResetPasswordConfirmationMessage(String userFirstName, String to) {
        //...
    }

    /**
     * @param userFirstName
     * @param to
     * @param verificationUrl
     */
    @Override
    public void sendAccountVerificationUrl(String userFirstName, String to, String verificationUrl) {
        //...
    }

    /**
     * @param userFirstName
     * @param to
     */
    @Override
    public void sendAccountVerifiedMessage(String userFirstName, String to) {
        //...
    }
}
