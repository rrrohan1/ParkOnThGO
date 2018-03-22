package edu.scu.smurali.parkonthego.retrofit.reponses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class LoginData {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("display_name")
    @Expose
    private String displayName;

    /**
     * @return The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName The display_name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}