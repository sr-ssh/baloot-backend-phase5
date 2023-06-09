package ir.ac.ut.ie.Entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.ac.ut.ie.Exceptions.InvalidCommand;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class Provider {
    //    private Integer id;
//    private String name;
//    private Date birthDate;
//    private String nationality;
    private Integer id;
    private String name;
    private Date registryDate;

    public void update(Provider updatedActor) {
        name = updatedActor.getName();
        registryDate = updatedActor.getRegistryDate();
    }

    public ObjectNode getInformation(ObjectMapper mapper) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("providerId", id);
        objectNode.put("name", name);
        return objectNode;
    }

    public void checkForInvalidCommand() throws InvalidCommand {
        if (id==null || name==null || registryDate==null)
            throw new InvalidCommand();
    }

    public int getAge() {
        LocalDate birthDate = new java.sql.Date(this.registryDate.getTime()).toLocalDate();
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age;
    }

    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Date getRegistryDate(){
        return registryDate;
    }
}
