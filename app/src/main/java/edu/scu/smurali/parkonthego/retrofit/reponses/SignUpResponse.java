
package edu.scu.smurali.parkonthego.retrofit.reponses;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SignUpResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private SignUpdata data;

    /**
     * 
     * @return
     *     The success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * 
     * @param success
     *     The success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * 
     * @return
     *     The data
     */
    public SignUpdata getData() {
        return data;
    }

    /**
     * 
     * @param data
     *     The data
     */
    public void setData(SignUpdata data) {
        this.data = data;
    }

}
