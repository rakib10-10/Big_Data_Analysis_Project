records = LOAD 'D:/Rakib/Final_Project/jan_taxi.csv' USING PigStorage(',') AS (
    VendorID:chararray, tpep_pickup_datetime:chararray, tpep_dropoff_datetime:chararray,
    passenger_count:int, trip_distance:float, RatecodeID:chararray,
    store_and_fwd_flag:chararray, PULocationID:chararray, DOLocationID:chararray,
    payment_type:chararray, fare_amount:float, extra:float, mta_tax:float,
    tip_amount:float, tolls_amount:float, improvement_surcharge:float, total_amount:float
);
filtered_surcharge = FILTER records BY RatecodeID != 'RatecodeID' AND RatecodeID IS NOT NULL;
grouped_rate = GROUP filtered_surcharge BY RatecodeID;
audit_summary = FOREACH grouped_rate GENERATE 
    group AS RatecodeID, 
    COUNT(filtered_surcharge) AS total_trips,
    ROUND_TO(SUM(filtered_surcharge.extra), 2) AS total_extra,
    ROUND_TO(SUM(filtered_surcharge.mta_tax), 2) AS total_mta_tax,
    ROUND_TO(SUM(filtered_surcharge.improvement_surcharge), 2) AS total_surcharge;
ordered_audit = ORDER audit_summary BY total_trips DESC;

formatted_output = FOREACH ordered_audit GENERATE 
    (RatecodeID == '1' ? 'Rate_Plan: Standard Rate (1)' : 
    (RatecodeID == '2' ? 'Rate_Plan: JFK Airport (2)' : 
    (RatecodeID == '3' ? 'Rate_Plan: Newark (3)' : 
    (RatecodeID == '4' ? 'Rate_Plan: Nassau/Westchester (4)' : 
    (RatecodeID == '5' ? 'Rate_Plan: Negotiated Fare (5)' : CONCAT('Rate_Plan: Plan_', (chararray)RatecodeID)))))) AS plan,
    CONCAT('Trips: ', (chararray)total_trips) AS trips,
    CONCAT('Extra_Charges: $', (chararray)total_extra) AS extra,
    CONCAT('MTA_Tax: $', (chararray)total_mta_tax) AS tax,
    CONCAT('Surcharge: $', (chararray)total_surcharge) AS surcharge;

STORE formatted_output INTO 'D:/Rakib/Final_Project/report_results/pig5_surcharge_audit' USING PigStorage('\t');