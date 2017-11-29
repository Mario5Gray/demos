package com.example.cachegemfile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@Log
@ClientCacheApplication(name = "CachingGemFireApplication", logLevel = "error")
// TODO used to be @EnableGemfireCache
public class CacheGemfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheGemfileApplication.class, args);
    }

    @Bean("Quotes")
    ClientRegionFactoryBean<Integer, Integer> quotesRegion(GemFireCache gemFireCache) {
        ClientRegionFactoryBean<Integer, Integer> quotesRegion = new ClientRegionFactoryBean<>();

        quotesRegion.setCache(gemFireCache);
        quotesRegion.setClose(false);
        quotesRegion.setShortcut(ClientRegionShortcut.LOCAL);

        return quotesRegion;
    }

    @Bean
    QuoteService quoteService() {
        return new QuoteService();
    }

    @Bean
    ApplicationRunner runner() {
        return args -> {
            Quote quote = requestQuote(12L);
            requestQuote(quote.getId());
            requestQuote(10L);
        };
    }

    Quote requestQuote(Long id) {
        QuoteService quoteService = quoteService();

        long startTime = System.currentTimeMillis();

        Quote quote = Optional.ofNullable(id)
                .map(quoteService::requestQuote)
                .orElseGet(quoteService::requestRandomQuote);
        long elapsedTime = System.currentTimeMillis();

        System.out.printf("\"%1$s\"%nCache Miss [%2$s] - Elapsed Time [%3$s ms]%n", quote,
                quoteService.isCacheMiss(), (elapsedTime - startTime));

        return quote;
    }
}

class QuoteService {
    protected static final String ID_BASED_QUOTE_SERVICE_URL = "http://gturnquist-quoters.cfapps.io/api/{id}";
    protected static final String RANDOM_QUOTE_SERVICE_URL = "http://gturnquist-quoters.cfapps.io/api/random";

    private volatile boolean cacheMiss = false;

    private final RestTemplate quoteServiceTemplate = new RestTemplate();

    boolean isCacheMiss() {
        boolean cacheMiss = this.cacheMiss;
        this.cacheMiss = false;
        return cacheMiss;
    }

    void setCacheMiss() {
        this.cacheMiss = true;
    }

    @Cacheable("Quotes")
    public Quote requestQuote(Long id) {
        setCacheMiss();
        return requestQuote(ID_BASED_QUOTE_SERVICE_URL, Collections.singletonMap("id", id));
    }

    @CachePut(cacheNames = "Quotes", key = "#result.id")
    public Quote requestRandomQuote() {
        setCacheMiss();
        return requestQuote(RANDOM_QUOTE_SERVICE_URL);
    }

    public Quote requestQuote(String URL) {
        return requestQuote(URL, Collections.emptyMap());
    }

    public Quote requestQuote(String URL, Map<String, Object> urlVars) {
        return Optional.ofNullable(this.quoteServiceTemplate.getForObject(URL, QuoteResponse.class, urlVars))
                .map(QuoteResponse::getQuote)
                .orElse(null);
    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
class Quote {

    Long id;

    String quote;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Quote)) {
            return false;
        }

        Quote that = (Quote) obj;
        return ObjectUtils.nullSafeEquals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        int hashValue = 17;
        return 37 * hashValue + ObjectUtils.nullSafeHashCode(getId());
    }

    @Override
    public String toString() {
        return getQuote();
    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class QuoteResponse {
    @JsonProperty("value")
    Quote quote;

    @JsonProperty("type")
    String status;

    @Override
    public String toString() {
        return String.format("{ @thype = %1$s, quote = '%2$s', status=%3$s }",
                getClass().getName(), getQuote(), getStatus());
    }
}
