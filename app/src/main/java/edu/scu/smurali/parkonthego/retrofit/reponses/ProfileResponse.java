package edu.scu.smurali.parkonthego.retrofit.reponses;

/**
 * Created by chshi on 6/4/2016.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileResponse {


    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private ProfileData data;

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
    public ProfileData getData() {
        return data;
    }

    /**
     * @param data The data
     */
    public void setData(ProfileData data) {
        this.data = data;
    }

}