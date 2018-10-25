package info.blockchain.wallet.prices;

import info.blockchain.wallet.prices.data.PriceDatum;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

public interface PriceEndpoints {

    @GET(PriceUrls.PRICE_SERIES)
    Single<List<PriceDatum>> getHistoricPriceSeries(@Query("base") String base,
                                                    @Query("quote") String quote,
                                                    @Query("start") long start,
                                                    @Query("scale") int scale,
                                                    @Query("api_key") String apiKey);

    @GET(PriceUrls.SINGLE_PRICE)
    Single<PriceDatum> getCurrentPrice(@Query("base") String base,
                                           @Query("quote") String quote,
                                           @Query("api_key") String apiKey);

    @GET(PriceUrls.SINGLE_PRICE)
    Single<PriceDatum> getHistoricPrice(@Query("base") String base,
                                            @Query("quote") String quote,
                                            @Query("time") long time,
                                            @Query("api_key") String apiKey);

    @GET(PriceUrls.PRICE_INDEXES)
    Single<Map<String, PriceDatum>> getPriceIndexes(@Query("base") String base,
                                                        @Query("api_key") String apiKey);

}
