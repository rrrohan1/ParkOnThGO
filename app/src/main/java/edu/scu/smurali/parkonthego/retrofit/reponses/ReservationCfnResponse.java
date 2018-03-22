package edu.scu.smurali.parkonthego.retrofit.reponses;

/**
 * Created by chshi on 5/23/2016.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReservationCfnResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private ReservationCfnData data;

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
    public ReservationCfnData getData() {
        return data;
    }

    /**
     * @param data The data
     */
    public void setData(ReservationCfnData data) {
        this.data = data;
    }

}