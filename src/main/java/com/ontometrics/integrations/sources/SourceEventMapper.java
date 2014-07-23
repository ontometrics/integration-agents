
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
import java.util.stream.Collectors;

/**
 * Created by rob on 7/11/14.
 * Copyright (c) ontometrics 2014 All rights reserved
 */
public class SourceEventMapper {

    private Logger log = LoggerFactory.getLogger(SourceEventMapper.class);
    private URL url;
    private URL editsUrl;
    private XMLEventReader eventReader;

    private ProcessEvent lastEvent;

    public SourceEventMapper(URL url) {
        this.url = url;
    }

    /**
     * Once we have this open, we should make sure that we are not resending events we have already seen.
     *
     * @return the last event that was returned to the user of this class
     */
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

    public List<EditSet> getLatestChanges(){
        List<ProcessEvent> latestEvents = getLatestEvents();
        return latestEvents.stream().map(e -> getChanges(e)).collect(Collectors.toList());
    }

    private EditSet getChanges(ProcessEvent e) {
        List<ProcessEvent> edits = new ArrayList<>();
        try {
            InputStream inputStream = editsUrl.openStream();
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = inputFactory.createXMLEventReader(inputStream);
            while (reader.hasNext()){
                XMLEvent nextEvent = reader.nextEvent();
                switch (nextEvent.getEventType()){
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = nextEvent.asStartElement();
                        String elementName = startElement.getName().getLocalPart();
                        if (elementName.equals("change")){
                            StartElement fieldTag = reader.nextEvent().asStartElement();
                            //String fieldName = fieldTag.getAttributeByName("name").getValue();






                        }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (XMLStreamException e1) {
            e1.printStackTrace();
        }
        return new EditSet.Builder().changedOn(new Date()).editor("Bozo").change(new ProcessEventChange.Builder().field("assignee").priorValue("Noura").currentValue("Rob").build()).build();
    }

    /**
     * Given that the stream should automatically do this, this might not be needed.
     *
     * @return the last event returned the last time #getLatestEvents() was called.
     */
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
        ProcessEvent event = new ProcessEvent.Builder()
                .title(currentTitle)
                .description(currentDescription)
                .link(currentLink)
                .published(currentPublishDate)
                .build();
        log.info("{}", event);
        return event;
    }

    public void setEditsUrl(URL editsUrl) {
        this.editsUrl = editsUrl;
    }
}
