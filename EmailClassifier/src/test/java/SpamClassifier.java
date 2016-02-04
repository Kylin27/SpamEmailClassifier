import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.linalg.Vector;

import java.util.Arrays;
import java.util.List;

/**
 * Created by win-7 on 2016/1/30.
 */
public class SpamClassifier {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("Spark hands-on").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        // Load 2 types of emails from text files , spam and ham (non-spam)
        // Each line has text from one email
        JavaRDD<String> spam = sc.textFile("aimer_tester.eml");


        // here we , create a HashTF instance to map email text to vectors of 100 features
        final HashingTF tf = new HashingTF(100);

        JavaRDD<Vector> javaRDD = spam.map(new Function<String, Vector>() {
            public Vector call(String v1) throws Exception {
                System.out.println(Arrays.asList(v1.split(" ")));
                return tf.transform(Arrays.asList(v1.split(" ")));
            }
        });

        List<Vector> vectorList = javaRDD.take(100);

        for (Vector vv : vectorList) {
            System.out.println(vv);
        }
    }
}
