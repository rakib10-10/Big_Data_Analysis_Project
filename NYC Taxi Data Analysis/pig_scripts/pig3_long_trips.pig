records = LOAD 'D:/Rakib/Final_Project/jan_taxi.csv' USING PigStorage(',') AS (
    VendorID:chararray, tpep_pickup_datetime:chararray, tpep_dropoff_datetime:chararray,
    passenger_count:int, trip_distance:float, RatecodeID:chararray,
    store_and_fwd_flag:chararray, PULocationID:chararray, DOLocationID:chararray,
    payment_type:chararray, fare_amount:float, extra:float, mta_tax:float,
    tip_amount:float, tolls_amount:float, improvement_surcharge:float, total_amount:float
);
valid_trips = FILTER records BY trip_distance IS NOT NULL AND trip_distance > 50.0;
projected_trips = FOREACH valid_trips GENERATE PULocationID, DOLocationID, trip_distance, total_amount;
ordered_trips = ORDER projected_trips BY trip_distance DESC;
top10_longest = LIMIT ordered_trips 10;

formatted_output = FOREACH top10_longest GENERATE 
    CONCAT('Route: From_Zone_', (chararray)PULocationID, ' -> To_Zone_', (chararray)DOLocationID) AS route,
    CONCAT('Distance_Miles: ', (chararray)trip_distance) AS distance,
    CONCAT('Total_Paid_USD: $', (chararray)total_amount) AS fare;

STORE formatted_output INTO 'D:/Rakib/Final_Project/report_results/pig3_long_trips' USING PigStorage('\t');