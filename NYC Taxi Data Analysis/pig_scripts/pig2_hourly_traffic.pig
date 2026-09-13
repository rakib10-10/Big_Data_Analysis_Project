records = LOAD 'D:/Rakib/Final_Project/jan_taxi.csv' USING PigStorage(',') AS (
    VendorID:chararray, tpep_pickup_datetime:chararray, tpep_dropoff_datetime:chararray,
    passenger_count:int, trip_distance:float, RatecodeID:chararray,
    store_and_fwd_flag:chararray, PULocationID:chararray, DOLocationID:chararray,
    payment_type:chararray, fare_amount:float, extra:float, mta_tax:float,
    tip_amount:float, tolls_amount:float, improvement_surcharge:float, total_amount:float
);
valid_data = FILTER records BY tpep_pickup_datetime != 'tpep_pickup_datetime' AND (tpep_pickup_datetime IS NOT NULL);
with_hour = FOREACH valid_data GENERATE SUBSTRING(tpep_pickup_datetime, 11, 13) AS pickup_hour;
grouped_hour = GROUP with_hour BY pickup_hour;
counted_traffic = FOREACH grouped_hour GENERATE group AS pickup_hour, COUNT(with_hour) AS total_trips;
ordered_traffic = ORDER counted_traffic BY total_trips DESC;

formatted_output = FOREACH ordered_traffic GENERATE 
    CONCAT('Hour_OfDay: ', (chararray)pickup_hour, ':00') AS hour,
    CONCAT('Trip_Count: ', (chararray)total_trips) AS trips;

STORE formatted_output INTO 'D:/Rakib/Final_Project/report_results/pig2_hourly_traffic' USING PigStorage('\t');