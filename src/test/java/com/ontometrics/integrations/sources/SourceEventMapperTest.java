package com.ontometrics.integrations.sources;

import com.ontometrics.test.util.TestUtil;
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
    public void testThatWeCanExtractYouTrackEvent() throws IOException, XMLStreamException {
        InputStream inputStream = sourceUrl.openStream();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        eventReader = inputFactory.createXMLEventReader(inputStream);

        while (eventReader.hasNext()) {
            XMLEvent nextEvent = eventReader.nextEvent();
            switch (nextEvent.getEventType()){
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = nextEvent.asStartElement();
                    String elementName = startElement.getName().getLocalPart();
                    if (elementName.equals("item")){
                        extractEventFromStream();
                    }


            }

        }

    }

    private void extractEventFromStream() {
        String currentTitle = "", currentLink = "", currentDescription = "", currentPublishDate = "";
        try {
            XMLEvent nextEvent = eventReader.nextEvent();
            log.info("next tag in extractor: {}", nextEvent);
            StartElement titleTag = eventReader.nextEvent().asStartElement(); // start title tag
            if ("title".equals(titleTag.getName().getLocalPart())){
                currentTitle = eventReader.getElementText();
                eventReader.nextEvent(); // eat end tag
                eventReader.nextEvent();
                currentLink = eventReader.getElementText();
                eventReader.nextEvent(); eventReader.nextEvent();
                currentDescription = eventReader.getElementText();
                eventReader.nextEvent(); eventReader.nextEvent();
                currentPublishDate = eventReader.getElementText();
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        log.info("found: {} {} {} {}", currentTitle, currentLink, currentDescription, currentPublishDate);

    }


}