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

public class TaxiPaymentTipAnalysis {

    public static class TipMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
        private Text paymentType = new Text();
        private DoubleWritable tipAmount = new DoubleWritable();

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (line.isEmpty() || line.startsWith("VendorID") || line.contains("payment_type")) {
                return;
            }
            String[] fields = line.split(",");
            // payment_type: index 9, tip_amount: index 13
            if (fields.length > 13) {
                try {
                    String pay = fields[9].trim();
                    double tip = Double.parseDouble(fields[13].trim());
                    if (!pay.isEmpty() && tip >= 0) {
                        paymentType.set("PaymentType_" + pay);
                        tipAmount.set(tip);
                        context.write(paymentType, tipAmount);
                    }
                } catch (Exception e) {
                    // Skip malformed records
                }
            }
        }
    }

    public static class TipReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        private DoubleWritable result = new DoubleWritable();

        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            double totalTip = 0.0;
            long count = 0;
            for (DoubleWritable val : values) {
                totalTip += val.get();
                count++;
            }
            double averageTip = (count > 0) ? (totalTip / count) : 0.0;
            result.set(Math.round(averageTip * 100.0) / 100.0);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Taxi Payment Tip Average Analysis");
        job.setJarByClass(TaxiPaymentTipAnalysis.class);
        job.setMapperClass(TipMapper.class);
        job.setReducerClass(TipReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}