package piuk.blockchain.androidbuysell.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by justin on 5/1/17.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeData {

    public TradeData() {
        // Empty constructor
    }

    @JsonProperty("id")
    private int id = 0;

    @JsonProperty("state")
    private String state = null;

    @JsonProperty("confirmed")
    private boolean confirmed = false;

    @JsonProperty("is_buy")
    private boolean isBuy = true;

    @JsonProperty("account_index")
    private int accountIndex = 0;

    @JsonProperty("receive_index")
    private int receiveIndex = 0;

    public int getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public int getAccountIndex() {
        return accountIndex;
    }

    public int getReceiveIndex() {
        return receiveIndex;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setBuy(boolean isBuy) {
        this.isBuy = isBuy;
    }

    public void setAccountIndex(int accountIndex) {
        this.accountIndex = accountIndex;
    }

    public void setReceiveIndex(int receiveIndex) {
        this.receiveIndex = receiveIndex;
    }
}
