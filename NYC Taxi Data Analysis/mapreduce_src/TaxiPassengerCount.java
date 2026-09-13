import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TaxiPassengerCount {

    public static class PassengerMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
        private Text passengerKey = new Text();
        private final static LongWritable ONE = new LongWritable(1);

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (line.isEmpty() || line.startsWith("VendorID") || line.contains("passenger_count")) {
                return;
            }
            String[] fields = line.split(",");
            if (fields.length > 3) {
                String pCount = fields[3].trim();
                if (!pCount.isEmpty()) {
                    try {
                        Integer.parseInt(pCount);
                        passengerKey.set("Passengers_" + pCount);
                        context.write(passengerKey, ONE);
                    } catch (NumberFormatException e) {
                        // Skip malformed records
                    }
                }
            }
        }
    }

    public static class PassengerReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        private LongWritable result = new LongWritable();

        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long sum = 0;
            for (LongWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Taxi Passenger Count Analysis");
        job.setJarByClass(TaxiPassengerCount.class);
        job.setMapperClass(PassengerMapper.class);
        job.setCombinerClass(PassengerReducer.class);
        job.setReducerClass(PassengerReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}