package edu.scu.smurali.parkonthego.retrofit.reponses;

/**
 * Created by chshi on 6/5/2016.
 */

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ReservationDeleteResponse {
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private List<Object> data = new ArrayList<Object>();

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
    public List<Object> getData() {
        return data;
    }

    /**
     * @param data The data
     */
    public void setData(List<Object> data) {
        this.data = data;
    }

}

