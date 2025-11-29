package org.example;

import org.example.entity.Gender;
import org.example.entity.PersonID;
import org.example.entity.PersonInfo;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import static org.example.entity.Gender.F;
import static org.example.entity.Gender.M;

public class XMLParser {
    private final Map<String, PersonInfo> infoByID = new HashMap<>();
    private final Map<String, PersonInfo> infoByName = new HashMap<>();

    private int personNumber = 0;
    private XMLEventReader reader;
    private PersonInfo person = null;
    private boolean isIdentifiedByID;
    private boolean isIdentifiedByName;


    public Set<PersonInfo> parse(File file){
        XMLInputFactory factory = XMLInputFactory.newInstance();


        try {
            reader = factory.createXMLEventReader(new FileInputStream(file));

            while(reader.hasNext()) {
                XMLEvent event = reader.nextEvent();

                if (event.isStartElement()) {
                    parseStart(event);
                }

            }

            reader.close();

        } catch (XMLStreamException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (PersonInfo person : infoByID.values()) {
            person.updateLists(infoByID, infoByName);
        }

        for (PersonInfo person : infoByName.values()) {
            person.updateLists(infoByID, infoByName);
        }

        Set<PersonInfo> res = new HashSet<>(infoByID.values());
        res.addAll(infoByName.values());

        return res;
    }


    private void parseStart(XMLEvent event) throws XMLStreamException {
        StartElement startElement = event.asStartElement();
        String localPart = startElement.getName().getLocalPart();

        if (localPart.equals("people") || localPart.equals("children-number")
                || localPart.equals("siblings-number") || localPart.equals("children")) {
            return;
        }



        if (localPart.equals("Person") || localPart.equals("person")) {
            parseStartPerson(startElement);
        }
        else if (localPart.equals("id")) {
            parseID(startElement);
        }
        else if (localPart.equals("brother") || localPart.equals("sister")) {
            parseSibling(startElement);
        }
        else if (localPart.equals("son") || localPart.equals("daughter") || localPart.equals("child")) {
            parseChild(startElement);
        }
        else if (localPart.equals("siblings")) {
            parseSiblings(startElement);
        }
        else if (localPart.equals("wife") || localPart.equals("husband") || localPart.equals("spouce")) {
            parseSpouse(startElement);
        }
        else if (localPart.equals("mother") || localPart.equals("father") || localPart.equals("parent")) {
            parseParent(startElement);
        }
        else if (startElement.getName().getLocalPart().equals("fullname")) {
            parseFullname(startElement);
        }
        else if (localPart.equals("gender")) {
            parseGender(startElement);
        }
        else if (localPart.equals("surname") || localPart.equals("family-name")) {
            parseSurname(startElement);
        }
        else if (localPart.equals("firstname")) {
            parseFirstName(startElement);
        }
        else {
            System.out.println(localPart);
            throw new RuntimeException("New Name: " + localPart);
        }


    }

    private void parseStartPerson(StartElement startElement) {
        System.out.println("Number: " + Integer.toString(this.personNumber++));
        isIdentifiedByID = false;
        isIdentifiedByName = false;

        person = new PersonInfo();
        Attribute nameAtr = startElement.getAttributeByName(new QName("name"));
        if (nameAtr != null) {
            String value = nameAtr.getValue();
            value = value.replaceAll("\\s+", " ").trim();


            if (infoByName.containsKey(value)) {
                person = infoByName.get(value);
            }
            else {
                String[] name = value.split(" ");

                person.fname = name[0];
                person.lname = name[1];
                infoByName.put(value, person);
            }
            isIdentifiedByName = true;
        }

        Attribute id = startElement.getAttributeByName(new QName("id"));
        if (id != null) {
            String value = validateData(id.getValue());
            if (infoByID.containsKey(value)) {
                if (isIdentifiedByName) {
                    PersonInfo personInfoById = infoByID.get(value);
                    personInfoById.makeOne(person);
                    person = personInfoById;
                    infoByID.put(person.id, personInfoById);
                    infoByName.put(person.getFullname(), person);
                }
                else {
                    person = infoByID.get(value);
                }
            }
            else {
                person.id = value;
                infoByID.put(value, person);
            }
            isIdentifiedByID = true;
        }
    }

    private void parseID(StartElement startElement) throws XMLStreamException {
        String value = getFromAttrOrValue(startElement, "value");
        if (value == null || checkIfNot(value)) return;

        person.id = value;
        if (!isIdentifiedByID) {
            if (infoByID.containsKey(value)) {
                infoByID.get(value).makeOne(person);
                person = infoByID.get(value);
            }
            else {
                infoByID.put(value, person);
            }
        }
        isIdentifiedByID = true;

        if (isIdentifiedByName) {
            PersonInfo infoByNameCur = infoByName.get(person.getFullname());
            person.makeOne(infoByNameCur);
            infoByName.put(person.getFullname(), person);
        }
    }

    private void parseGender(StartElement startElement) throws XMLStreamException {
        String value = getFromAttrOrValue(startElement, "value");
        if (value == null || checkIfNot(value)) return;

        if (value.equals("male") || value.equals("M")) person.gender = M;
        else if (value.equals("female") || value.equals("F")) person.gender = F;
        else throw new RuntimeException("Unknown gender " + value);
    }

    private void parseSurname(StartElement startElement) throws XMLStreamException {
        String value = getFromAttrOrValue(startElement, "value");
        if (value == null || checkIfNot(value)) return;
        person.lname = value;
        String first = person.fname;
        String last = person.lname;

        collapseByDefinedName(first, last);
    }

    private void parseFirstName(StartElement startElement) throws XMLStreamException {
        String value = getFromAttrOrValue(startElement, "value");
        if (value == null || checkIfNot(value)) return;
        person.fname = value;
        String first = person.fname;
        String last = person.lname;

        collapseByDefinedName(first, last);
    }

    private void parseSiblings(StartElement startElement) {
        String value = getFromAttr(startElement, "val");
        if (value == null || checkIfNot(value)) return;
        Identificator identificator = getIdentityType(value);
        addToList(person.siblings, identificator, value);
    }

    private void parseSibling(StartElement startElement) throws XMLStreamException {
        String value = getFromAttrOrValue(startElement, "val");
        String name = startElement.getName().getLocalPart();
        if (value == null || checkIfNot(value)) return;

        Identificator identificator = getIdentityType(value);
        addToList(person.siblings, identificator, value);

        if (name.equals("brother")) addGender(value, M, identificator);
        if (name.equals("sister")) addGender(value, F, identificator);
    }

    private void parseChild(StartElement startElement) throws XMLStreamException {
        String value = getFromAttrOrValue(startElement, "id");
        String name = startElement.getName().getLocalPart();
        if (value == null || checkIfNot(value)) return;

        addToList(person.children, Identificator.ID, value);

        if (name.equals("son")) addGenderById(value, M);
        if (name.equals("daughter")) addGenderById(value, F);
    }

    private void parseSpouse(StartElement startElement) throws XMLStreamException {
        String value = validateData(getFromAttrOrValue(startElement, "value"));
        String name = startElement.getName().getLocalPart();
        if (checkIfNot(value)) return;

        Identificator identificator = getIdentityType(value);

        PersonID spouseId = person.spouse;
        if (spouseId == null) {
            spouseId = new PersonID();
            person.spouse = spouseId;
        }
        switch (identificator) {
            case U -> {}
            case ID -> spouseId.id = value;
            case fullName -> {
                String[] fullname = value.split(" ");
                if (fullname.length != 2) return;
                spouseId.fname = fullname[0];
                spouseId.lname = fullname[1];
            }
        }

        if (name.equals("wife")) addGender(value, F, identificator);
        if (name.equals("husband")) addGender(value, M, identificator);
    }

    private void parseParent(StartElement startElement) throws XMLStreamException {
        String value = validateData(getFromAttrOrValue(startElement, "value"));
        String name = startElement.getName().getLocalPart();
        if (checkIfNot(value)) return;
        Identificator identificator = getIdentityType(value);
        addToList(person.parents, identificator, value);

        if (name.equals("mother")) addGender(value, F, identificator);
        if (name.equals("father")) addGender(value, M, identificator);
    }

    private void parseFullname(StartElement startElement) throws XMLStreamException {
        String first = null;
        String last = null;
        XMLEvent event;
        for (int i = 0; i < 2; i++) {
            while (true) {
                event = reader.nextEvent();
                if (event.isStartElement()) break;
            }
            StartElement next = event.asStartElement();

            if (next.getName().getLocalPart().equals("first")) {
                first = validateData(reader.nextEvent().asCharacters().getData());
            }
            if (next.getName().getLocalPart().equals("family")) {
                last = validateData(reader.nextEvent().asCharacters().getData());
            }
        }

        collapseByDefinedName(first, last);
    }



    private String getFromAttr(StartElement startElement, String attrName) {
        Attribute attr = startElement.getAttributeByName(new QName(attrName));
        if (attr != null) {
            return validateData(attr.getValue());
        }
        return null;
    }

    private String getFromAttrOrValue(StartElement startElement, String attrName) throws XMLStreamException {
        String byAttr = getFromAttr(startElement, attrName);
        if (byAttr != null) return byAttr;
        XMLEvent event = reader.nextEvent();
        while(!event.isCharacters()) {
            event = reader.nextEvent();
        }
        return validateData(event.asCharacters().getData());
    }

    private String validateData(String data) {
        return data.strip().replaceAll("\\s+", " ");
    }

    private void addToList(List<PersonID> list, Identificator identificator, String value) {
        switch (identificator) {
            case fullName -> {
                PersonID personID = new PersonID();
                String[] fullname = value.split(" ");
                personID.fname = fullname[0];
                personID.lname = fullname[1];
                list.add(personID);
            }
            case ID -> {
                PersonID personID = new PersonID();
                personID.id = value;
                list.add(personID);
            }
            case U -> {}
        }
    }

    private Identificator getIdentityType(String str) {
        if (str.matches("P[0-9]+")) {
            return Identificator.ID;
        }
        if (str.matches("[a-zA-Z]+[ ]*[a-zA-Z]+")) {
            return Identificator.fullName;
        }

        return Identificator.U;
    }

    private boolean checkIfNot(String str) {
        return str.compareTo("UNKNOWN") == 0;
    }

    private void addGenderById(String id, Gender gender) {
        if (infoByID.containsKey(id)) {
            infoByID.get(id).gender = gender;
        }
        else {
            PersonInfo info = new PersonInfo();
            info.gender = gender;
            info.id = id;
            infoByID.put(id, info);
        }
    }

    private void addGenderByName(String fullname, Gender gender) {
        if (infoByName.containsKey(fullname)) {
            infoByName.get(fullname).gender = gender;
        }
        else {
            PersonInfo info = new PersonInfo();
            String[] names = fullname.split(" ");
            info.fname = names[0];
            info.lname = names[1];
            info.gender = gender;
            infoByName.put(fullname, info);
        }
    }

    private void addGender(String feature, Gender gender, Identificator identificator) {
        if (identificator == Identificator.ID) addGenderById(feature, gender);
        if (identificator == Identificator.fullName) addGenderByName(feature, gender);
    }


    private void collapseByDefinedName(String first, String last) {
        if (first != null && last != null) {
            String fullname = first + " " + last;
            if (isIdentifiedByName) return;

            infoByName.put(fullname, person);
            isIdentifiedByName = true;

            if (isIdentifiedByID) {
                if (infoByName.containsKey(fullname)) {
                    person.makeOne(infoByName.get(fullname));
                    infoByName.put(fullname, person);
                }
                else {
                    person.lname = last;
                    person.fname = first;
                    infoByName.put(fullname, person);
                }
            }
            else {
                person.lname = last;
                person.fname = first;
                infoByName.put(fullname, person);
            }
        }
    }
}
