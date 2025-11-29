package org.example.entity;

public class PersonID {
    public String id = null;
    public String fname = null;
    public String lname = null;

    public String getFullname() {
        if (fname == null || lname == null) return null;
        return fname + " " + lname;
    }
}
