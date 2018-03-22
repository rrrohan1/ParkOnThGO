package edu.scu.smurali.parkonthego.retrofit.reponses;

/**
 * Created by chshi on 6/4/2016.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateProfileResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private Boolean data;

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
    public Boolean getData() {
        return data;
    }

    /**
     * @param data The data
     */
    public void setData(Boolean data) {
        this.data = data;
    }

}
