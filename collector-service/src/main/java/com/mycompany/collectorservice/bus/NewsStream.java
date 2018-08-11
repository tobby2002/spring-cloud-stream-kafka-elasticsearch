package com.mycompany.collectorservice.bus;

import com.mycompany.collectorservice.model.News;
import com.mycompany.collectorservice.service.NewsService;
import com.mycompany.commons.avro.NewsEvent;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@EnableBinding(Sink.class)
public class NewsStream {

    private final NewsService newsService;
    private final MapperFacade mapperFacade;

    public NewsStream(NewsService newsService, MapperFacade mapperFacade) {
        this.newsService = newsService;
        this.mapperFacade = mapperFacade;
    }

    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public NewsEvent handleNewsEvent(Message<NewsEvent> message,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                     @Header(KafkaHeaders.OFFSET) int offset,
                                     @Header(IntegrationMessageHeaderAccessor.DELIVERY_ATTEMPT) int deliveryAttempt) {
        NewsEvent newsEvent = message.getPayload();
        log.info("NewsEvent with id '{}' and title '{}' received from bus. topic: {}, partition: {}, offset: {}, deliveryAttempt: {}",
                newsEvent.getId(), newsEvent.getTitle(), topic, partition, offset, deliveryAttempt);

        News news = mapperFacade.map(newsEvent, News.class);

        Optional<News> optionalNews = newsService.createNews(news);
        News newSaved = optionalNews.get(); //TODO Handle ES Error
        log.info("News with id {} saved in Elasticsearch.", newSaved.getId());

        return newsEvent;
    }

}
