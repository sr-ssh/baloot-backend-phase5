package ir.ac.ut.ie.Entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.ac.ut.ie.DataBase;
import ir.ac.ut.ie.Exceptions.*;

import java.util.*;

public class User {
    //    private String email;
//    private String password;
//    private String nickname;
//    private String name;
//    private Date birthDate;
//    private Set<Integer> watchList = new HashSet<>();
    private String username;
    private String password;
    private String email;
    private Date birthDate;
    private String address;
    private Integer credit;
    private Set<Integer> buyList = new HashSet<>();
    private List<String> usedDiscounts = new ArrayList<>();
    private Discount currentDiscount;

    public void addToBuyList(Integer commodityId, int inStock) throws Exception {
        commodityAlreadyExists(commodityId);
        stockLimitError(inStock);
        buyList.add(commodityId);
    }

    public void commodityAlreadyExists(Integer commodityId) throws JsonProcessingException, CommodityAlreadyExists {
        if (buyList.contains(commodityId))
            throw new CommodityAlreadyExists();
    }

    public void stockLimitError(int inStock) throws Exception {
        if (0 >= inStock)
            throw new StockLimitError();
    }

    public void removeFromBuyList(Integer commodityId) throws Exception {
        if (!buyList.contains(commodityId))
            throw new CommodityNotFound();
        buyList.remove(commodityId);
    }

    public void addCredit(Integer credit) throws Exception {
        if (credit < 0)
            throw new InvalidCredit();
        this.credit += credit;
    }

    public Integer getBuyListPrice() throws Exception{
        int totalPrice = 0;
        for (Integer commodityId: buyList) {
            Commodity commodity = DataBase.getInstance().getCommodityById(commodityId);
            totalPrice += commodity.getPrice();
        }
        if (checkDiscount()) {
            Float tmpPrice = (float)(totalPrice);
            Float discount = (float)(currentDiscount.getDiscount());
            totalPrice = (int)(tmpPrice * (1 - discount/100));
        }
        return totalPrice;
    }
    private boolean checkDiscount() throws Exception {
        if (currentDiscount != null){
            if (usedDiscounts.contains(currentDiscount.getDiscountCode())){
                throw new DiscountAlreadyUsed();
            }
            return true;
        }
        return false;
    }

    public void payment() throws Exception  {
        int totalBuyListPrice = getBuyListPrice();
        if (totalBuyListPrice > credit)
            throw new NotEnoughCredit();
        for (Integer commodityId: buyList) {
            Commodity commodity = DataBase.getInstance().getCommodityById(commodityId);
            if (commodity.getInStock() < 1)
                throw new StockLimitError();
        }
        for (Integer commodityId: buyList) {
            Commodity commodity = DataBase.getInstance().getCommodityById(commodityId);
            commodity.reduceStock();
        }
        credit -= totalBuyListPrice;
        buyList.clear();
        if (currentDiscount != null){
            usedDiscounts.add(currentDiscount.getDiscountCode());
            currentDiscount = null;
        }
    }

    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getAddress() {
        return address;
    }
    public Integer getCredit() {
        return credit;
    }
    //    public String getNickname() {
//        return nickname;
//    }
//
//    public String getName() {
//        return name;
//    }
    public Date getBirthDate() {
        return birthDate;
    }
    public Set<Integer> getBuyList() {
        return buyList;
    }

    public void setCurrentDiscount(Discount currentDiscount) throws Exception{
        if (usedDiscounts.contains(currentDiscount.getDiscountCode())){
            throw new DiscountAlreadyUsed();
        }
        this.currentDiscount = currentDiscount;
    }
    public void setName(String name) {
        this.username = name;
    }
}
