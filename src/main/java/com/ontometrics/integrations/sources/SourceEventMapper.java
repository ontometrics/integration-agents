
package com.ontometrics.integrations.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
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

/**
 * Created by rob on 7/11/14.
 * Copyright (c) ontometrics 2014 All rights reserved
 */
public class SourceEventMapper {

    private Logger log = LoggerFactory.getLogger(SourceEventMapper.class);
    private URL url;
    private XMLEventReader eventReader;

    private ProcessEvent lastEvent;

    public SourceEventMapper(URL url) {
        this.url = url;
    }

    public List<ProcessEvent> getLatestEvents(){
        List<ProcessEvent> events = new ArrayList<>();
        try {
            InputStream inputStream = url.openStream();
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            eventReader = inputFactory.createXMLEventReader(inputStream);

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
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
        lastEvent = events.get(events.size()-1);
        return events;
    }

    public ProcessEvent getLastEvent() {
        return lastEvent;
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
