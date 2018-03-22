package edu.scu.smurali.parkonthego.retrofit.reponses;

/**
 * Created by chshi on 5/22/2016.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private LoginData data;

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
    public LoginData getData() {
        return data;
    }

    /**
     * @param data The data
     */
    public void setData(LoginData data) {
        this.data = data;
    }

}
