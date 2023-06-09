package ir.ac.ut.ie.Controllers;

import ir.ac.ut.ie.DataBase;
import ir.ac.ut.ie.Entities.Commodity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class CommoditiesController {

    @RequestMapping(value = "/getCommodities", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Commodity[] getCommodities(
            @RequestParam(value = "defaultSort") String defaultSort,
            @RequestParam(value = "searchBy", required = false) String searchBy,
            @RequestParam(value = "searchValue", required = false) String searchValue) throws IOException, InterruptedException {

        if(searchValue == null)
            searchValue ="";
        if(searchBy == null)
            searchBy ="";
        Map<Integer, Commodity> commoditiesMap = DataBase.getInstance().commoditiesToShow(Boolean.parseBoolean(defaultSort), searchBy, searchValue);
        Commodity[] commodities = commoditiesMap.values().toArray(new Commodity[0]);
        TimeUnit.SECONDS.sleep(3);
        return commodities;
    }
}


