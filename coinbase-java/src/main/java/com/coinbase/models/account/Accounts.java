package com.coinbase.models.account;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Accounts {

    @SerializedName("pagination")
    @Expose
    private com.coinbase.models.Pagination pagination;
    @SerializedName("data")
    @Expose
    private List<Data> data = new ArrayList<Data>();

    /**
     *
     * @return
     * The pagination
     */
    public com.coinbase.models.Pagination getPagination() {
        return pagination;
    }

    /**
     *
     * @param pagination
     * The pagination
     */
    public void setPagination(com.coinbase.models.Pagination pagination) {
        this.pagination = pagination;
    }

    /**
     *
     * @return
     * The data
     */
    public List<Data> getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    public void setData(List<Data> data) {
        this.data = data;
    }

}