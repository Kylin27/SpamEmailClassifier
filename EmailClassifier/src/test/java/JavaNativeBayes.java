import junit.framework.Assert;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by win-7 on 2016/1/30.
 */
public class JavaNativeBayes {
    private transient JavaSparkContext sc ;

    @Before
    public void setUp(){
        sc = new JavaSparkContext("local", "JavaNaiveBayes") ;
    }

    @After
    public void tearDown(){
        sc.stop() ;
        sc = null ;
    }

    private static final List<LabeledPoint> POINTS =
            Arrays.asList(
                    new LabeledPoint(0, Vectors.dense(1.0, 0.0, 0.0)) ,
                    new LabeledPoint(0, Vectors.dense(2.0, 0.0, 0.0)),
                    new LabeledPoint(1, Vectors.dense(0.0,1.0,0.0)),
                    new LabeledPoint(1, Vectors.dense(0.0,2.0,0.0)),
                    new LabeledPoint(2, Vectors.dense(0.0,0.0,1.0)),
                    new LabeledPoint(2, Vectors.dense(0.0,0.0,2.0))
            ) ;


    private int validatePrediction(List<LabeledPoint> points, NaiveBayesModel model){
        int correct = 0 ;

        for ( LabeledPoint p : points ){
            if ( model.predict(p.features()) == p.label()){
                correct += 1 ;
            }
        }
        return correct ;
    }

    @Test
    public void runUsingConstructor(){
        JavaRDD<LabeledPoint> testRDD = sc.parallelize(POINTS,2).cache() ;

        NaiveBayes nb = new NaiveBayes().setLambda(1.0) ;
        NaiveBayesModel model = nb.run(testRDD.rdd()) ;

        int numAccurate = validatePrediction(POINTS, model) ;

        if ( POINTS.size() == numAccurate)
            System.out.println("OK") ;

    }

    @Test
    public void runUsingStaticMethods(){
      JavaRDD<LabeledPoint> testRDD = sc.parallelize(POINTS,2).cache() ;

        NaiveBayesModel model1 = NaiveBayes.train(testRDD.rdd()) ;
        int numAccurate1 = validatePrediction(POINTS, model1) ;

        if ( POINTS.size() == numAccurate1)
            System.out.println(" ok 2");

        NaiveBayesModel model2 = NaiveBayes.train(testRDD.rdd() , 0.5) ;
        int numAccurate2 = validatePrediction(POINTS, model2) ;
        if (POINTS.size() == numAccurate2)
            System.out.println("ok 4") ;

    }

    @Test
    public void testPredictionJavaRDD(){
        JavaRDD<LabeledPoint> examples = sc.parallelize(POINTS,2).cache() ;
        NaiveBayesModel model = NaiveBayes.train(examples.rdd()) ;

        System.out.println();

        JavaRDD<Vector> vectors = examples.map(new Function<LabeledPoint, Vector>() {
            public Vector call(LabeledPoint v1) throws Exception {
                return  v1.features() ;
            }
        }) ;

        JavaRDD<Double> predictions = model.predict(vectors) ;

        // Should be able to get the first prediction.
        predictions.first() ;
    }
}
