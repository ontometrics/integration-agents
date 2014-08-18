package com.ontometrics.integrations.jobs;

import com.ontometrics.integrations.sources.ChannelMapper;
import com.ontometrics.test.util.TestUtil;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created on 8/18/14.
 */
public class EventListenerTest {

    private EventListenerImpl eventListener;

    @Before
    public void setUp() throws Exception {
        URL sourceURL =  TestUtil.getFileAsURL("/feeds/issues-feed-rss.xml");
        ChannelMapper channelMapper = new ChannelMapper.Builder()
                .defaultChannel("process")
//                .addMapping("ASOC", "vixlet")
                .addMapping("DMAN", "dminder")
                .build();

        eventListener = new EventListenerImpl(sourceURL, channelMapper);

    }

    @Test
    public void testGettingNewEvents() throws Exception {

        assertThat(eventListener.checkForNewEvents(), is(50));
    }
}
