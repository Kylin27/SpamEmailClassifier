import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;

import java.util.Arrays;
import java.util.List;

/**
 * Created by win-7 on 2016/1/31.
 */
public class NaiveBayes_demo {

    public  static final List<LabeledPoint> POINTS = Arrays.asList(
            new LabeledPoint(0, Vectors.dense(1.0, 0.0, 0.0)),
            new LabeledPoint(0, Vectors.dense(2.0, 0.0, 0.0)),
            new LabeledPoint(1, Vectors.dense(0.0, 1.0, 0.0)),
            new LabeledPoint(1, Vectors.dense(0.0, 2.0, 0.0)),
            new LabeledPoint(2, Vectors.dense(0.0, 0.0, 1.0)),
            new LabeledPoint(2, Vectors.dense(0.0, 0.0, 2.0))
    );

    public   int validatePrediction(List<LabeledPoint> points , NaiveBayesModel model){
        int correct = 0 ;
        for (LabeledPoint p : points ){
            System.out.println(p.features()) ;
            System.out.println(p.label()) ;

            System.out.println(" predict result " + model.predict(p.features())) ;
            System.out.println() ;
        }

        return 0 ;
    }

    public static void main (String [] args ){
        JavaSparkContext sc = new JavaSparkContext("local","NaiveBayes_demo") ;

        JavaRDD<LabeledPoint> testRDD = sc.parallelize(POINTS, 2).cache() ;

        NaiveBayes nb = new NaiveBayes().setLambda(1.0) ;
        NaiveBayesModel model = nb.run(testRDD.rdd()) ;

        NaiveBayes_demo nbDemo = new NaiveBayes_demo() ;

        nbDemo.validatePrediction(POINTS, model) ;
    }

}
