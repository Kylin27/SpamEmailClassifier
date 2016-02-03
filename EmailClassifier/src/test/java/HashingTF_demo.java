import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by win-7 on 2016/1/30.
 */
public class HashingTF_demo implements Serializable {

    @Test
    public void testTextAnalysis() throws Exception{
        SparkConf sparkConf = new SparkConf().setAppName("HashingTF_demo").setMaster("local") ;
        JavaSparkContext sc = new JavaSparkContext(sparkConf) ;



        final HashingTF hashingTF = new HashingTF(100) ;

        JavaRDD<String> ham =sc.textFile("org/kylin/zhang/text.txt") ;

        System.out.println(ham.toString()) ;

        JavaRDD<LabeledPoint> positiveExamples = ham.map(new Function<String, LabeledPoint>() {
            public LabeledPoint call(String v1) throws Exception {
                return new LabeledPoint(1, hashingTF.transform(Arrays.asList(v1.split(" ")))) ;
            }
        }) ;

        for ( LabeledPoint p : positiveExamples.collect()){
            System.out.println(p.label()) ;
            System.out.println(p.features());
        }
    }

}
