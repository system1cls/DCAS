package org.example;

import org.example.entity.Gender;
import org.example.entity.PersonInfo;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Writer {

    public static void writeToFile(File file, Set<PersonInfo> info) {
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(new FileWriter(file));

            writer.writeStartDocument();
            writer.writeCharacters("\n");

            writer.writeStartElement("people");
            writer.writeAttribute("cnt", Integer.toString(info.size()));
            for (PersonInfo person : info) {
                writePerson(writer, person);
            }
            writer.writeCharacters("\n");
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writePerson(XMLStreamWriter writer, PersonInfo info) throws XMLStreamException {
        writer.writeCharacters("\n\t");
        writer.writeStartElement("person");
        if (info.id != null) writer.writeAttribute("id", info.id);
        if (info.fname != null) {
            writeElem(writer, "firstname", info.fname);
        }
        if (info.lname != null) {
            writeElem(writer, "lastname", info.lname);
        }
        writeGender(writer, info);

        writeSpouse(writer, info);
        writeParents(writer, info);
        writeChildren(writer, info);

        writer.writeCharacters("\n\t");
        writer.writeEndElement();
    }

    private static void writeElem(XMLStreamWriter writer, String elemName, String value) throws XMLStreamException {
        writer.writeCharacters("\n\t\t");
        writer.writeStartElement(elemName);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }

    private static void writeGender(XMLStreamWriter writer, PersonInfo info) throws XMLStreamException {
        switch(info.gender) {
            case F -> writeElem(writer, "gender", "female");
            case M -> writeElem(writer, "gender", "male");
            case U -> {}
        }
    }


    private static void writeSpouse(XMLStreamWriter writer, PersonInfo info) throws XMLStreamException {
        if (checkForIdentification(info.spouseInfo)) {
            String identification = getIdentification(info.spouseInfo);

            Gender spouseGender = info.spouseInfo.gender;
            switch (spouseGender) {
                case F -> writeElem(writer, "wife", identification);
                case M -> writeElem(writer, "husband", identification);
                case U -> writeElem(writer, "spouse", identification);
            }

        }
    }


    private static void writeParents(XMLStreamWriter writer, PersonInfo info) throws XMLStreamException {

        for (PersonInfo parent : info.parentsSet) {
            if (checkForIdentification(parent)) {
                String identification = getIdentification(parent);

                Gender parentGender = parent.gender;
                switch (parentGender) {
                    case F -> writeElem(writer, "mother", identification);
                    case M -> writeElem(writer, "father", identification);
                    case U -> writeElem(writer, "parent", identification);
                }
            }
        }
    }

    private static void writeSiblings(XMLStreamWriter writer, PersonInfo info) throws XMLStreamException {
        for (PersonInfo sibling : info.siblingsSet) {
            if (checkForIdentification(sibling)) {
                String identification = getIdentification(sibling);

                Gender parentGender = sibling.gender;
                switch (parentGender) {
                    case F -> writeElem(writer, "sister", identification);
                    case M -> writeElem(writer, "brother", identification);
                    case U -> writeElem(writer, "sibling", identification);
                }
            }
        }
    }

    private static void writeChildren(XMLStreamWriter writer, PersonInfo info) throws XMLStreamException {
        for (PersonInfo child : info.childrenSet) {
            if (checkForIdentification(child)) {
                String identification = getIdentification(child);

                Gender parentGender = child.gender;
                switch (parentGender) {
                    case F -> writeElem(writer, "daughter", identification);
                    case M -> writeElem(writer, "son", identification);
                    case U -> writeElem(writer, "child", identification);
                }
            }
        }
    }


    private static boolean checkForIdentification(PersonInfo info) {
        return info!= null && (info.getFullname() != null || info.id != null);
    }

    private static String getIdentification(PersonInfo info) {
        return info.getFullname() != null ? info.getFullname() : info.id;
    }
}
