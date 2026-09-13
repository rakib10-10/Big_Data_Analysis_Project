records = LOAD 'D:/Rakib/Final_Project/jan_taxi.csv' USING PigStorage(',') AS (
    VendorID:chararray, tpep_pickup_datetime:chararray, tpep_dropoff_datetime:chararray,
    passenger_count:int, trip_distance:float, RatecodeID:chararray,
    store_and_fwd_flag:chararray, PULocationID:chararray, DOLocationID:chararray,
    payment_type:chararray, fare_amount:float, extra:float, mta_tax:float,
    tip_amount:float, tolls_amount:float, improvement_surcharge:float, total_amount:float
);
filtered_data = FILTER records BY PULocationID != 'PULocationID' AND PULocationID IS NOT NULL;
grouped_loc = GROUP filtered_data BY PULocationID;
counted_loc = FOREACH grouped_loc GENERATE group AS PULocationID, COUNT(filtered_data) AS trip_count;
ordered_loc = ORDER counted_loc BY trip_count DESC;
top10_pickups = LIMIT ordered_loc 10;

formatted_output = FOREACH top10_pickups GENERATE 
    CONCAT('Pickup_Location_ID: ', (chararray)PULocationID) AS location,
    CONCAT('Total_Trips: ', (chararray)trip_count) AS trips;

STORE formatted_output INTO 'D:/Rakib/Final_Project/report_results/pig1_top_pickups' USING PigStorage('\t');