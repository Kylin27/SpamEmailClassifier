package org.kylin.zhang.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;

import java.util.List;


/**
 * Created by win-7 on 2016/2/2.
 */
public class NaiveBayesUtil {
    transient private SparkConf sparkConf ;
    transient private JavaSparkContext sc ;
    private NaiveBayesModel naiveBayesModel = null  ;

    public NaiveBayesUtil(){
        sparkConf = new SparkConf().setAppName("Spark hands-on").setMaster("local");
        sc = new JavaSparkContext(sparkConf);
    }
    public NaiveBayesUtil(SparkConf conf ){
        this.sparkConf = conf ;
        this.sc = new JavaSparkContext(sparkConf) ;
    }

    public NaiveBayesUtil(SparkConf conf , JavaSparkContext sparkContext){
        this.sparkConf = conf ;
        this.sc = sparkContext ;
    }

    public void TrainDataSet(List<LabeledPoint> labeledPointsList){
        JavaRDD<LabeledPoint> trainDataSets = sc.parallelize(labeledPointsList).cache() ;
        NaiveBayes nb = new NaiveBayes().setLambda(1.0) ;
        this.naiveBayesModel = nb.run(trainDataSets.rdd()) ;
    }

    public double getClassifierResult (Vector testData ){
        if ( this.naiveBayesModel == null ){
            System.out.println("training naive bayes model not build") ;
            return -1.0 ;
        }
        return this.naiveBayesModel.predict(testData) ;
    }
}
