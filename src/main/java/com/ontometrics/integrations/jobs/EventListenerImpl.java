package com.ontometrics.integrations.jobs;

import com.ontometrics.integrations.sources.ChannelMapper;
import com.ontometrics.integrations.sources.ProcessEvent;
import com.ontometrics.integrations.sources.SourceEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.List;

/**
 * Created on 8/18/14.
 */
public class EventListenerImpl implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(EventListenerImpl.class);

    public static final String TOKEN = "xoxp-2427064028-2427064030-2467602952-3d5dc6";
    public static final String TOKEN_KEY = "token";
    public static final String TEXT_KEY = "text";
    public static final String CHANNEL_KEY = "channel";
    private final URL sourceURL;
    private final ChannelMapper channelMapper;
    public static final String SLACK_URL = "https://slack.com/api/";
    public static final String CHANNEL_POST_PATH = "chat.postMessage";

    public EventListenerImpl(URL sourceURL, ChannelMapper channelMapper) {
        this.sourceURL = sourceURL;
        this.channelMapper = channelMapper;
        if(sourceURL == null || channelMapper == null) throw new IllegalArgumentException("You must provide sourceURL and channelMapper.");
    }

    @Override
    public int checkForNewEvents() {
        //get events
        SourceEventMapper sourceEventMapper = new SourceEventMapper(sourceURL);
        List<ProcessEvent> events = sourceEventMapper.getLatestEvents();

        events.stream().forEach(e -> postEventToChannel(e, channelMapper.getChannel(e)));
        return events.size();
    }

    private void postEventToChannel(ProcessEvent event, String channel){
        log.info("posting event {}.", event.toString());
        Client client = ClientBuilder.newClient();

        WebTarget slackApi = client.target(SLACK_URL).path(CHANNEL_POST_PATH)
                .queryParam(TOKEN_KEY, TOKEN)
                .queryParam(TEXT_KEY, getText(event))
                .queryParam(CHANNEL_KEY, "#" + channel);

        Invocation.Builder invocationBuilder = slackApi.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        log.info("response code: {} response: {}", response.getStatus(), response.readEntity(String.class));

    }

    private String getText(ProcessEvent event){
        StringBuilder builder = new StringBuilder();
        String title = event.getTitle();
        title = title.replace(event.getID(), "");
        builder.append("<").append(event.getLink()).append("|").append(event.getID()).append(">").append(title);
        return builder.toString();
    }
}
