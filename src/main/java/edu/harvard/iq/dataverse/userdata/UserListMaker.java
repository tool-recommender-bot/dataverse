/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse.userdata;

import edu.harvard.iq.dataverse.UserServiceBean;
import edu.harvard.iq.dataverse.mydata.Pager;
import edu.harvard.iq.dataverse.search.SearchConstants;
import edu.harvard.iq.dataverse.util.json.NullSafeJsonBuilder;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author rmp553
 */
public class UserListMaker {
    
    UserServiceBean userService;
    
    public boolean errorFound = false;
    public String errorMessage = null;
    
    public static final int ITEMS_PER_PAGE = 25;

    
    /*
     * Constructor
     */
    public UserListMaker(UserServiceBean userService) {
        this.msgt("MyDataFinder, constructor");
        this.userService = userService;
    }
    
    /*
     * Run the search
     */
    public JsonObjectBuilder runSearch(String searchTerm, Integer itemsPerPage, Integer selectedPage, String sorKey){
        
        // Initialize searchTerm
        if ((searchTerm == null) || (searchTerm.trim().isEmpty())){
            searchTerm = null;
        }

        // Initialize itemsPerPage
        if ((itemsPerPage == null) || (itemsPerPage < 10)){
            itemsPerPage = ITEMS_PER_PAGE;
        }

        // Initialize selectedPage
        if ((selectedPage == null) || (selectedPage < 1)){
            selectedPage = 1;
        }

        // Initialize sortKey
        String sortKey = null;

              
        // -------------------------------------------------
        // (1) What is the user count for this search?
        // -------------------------------------------------
        Long userCount = userService.getUserCount(searchTerm);
        
        // Are there any hits?  No; return info
        if ((userCount == null)||(userCount == 0)){
            return getNoResultsJSON();
        }
              
        // -------------------------------------------------
        // (2) Do some calculations here regarding the selected page, offset, etc.
        // -------------------------------------------------
        
        int offset = (selectedPage - 1) * itemsPerPage;
        if (offset > userCount){
            offset = 0;
            selectedPage = 1;
        }

        
        // -------------------------------------------------
        // (3) Retrieve the users
        // -------------------------------------------------
        
        
        JsonArrayBuilder jsonUserListArray = userService.getUserListAsJSON(searchTerm, sortKey, itemsPerPage, offset);       
        if (jsonUserListArray==null){
            return getNoResultsJSON();
        }
        
         Pager pager = new Pager(userCount.intValue(), itemsPerPage, selectedPage);

        JsonObjectBuilder jsonOverallData = Json.createObjectBuilder();
        jsonOverallData.add("userCount", userCount)
                       .add("pagination", pager.asJsonObjectBuilder())
                       .add("users", jsonUserListArray)
                       .add("selectedPage", 1);

        return jsonOverallData;
        
    }
    
    private JsonObjectBuilder getNoResultsJSON(){
        
         return Json.createObjectBuilder()
                        .add("userCount", 0)
                        .add("users", Json.createArrayBuilder()) // empty array
                        .add("selectedPage", 1);
    }
    
    public boolean hasError(){
        return this.errorFound;
    }
    
    public String getErrorMessage(){
        return this.errorMessage;
    }
    
    private void addErrorMessage(String errMsg){
        this.errorFound = true;
        this.errorMessage = errMsg;
    }
   
    private void msg(String s){
        System.out.println(s);
    }
    
    private void msgt(String s){
        msg("-------------------------------");
        msg(s);
        msg("-------------------------------");
    }
}
