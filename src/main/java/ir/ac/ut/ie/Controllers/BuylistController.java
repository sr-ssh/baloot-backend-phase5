package ir.ac.ut.ie.Controllers;

import ir.ac.ut.ie.DataBase;
import ir.ac.ut.ie.Entities.Commodity;
import ir.ac.ut.ie.Entities.User;
import ir.ac.ut.ie.Exceptions.StockLimitError;
import ir.ac.ut.ie.Exceptions.CommodityAlreadyExists;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@RestController
public class BuylistController extends HttpServlet {
    private Commodity[] getBuylist(String userId) throws Exception {
        Set<Integer> commodityIds = DataBase.getInstance().getUsers().get(userId).getBuyList();
        Commodity[] BuyList = new Commodity[commodityIds.size()];
        int i=0;
        for (Integer id:commodityIds) {
            BuyList[i] = DataBase.getInstance().getCommodityById(id);
            i++;
        }
        return BuyList;
    }

    @RequestMapping(value = "/getBuylist/{userId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Commodity[] getUser(@PathVariable(value = "userId") String userId) throws Exception {
        TimeUnit.SECONDS.sleep(3);
        return getBuylist(userId);
    }

    @RequestMapping(value = "/addToBuylist/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public String addToBuylist(
            @PathVariable(value = "userId") String userId,
            @RequestParam(value = "CommodityId") Integer commodityId,
            @RequestParam(value = "inStock") Integer inStock) throws InterruptedException, IOException{
        TimeUnit.SECONDS.sleep(3);
        try {
            DataBase.getInstance().getUsers().get(userId).addToBuyList(commodityId, inStock);
            return "Commodity Added To Buylist Successfully";
        } catch (CommodityAlreadyExists e1) {
            return e1.getMessage();
        } catch (StockLimitError e2) {
            return e2.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/deleteFromBuylist/{userId}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Commodity[] deleteFromBuylist(
            @PathVariable(value = "userId") String userId,
            @RequestParam(value = "commodityId") Integer commodityId) throws Exception {
        DataBase.getInstance().getUsers().get(userId).removeFromBuyList(commodityId);
        TimeUnit.SECONDS.sleep(3);
        return getBuylist(userId);
    }

    @RequestMapping(value = "/getRecommendedCommodities/{userId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Commodity[] getRecommendedCommodities(@PathVariable(value = "userId") String userId) throws Exception {
        List <Integer> recommended_commodities = new ArrayList<>();
        List <Integer> commodityId_byScore = new ArrayList<>();
        List <Integer> scores = new ArrayList<>();
        User current_user = DataBase.getInstance().getUsers().get(userId);
        for (Commodity commodity : DataBase.getInstance().getCommodities().values()) {
            int category_similarity_score = 0;
            for (Integer commodityId_in_BuyList : current_user.getBuyList()) {
                Commodity commodity_in_BuyList = DataBase.getInstance().getCommodityById(commodityId_in_BuyList);
                ArrayList <String> temp_list = new ArrayList<>(commodity.getCategories());
                temp_list.retainAll(commodity_in_BuyList.getCategories());
                category_similarity_score += temp_list.size();
            }
            scores.add((int) (3 * category_similarity_score + /*+ commodity.getImdbRate() +*/ commodity.getRating()));
            commodityId_byScore.add(commodity.getId());
            commodity.setScore((int) (3 * category_similarity_score /* commodity.getImdbRate() +*/ + commodity.getRating()));
        }

        while (commodityId_byScore.size() != 0) {
            int max_score_index = scores.indexOf(Collections.max(scores));
            recommended_commodities.add(commodityId_byScore.get(max_score_index));
            scores.remove(max_score_index);
            commodityId_byScore.remove((max_score_index));
        }
        Commodity[] finalList = new Commodity[3];
        for (int i=0; i<3; i++)
            finalList[i] = DataBase.getInstance().getCommodityById(recommended_commodities.get(i));
        TimeUnit.SECONDS.sleep(3);
        return finalList;
    }
}


