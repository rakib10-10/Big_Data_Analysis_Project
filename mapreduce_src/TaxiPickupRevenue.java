import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TaxiPickupRevenue {

    public static class RevenueMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
        private Text pickupLocation = new Text();
        private DoubleWritable totalAmount = new DoubleWritable();

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (line.isEmpty() || line.startsWith("VendorID") || line.contains("PULocationID")) {
                return;
            }
            String[] fields = line.split(",");
            // PULocationID: index 7, total_amount: index 16
            if (fields.length > 16) {
                try {
                    String puLoc = fields[7].trim();
                    double total = Double.parseDouble(fields[16].trim());
                    if (!puLoc.isEmpty() && total > 0) {
                        pickupLocation.set("PULocation_" + puLoc);
                        totalAmount.set(total);
                        context.write(pickupLocation, totalAmount);
                    }
                } catch (Exception e) {
                    // Skip malformed records
                }
            }
        }
    }

    public static class RevenueReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        private DoubleWritable result = new DoubleWritable();

        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            double sumRevenue = 0.0;
            for (DoubleWritable val : values) {
                sumRevenue += val.get();
            }
            result.set(Math.round(sumRevenue * 100.0) / 100.0);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Taxi Pickup Location Revenue Analysis");
        job.setJarByClass(TaxiPickupRevenue.class);
        job.setMapperClass(RevenueMapper.class);
        job.setCombinerClass(RevenueReducer.class);
        job.setReducerClass(RevenueReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}