package org.example.entity;

import java.util.*;

public class PersonInfo extends  PersonID {
    public Gender gender = Gender.U;
    public PersonID spouse = null;
    public final List<PersonID> parents = new ArrayList<>();
    public List<PersonID> children = new ArrayList<>();
    public List<PersonID> siblings = new ArrayList<>();

    public Set<PersonInfo> parentsSet = new HashSet<>();
    public Set<PersonInfo> childrenSet = new HashSet<>();
    public Set<PersonInfo> siblingsSet = new HashSet<>();
    public PersonInfo spouseInfo = null;

    private boolean isUpdated = false;

    public void makeOne(PersonInfo info) {
        if (this.id == null) this.id = info.id;
        if (this.fname == null) this.fname = info.fname;
        if (this.lname == null) this.lname = info.lname;
        if (this.gender == Gender.U) this.gender = info.gender;
        if (spouse == null) this.spouse = info.spouse;
        this.parents.addAll(info.parents);
        this.children.addAll(info.children);
        this.siblings.addAll(info.siblings);
    }

    public void updateLists(Map<String, PersonInfo> personByID, Map<String, PersonInfo> personByName) {
        if (isUpdated) return;

        if (spouse != null) {
            if (this.spouse.id != null && personByID.containsKey(spouse.id)) {
                this.spouseInfo = personByID.get(spouse.id);
            } else if (this.spouse.getFullname() != null && personByName.containsKey(spouse.getFullname())) {
                this.spouseInfo = personByName.get(spouse.getFullname());
            }
        }

        updateList(this.parents, parentsSet, personByID, personByName);
        updateList(this.siblings, siblingsSet, personByID, personByName);
        updateList(this.children, siblingsSet, personByID, personByName);

        isUpdated = true;
    }

    private void updateList(List<PersonID> list, Set<PersonInfo> personInfos, Map<String, PersonInfo> personByID, Map<String, PersonInfo> personByName) {
        for (PersonID id : list) {
            PersonInfo info = null;
            if (id.id != null && personByID.containsKey(id.id)) {
                info = personByID.get(id.id);
            }
            else if (id.getFullname() != null && personByName.containsKey(id.getFullname())) {
                info = personByName.get(id.getFullname());
            }

            if (info != null && !personInfos.contains(info)) {
                personInfos.add(info);
            }
        }
    }
}
