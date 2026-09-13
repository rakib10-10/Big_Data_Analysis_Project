records = LOAD 'D:/Rakib/Final_Project/jan_taxi.csv' USING PigStorage(',') AS (
    VendorID:chararray, tpep_pickup_datetime:chararray, tpep_dropoff_datetime:chararray,
    passenger_count:int, trip_distance:float, RatecodeID:chararray,
    store_and_fwd_flag:chararray, PULocationID:chararray, DOLocationID:chararray,
    payment_type:chararray, fare_amount:float, extra:float, mta_tax:float,
    tip_amount:float, tolls_amount:float, improvement_surcharge:float, total_amount:float
);
filtered_pay = FILTER records BY payment_type != 'payment_type' AND payment_type IS NOT NULL;
grouped_pay = GROUP filtered_pay BY payment_type;
summary_pay = FOREACH grouped_pay GENERATE 
    group AS payment_type, 
    COUNT(filtered_pay) AS total_count, 
    ROUND_TO(SUM(filtered_pay.total_amount), 2) AS total_revenue;
ordered_pay = ORDER summary_pay BY total_count DESC;

formatted_output = FOREACH ordered_pay GENERATE 
    (payment_type == '1' ? 'Payment_Method: Credit Card (1)' : 
    (payment_type == '2' ? 'Payment_Method: Cash (2)' : 
    (payment_type == '3' ? 'Payment_Method: No Charge (3)' : 
    (payment_type == '4' ? 'Payment_Method: Dispute (4)' : CONCAT('Payment_Method: Type_', (chararray)payment_type))))) AS method,
    CONCAT('Total_Transactions: ', (chararray)total_count) AS count,
    CONCAT('Total_Revenue_USD: $', (chararray)total_revenue) AS revenue;

STORE formatted_output INTO 'D:/Rakib/Final_Project/report_results/pig4_payment_comparison' USING PigStorage('\t');