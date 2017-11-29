package com.example.wsconsumer;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.ws.Provider;

@SpringBootApplication
@Log
public class WsConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsConsumerApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(QuoteClient quoteClient) {
        return args -> {
            String ticker = "IBM";

            if(args.length > 0) {
                ticker = args[0];
            }
            GetQuoteResponse response = quoteClient.getQuote(ticker);
            log.info(response.getGetQuoteResult());
        };
    }

}

@Configuration
class QuoteClientConfiguration {

    @Value("ws.endpoint.uri")
    String uri;

    @Bean
    Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.example.wsconsumer");
        return marshaller;
    }

    @Bean
    QuoteClient quoteclient(Jaxb2Marshaller marshaller) {
        QuoteClient client = new QuoteClient();
        client.setDefaultUri(uri);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }
}

@Log
class QuoteClient extends WebServiceGatewaySupport {
    @Value("ws.endpoint.uri")
    String wsURI;

    @Value("ws.callback.uri")
    String wsCallbackURI;

    public GetQuoteResponse getQuote(String symbol) {

        GetQuote request = new GetQuote();
        request.setSymbol(symbol);

        log.info("requesting: " + symbol);

        GetQuoteResponse response = (GetQuoteResponse) getWebServiceTemplate()
                .marshalSendAndReceive(
                        wsURI,
                        request,
                        new SoapActionCallback(wsCallbackURI)
                );

        return response;
    }
}