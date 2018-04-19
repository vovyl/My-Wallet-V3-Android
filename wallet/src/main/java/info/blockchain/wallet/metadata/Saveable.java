package info.blockchain.wallet.metadata;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public interface Saveable {

    // TODO: 19/04/2018 I'm not sure that this will actually be ignored in a subtype
    // Requires testing as not to break metadata
    @JsonIgnore
    int getMetadataType();

    @JsonIgnore
    Saveable fromJson(String json) throws IOException;

    @JsonIgnore
    String toJson() throws JsonProcessingException;

}