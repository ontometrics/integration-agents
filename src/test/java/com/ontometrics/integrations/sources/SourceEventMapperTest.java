package com.ontometrics.integrations.sources;

import com.ontometrics.test.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class SourceEventMapperTest {

    private static final Logger log = LoggerFactory.getLogger(SourceEventMapperTest.class);

    private URL sourceUrl;

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


}