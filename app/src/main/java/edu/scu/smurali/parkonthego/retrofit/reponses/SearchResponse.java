package edu.scu.smurali.parkonthego.retrofit.reponses;

/**
 * Created by chshi on 5/23/2016.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SearchResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private List<SearchData> data = new ArrayList<SearchData>();

    /**
     * @return The success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * @param success The success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * @return The data
     */
    public List<SearchData> getData() {
        return data;
    }

    /**
     * @param data The data
     */
    public void setData(List<SearchData> data) {
        this.data = data;
    }

}

