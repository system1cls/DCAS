package org.example;

import org.example.entity.Gender;
import org.example.entity.PersonID;
import org.example.entity.PersonInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        XMLParser parser = new XMLParser();
        Set<PersonInfo> info = parser.parse(new File("C:\\Users\\syste\\IdeaProjects\\XML1\\src\\main\\resources\\people.xml"));
        Writer.writeToFile(new File("C:\\Users\\syste\\IdeaProjects\\XML1\\src\\main\\resources\\res.xml"), info);

    }

    public static void printData(Set<PersonInfo> data) {
        StringBuilder builder = new StringBuilder();
        for (PersonInfo info : data) {
            builder.append("-------------\n");
            if (info.id != null) builder.append("id: ").append(info.id).append("\n");
            if (info.getFullname() != null) builder.append("name: ").append(info.getFullname()).append("\n");
            switch (info.gender) {
                case U -> {}
                case F -> builder.append("Gender is ").append("female").append("\n");
                case M -> builder.append("Gender is ").append("male").append("\n");
            }
            if (info.spouseInfo != null) {
                if (info.spouseInfo.gender == Gender.M) {
                    addPersonID(builder, info.spouseInfo, "husband");
                }
                else if (info.spouseInfo.gender == Gender.F) {
                    addPersonID(builder, info.spouseInfo, "wife");
                }
                else {
                    addPersonID(builder, info.spouseInfo, "spouse");
                }
            }
            if (info.childrenSet != null)  {
                for (PersonInfo childrenInfo : info.childrenSet) {
                    addPersonID(builder, childrenInfo, childrenInfo.getFullname());
                }
            }
            if (info.siblingsSet != null)  {
                for (PersonInfo sibling : info.siblingsSet) {
                    if (sibling.gender == Gender.M) {
                        addPersonID(builder, sibling, "brother " + sibling.getFullname());
                    }
                    else if (sibling.gender == Gender.F) {
                        addPersonID(builder, sibling, "sister " + sibling.getFullname());
                    }
                    else {
                        addPersonID(builder, sibling, sibling.getFullname());
                    }
                }
            }
            if (info.parentsSet != null)  {
                for (PersonInfo parent : info.parentsSet) {
                    if (parent.gender == Gender.M) {
                        addPersonID(builder, parent, "father " + parent.getFullname());
                    }
                    else if (parent.gender == Gender.F) {
                        addPersonID(builder, parent, "mother " + parent.getFullname());
                    }
                    else {
                        addPersonID(builder, parent, parent.getFullname());
                    }
                }
            }


            builder.append("-------------\n");
        }

        System.out.println(builder.toString());
    }

    private static void addPersonID(StringBuilder builder, PersonID id, String prefix) {
        builder.append(prefix).append(" id: ").append(id.id).append("\n");
        builder.append(prefix).append(" name: ").append(id.getFullname()).append("\n");
    }
}