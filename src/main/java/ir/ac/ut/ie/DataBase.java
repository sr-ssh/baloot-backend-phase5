package ir.ac.ut.ie;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.ac.ut.ie.Entities.*;
import ir.ac.ut.ie.Exceptions.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DataBase {
    private static DataBase instance;
    static private ObjectMapper mapper;
    static private String host;
    private static Map<Integer, Provider> existingProviders;
    private static HashMap<Integer, Commodity> commodities;
    private static HashMap<Integer, Commodity> commoditiesSortedByDate;
    private static Map<Integer, Commodity> commoditiesNotSorted;
    private static HashMap<Integer, Commodity> searchedCommodities;
    private static Map<String, User> users;
    private static Map<String, User> usersUsername;

    private static Map<Integer, Comment> comments;
    private static Integer commentId;
    private static Map<String, Discount> discounts;


    private DataBase() throws IOException {
        mapper = new ObjectMapper();
        host = "http://5.253.25.110:5000";
        existingProviders = new HashMap<>();
        commodities = new LinkedHashMap<>();
        commoditiesNotSorted = new HashMap<>();
//        commoditiesSortedByDate = new LinkedHashMap<>();
        searchedCommodities = new LinkedHashMap<>();
        users = new HashMap<>();
        usersUsername = new HashMap<>();
        comments = new HashMap<>();
        commentId = 1;
        discounts = new HashMap<>();
        setInformation();
    }

    public static DataBase getInstance() throws IOException {
        if (instance == null)
            instance = new DataBase();
        return instance;
    }

    static private String getConnection(String path) throws IOException {
        URL url = new URL(host + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();
        return line;
    }

    private void setInformation() {
        try {
            setProvidersList();
            setCommoditiesList();
            setUsersList();
            setCommentsList();
            setDiscountList();
        }
        catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    static void setCommoditiesList() throws Exception {
        String data = getConnection("/api/commodities");
        System.out.println(data);
        Commodity[] commoditiesList = mapper.readValue(data, Commodity[].class);
//        Map<Integer, Commodity> commoditiesNotSorted = new HashMap<>();
        for (Commodity commodity: commoditiesList) {
            System.out.println(commodity.getRating());
            commodity.checkForInvalidCommand();
            checkProviderExist(commodity.getProviderId());

            commodity.setProviderName(existingProviders.get(commodity.getProviderId()).getName());

            if (commoditiesNotSorted.containsKey(commodity.getId()))
                commoditiesNotSorted.get(commodity.getId()).update(commodity);
            else {
                commodity.initialValues();
                commoditiesNotSorted.put(commodity.getId(), commodity);
            }
        }
        System.out.println("test rating");
        for (Commodity commodity: commoditiesNotSorted.values()) {
            System.out.println(commodity.getRating());
        }
//        sortMoviesByRate(commoditiesNotSorted);
        for (Commodity commodity: commodities.values()) {
            System.out.println(commodity.getRating());
        }

//        commoditiesSortedByDate = sortMoviesByDate(commodities);
    }

    private static void sortCommoditiesByRate(Map<Integer, Commodity> commoditiesNotSorted) {
        List list = new LinkedList(commoditiesNotSorted.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) (((Commodity)((Map.Entry) (o2)).getValue())).getRating()).compareTo((((Commodity)((Map.Entry) (o1)).getValue())).getRating());
            }
        });
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            commodities.put((Integer) entry.getKey(), (Commodity) entry.getValue());
        }
    }

    private static HashMap<Integer, Commodity> sortCommoditiesByPrice(Map<Integer, Commodity> commoditiesNotSorted) {
        commodities.clear();
        HashMap<Integer, Commodity> sorted = new HashMap<>();
        List list = new LinkedList(commoditiesNotSorted.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) (((Commodity)((Map.Entry) (o2)).getValue())).getPrice()).compareTo((((Commodity)((Map.Entry) (o1)).getValue())).getPrice());
            }
        });
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            commodities.put((Integer) entry.getKey(), (Commodity) entry.getValue());
        }
        return sorted;
    }

//    private static HashMap<Integer, Commodity> sortCommoditiesByDate(HashMap<Integer, Commodity> moviesToSort) {
//        List list = new LinkedList(moviesToSort.entrySet());
//        HashMap<Integer, Commodity> sorted = new LinkedHashMap<>();
//        Collections.sort(list, new Comparator() {
//            public int compare(Object o1, Object o2) {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                try {
//                    return sdf.parse(((Commodity) ((Map.Entry) o1).getValue()).getReleaseDate()).before(sdf.parse(((Commodity) ((Map.Entry) o2).getValue()).getReleaseDate())) ? 1 : -1;
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                    return 0;
//                }
//            }
//        });
//        for (Iterator it = list.iterator(); it.hasNext();) {
//            Map.Entry entry = (Map.Entry) it.next();
//            sorted.put((Integer) entry.getKey(), (Commodity) entry.getValue());
//        }
//        return sorted;
//    }

    private void setProvidersList() throws Exception {
        String data = getConnection("/api/providers");
        Provider[] providersList = mapper.readValue(data, Provider[].class);
        for (Provider provider: providersList) {
            provider.checkForInvalidCommand();
            if (existingProviders.containsKey(provider.getId()))
                existingProviders.get(provider.getId()).update(provider);
            else
                existingProviders.put(provider.getId(), provider);
        }
    }

    private void setUsersList() throws Exception {
        String data = getConnection("/api/users");
        User[] usersList = mapper.readValue(data, User[].class);
        for (User user: usersList) {
            checkUserExist(user.getEmail());
            users.put(user.getEmail(), user);
            usersUsername.put(user.getUsername(), user);
        }
    }

    private void setCommentsList() throws Exception {
        String data = getConnection("/api/comments");
        Comment[] commentsList = mapper.readValue(data, Comment[].class);
        for(Comment comment: commentsList) {
            comment.checkForInvalidCommand();
            userNotFoundWithUserEmail(comment.getUserEmail());
            commodityNotFound(comment.getCommodityId());
            commoditiesNotSorted.get(comment.getCommodityId()).addComment(comment, commentId);
            comments.put(commentId, comment);
            commentId += 1;
        }
    }
    private void setDiscountList() throws Exception {
        String data = getConnection("/api/discount");
        Discount[] discountList = mapper.readValue(data, Discount[].class);
        for(Discount discount: discountList) {
            discount.checkForInvalidCommand();
            discounts.put(discount.getDiscountCode(), discount);
        }
    }

    public Provider getProviderById(Integer id) throws Exception {
        return existingProviders.get(id);
    }

    public ArrayList<Commodity> getCommoditiesFromProvider(Integer providerId) {
        ArrayList<Commodity> commodities_provided = new ArrayList<>();
        for (Commodity commodity:commodities.values())
            if(commodity.getProviderId() == providerId)
            commodities_provided.add(commodity);
        return commodities_provided;
    }

    public User getAuthenticatedUser(String username, String password) {
        if (users.containsKey(username)) {
            if (users.get(username).getPassword().equals(password))
                return users.get(username);
        }
        User errorUser = new User();
        errorUser.setName("error");
        return errorUser;
    }

    public String findEmailFromUsername(String username) throws Exception {
        return usersUsername.get(username).getEmail();
    }

    public void addComment(String username, Integer commodityId, String text) throws Exception {
        Comment new_comment = new Comment(commentId);
        new_comment.setUserEmail(usersUsername.get(username).getEmail());
        new_comment.setCommodityId(commodityId);
        new_comment.setText(text);
        userNotFoundWithUserEmail(new_comment.getUserEmail());
        commodityNotFound(new_comment.getCommodityId());
        commoditiesNotSorted.get(new_comment.getCommodityId()).addComment(new_comment, commentId);
        comments.put(commentId, new_comment);
        commentId += 1;
    }

    public Comment addAndReturnComment(String username, Integer commodityId, String text) throws Exception {
        Comment new_comment = new Comment(commentId);
        new_comment.setUserEmail(usersUsername.get(username).getEmail());
        new_comment.setCommodityId(commodityId);
        new_comment.setText(text);
        userNotFoundWithUserEmail(new_comment.getUserEmail());
        commodityNotFound(new_comment.getCommodityId());
        commoditiesNotSorted.get(new_comment.getCommodityId()).addComment(new_comment, commentId);
        comments.put(commentId, new_comment);
        commentId += 1;
        return new_comment;
    }

    public void removeFromBuyList(String username, Integer commodityId) throws Exception {
        userNotFound(username);
        commodityNotFound(commodityId);
        usersUsername.get(username).removeFromBuyList(commodityId);
    }
    public void setDiscount(String username, String discountCode) throws Exception {
        userNotFound(username);
        discountNotFound(discountCode);
        usersUsername.get(username).setCurrentDiscount(discounts.get(discountCode));
    }

    public void addToBuyList(String username, Integer commodityId) throws Exception {
        userNotFound(username);
        commodityNotFound(commodityId);
        int inStock = commoditiesNotSorted.get(commodityId).getInStock();
        usersUsername.get(username).addToBuyList(commodityId, inStock);
    }

    public void addCredit(String username, Integer credit) throws Exception {
        userNotFound(username);
        usersUsername.get(username).addCredit(credit);
    }

    public void rateCommodity(Rate rate) throws Exception {
        userNotFound(rate.getUsername());
        commentNotFound(rate.getCommodityId());
        rate.hasError();
        commoditiesNotSorted.get(rate.getCommodityId()).addRate(rate);
    }

    public void voteComment(Vote vote) throws Exception {
        userNotFound(vote.getUsername());
        commentNotFound(vote.getCommentId());
        vote.hasError();
        comments.get(vote.getCommentId()).addVote(vote);
    }
    public void payment(String username) throws Exception {
        userNotFound(username);
        usersUsername.get(username).payment();
    }

    public Commodity getCommodityById(Integer id) throws Exception {
        return commoditiesNotSorted.get(id);
    }

    public static void checkProviderExist(Integer providerId) throws Exception {
        if (!existingProviders.containsKey(providerId))
            throw new ProviderNotFound();
    }

    public static void checkUserExist(String userEmail) throws Exception {
        if (users.containsKey(userEmail))
            throw new UserAlreadyExists();
    }

    public void userNotFound(String username) throws Exception {
        if (!usersUsername.containsKey(username))
            throw new UserNotFound();
    }
    public void userNotFoundWithUserEmail(String userEmail) throws Exception {
        if (!users.containsKey(userEmail))
            throw new UserNotFound();
    }
    public void isCorrectPass(String password, String username) throws Exception {
        if (!usersUsername.get(username).getPassword().equals(password))
            throw new WrongPassword();
    }

    public void actorNotFound(Integer userEmail) throws Exception {
        if (!existingProviders.containsKey(userEmail))
            throw new ProviderNotFound();
    }

    public void commodityNotFound(Integer commodityId) throws Exception {
        if (!commoditiesNotSorted.containsKey(commodityId))
            throw new CommodityNotFound();
    }
    public void discountNotFound(String discountCode) throws Exception {
        if (!discounts.containsKey(discountCode))
            throw new DiscountNotFound();
    }
    public void commentNotFound(Integer commentId) throws Exception {
        if (!comments.containsKey(commentId))
            throw new CommentNotFound();
    }

    public void setSearchedCommodities(String searchedCommodityName) {
        searchedCommodities.clear();
        for (Commodity commodity : commoditiesNotSorted.values()){
            if (commodity.getName().toUpperCase().contains(searchedCommodityName.toUpperCase())){
                searchedCommodities.put(commodity.getId(), commodity);
            }
        }
    }

    public void setSearchedCommoditiesBuyCat(String searchedCommodityCategory) {
        searchedCommodities.clear();
        for (Commodity commodity : commoditiesNotSorted.values()){
            for (String category: commodity.getCategories()){
                if (category.toUpperCase().contains(searchedCommodityCategory.toUpperCase())){
                    searchedCommodities.put(commodity.getId(), commodity);
                }
            }
        }
    }

//    public Map<Integer, Commodity> moviesToShow() {
//        System.out.println(UserManager.getInstance().isDefaultSort());
//        if (! UserManager.getInstance().isSearch()) {
//            System.out.println("kok1");
//            if (UserManager.getInstance().isDefaultSort()){
//                System.out.println("kok2");
//                return commoditiesNotSorted;
//            }
//            else{
//                System.out.println("kok3");
//                sortCommoditiesByPrice(commoditiesNotSorted);
//                return commodities;
//            }
////                return moviesSortedByDate;
//        }
//        else {
//            System.out.println("in else");
//            System.out.println(searchedCommodities);
//            if (UserManager.getInstance().isDefaultSort())
//                return searchedCommodities;
//            else{
//                System.out.println("in else 2");
//                sortCommoditiesByPrice(searchedCommodities);
//                return commodities;
//            }
//
//        }
//    }

    public Map<String, User> getUsers() {
        return users;
    }

    public static Map<String, User> getUsersUsername() {
        return usersUsername;
    }

    public Map<Integer, Commodity> getCommodities() {
        return commoditiesNotSorted;
    }

    public Map<Integer, Provider> getExistingActors() {
        return existingProviders;
    }

    public static Map<Integer, Comment> getComments() {
        return comments;
    }

    public static HashMap<Integer, Commodity> getCommoditiesSortedByDate() {
        return commoditiesSortedByDate;
    }

    private void searchByName(String searchValue) {
        for (Commodity commodity : commodities.values())
            if (commodity.getName().toUpperCase().contains(searchValue.toUpperCase()))
                searchedCommodities.put(commodity.getId(), commodity);
    }

    private void searchByReleaseDate(String searchValue) {
        for (Commodity commodity : commodities.values())
            if (commodity.getReleaseDate().toUpperCase().contains(searchValue.toUpperCase()))
                searchedCommodities.put(commodity.getId(), commodity);
    }

    private void searchByGenre(String searchValue) {
        for (Commodity commodity : commodities.values())
            for(String genre:commodity.getCategories())
                if (genre.contains(searchValue)) {
                    searchedCommodities.put(commodity.getId(), commodity);
                    break;
                }
    }

    private void searchAll() {
        for(Commodity commodity : commodities.values())
            searchedCommodities.put(commodity.getId(), commodity);
    }

    private static HashMap<Integer, Commodity> sortCommoditiesByDate(HashMap<Integer, Commodity> commoditiesToSort) {
        HashMap <Integer, Commodity> commodityCopy = new LinkedHashMap<>();
        for (Map.Entry<Integer, Commodity> entry: commoditiesToSort.entrySet()) {
            if (!entry.getValue().getReleaseDate().equals("-"))
                commodityCopy.put(entry.getKey(), entry.getValue());
        }
        List list = new LinkedList(commodityCopy.entrySet());
        HashMap<Integer, Commodity> sorted = new LinkedHashMap<>();
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                try {
                    return sdf.parse(((Commodity) ((Map.Entry) o1).getValue()).getReleaseDate()).before(sdf.parse(((Commodity) ((Map.Entry) o2).getValue()).getReleaseDate())) ? 1 : -1;
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sorted.put((Integer) entry.getKey(), (Commodity) entry.getValue());
        }
        return sorted;
    }
    public void setSearchedCommodities(String searchValue, String searchBy) {
        searchedCommodities.clear();
        if(searchBy.equals("category"))
            searchByGenre(searchValue);
        if(searchBy.equals("name"))
            searchByName(searchValue);
        if(searchBy.equals("releaseDate"))
            searchByReleaseDate(searchValue);
        if(searchBy.equals(""))
            searchAll();
    }
    public Map<Integer, Commodity> commoditiesToShow(boolean defaultSort, String searchBy, String searchValue) {
        setSearchedCommodities(searchValue, searchBy);
        if (defaultSort)
            return searchedCommodities;
        else
            return sortCommoditiesByDate(searchedCommodities);

    }

}
