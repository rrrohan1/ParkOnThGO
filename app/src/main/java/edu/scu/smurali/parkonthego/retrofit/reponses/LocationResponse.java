package edu.scu.smurali.parkonthego.retrofit.reponses;

/**
 * Created by chshi on 5/23/2016.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private LocationData data;

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
    public LocationData getData() {
        return data;
    }

    /**
     * @param data The data
     */
    public void setData(LocationData data) {
        this.data = data;
    }

}
