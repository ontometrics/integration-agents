package com.ontometrics.integrations.sources;

import com.ontometrics.test.util.TestUtil;
import com.ontometrics.util.DateBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Calendar.JULY;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class SourceEventMapperTest {

    private static final Logger log = LoggerFactory.getLogger(SourceEventMapperTest.class);

    private URL sourceUrl;
    private XMLEventReader eventReader;

    @Before
    public void setup(){
        sourceUrl = TestUtil.getFileAsURL("/feeds/issues-feed-rss.xml");
    }

    @Test
    public void testGettingLocalFileAsUrlWorks(){
        assertThat(sourceUrl, notNullValue());
    }

    @Test
    public void testThatWeCanReadFromFile() throws IOException, XMLStreamException {
        int startElementCount = 0;
        int endElementCount = 0;
        InputStream inputStream = sourceUrl.openStream();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(inputStream);

        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    log.info("start element");

                    startElementCount++;
                case XMLStreamReader.ATTRIBUTE:
                    log.info("end element");
                    endElementCount++;
            }
        }
        assertThat(startElementCount, is(not(0)));
        assertThat(endElementCount, is(equalTo(startElementCount)));
    }

    @Test
    public void testThatWeCanReadAsEventStream() throws IOException, XMLStreamException {
        InputStream inputStream = sourceUrl.openStream();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(inputStream);

        while (eventReader.hasNext()) {
            log.info("event: {}", eventReader.nextEvent());
        }
    }

    @Test
    public void testCanParseDate() {
        Date date = new DateBuilder().day(14).month(JULY).year(2014).hour(16).minutes(41).seconds(3).build();
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zz");

        log.info("date: {}", df.format(date));
    }

    @Test
    public void testThatWeCanExtractYouTrackEvent() throws IOException, XMLStreamException {
        InputStream inputStream = sourceUrl.openStream();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        eventReader = inputFactory.createXMLEventReader(inputStream);

        List<ProcessEvent> events = new ArrayList<>();
        while (eventReader.hasNext()) {
            XMLEvent nextEvent = eventReader.nextEvent();
            switch (nextEvent.getEventType()){
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = nextEvent.asStartElement();
                    String elementName = startElement.getName().getLocalPart();
                    if (elementName.equals("item")){
                        events.add(extractEventFromStream());
                    }
            }
        }

        ProcessEvent firstEvent = events.get(0);

        assertThat(firstEvent.getTitle(), is("ASOC-28: User searches for Users by name"));
        assertThat(firstEvent.getLink(), is("http://ontometrics.com:8085/issue/ASOC-28"));
        assertThat(firstEvent.getDescription(), is("<table> <tr> <th>Reporter</th> <td> <img src=\"http://ontometrics.com:8085/_classpath/smartui/img/youPicture.gif\" width=\"56\" height=\"59\" alt=\"Tim Fulmer (timfulmer)\" title=\"Tim Fulmer (timfulmer)\"/> Tim Fulmer (timfulmer) </td> </tr> <tr> <th>Created</th> <td>Apr 15, 2014 9:05:53 AM</td> </tr> <tr> <th>Updated</th> <td>Jul 14, 2014 9:41:03 AM</td> </tr> <tr> <th>Priority</th> <td>Normal</td> </tr> <tr> <th>Type</th> <td>Feature</td> </tr> <tr> <th>State</th> <td>Open</td> </tr> <tr> <th>Assignee</th> <td>Noura Hassan (noura)</td> </tr> <tr> <th>Subsystem</th> <td>No subsystem</td> </tr> <tr> <th>Fix versions</th> <td>1.0.0</td> </tr> <tr> <th>Affected versions</th> <td>Unknown</td> </tr> <tr> <th>Fixed in build</th> <td>Next Build</td> </tr> </table> <div class=\"wiki text\">User can search for other Users by name, first screenshot. Users can follow in the search results. Tapping on a search result shows the selected User&#39;s profile page.<br/><br/>API service call:<br/><br/><a href=\"http://devvixletapi-env.elasticbeanstalk.com/#!/search/_get_0\">http://devvixletapi-env.elasticbeanstalk.com/#!/search/_get_0</a></div>"));

        log.info("size of events: {}", events.size());
        assertThat(events.size(), is(not(0)));
    }

    private ProcessEvent extractEventFromStream() {
        String currentTitle = "", currentLink = "", currentDescription = "";
        Date currentPublishDate = null;
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

        try {
            eventReader.nextEvent();
            StartElement titleTag = eventReader.nextEvent().asStartElement(); // start title tag
            if ("title".equals(titleTag.getName().getLocalPart())){
                currentTitle = eventReader.getElementText();
                eventReader.nextEvent(); // eat end tag
                eventReader.nextEvent();
                currentLink = eventReader.getElementText();
                eventReader.nextEvent(); eventReader.nextEvent();
                currentDescription = eventReader.getElementText().replace("\n", "").trim();
                eventReader.nextEvent(); eventReader.nextEvent();
                currentPublishDate = df.parse(eventReader.getElementText());
            }
        } catch (XMLStreamException | ParseException e) {
            e.printStackTrace();
        }
        ProcessEvent event = new ProcessEvent.Builder().title(currentTitle).description(currentDescription).link(currentLink).published(currentPublishDate).build();
        log.info("{}", event);
        return event;
    }


}