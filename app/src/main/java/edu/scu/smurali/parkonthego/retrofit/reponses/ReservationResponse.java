package edu.scu.smurali.parkonthego.retrofit.reponses;

/**
 * Created by chshi on 5/25/2016.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ReservationResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private List<ReservationData> data = new ArrayList<ReservationData>();

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
    public List<ReservationData> getData() {
        return data;
    }

    /**
     * @param data The data
     */
    public void setData(List<ReservationData> data) {
        this.data = data;
    }

}