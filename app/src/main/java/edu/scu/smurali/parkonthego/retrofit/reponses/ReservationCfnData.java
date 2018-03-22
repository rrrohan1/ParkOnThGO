package edu.scu.smurali.parkonthego.retrofit.reponses;

/**
 * Created by chshi on 5/23/2016.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReservationCfnData {


    @SerializedName("id")
    @Expose
    private Integer id;

    /**
     * @return The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

}
