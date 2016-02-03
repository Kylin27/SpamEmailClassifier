package org.kylin.zhang;

/**
 * Created by win-7 on 2016/1/30.
 */

import java.util.Arrays ;
import java.util.List;

import ch.epfl.lamp.compiler.msil.emit.Label;
import org.apache.spark.SparkConf ;
import org.apache.spark.api.java.JavaRDD ;
import org.apache.spark.api.java.JavaSparkContext ;
import org.apache.spark.api.java.function.Function ;

import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD ;
import org.apache.spark.mllib.feature.HashingTF ;
import org.apache.spark.mllib.linalg.Vector ;
import org.apache.spark.mllib.regression.LabeledPoint ;


public class SpamClassifier {

    public static void main (String [] args) {
        SparkConf sparkConf = new SparkConf().setAppName("Spark hands-on").setMaster("local") ;
        JavaSparkContext sc = new JavaSparkContext(sparkConf) ;

        // Load 2 types of emails from text files , spam and ham (non-spam)
        // Each line has text from one email
        JavaRDD<String> spam = sc.textFile("text.txt") ;


        // here we , create a HashTF instance to map email text to vectors of 100 features
        final HashingTF tf = new HashingTF(100) ;

        JavaRDD<Vector> javaRDD = spam.map(new Function<String, Vector>() {
            public Vector call(String v1) throws Exception {
                System.out.println(Arrays.asList(v1.split(" "))) ;
               return  tf.transform(Arrays.asList(v1.split(" "))) ;
            }
        }) ;

        List<Vector> vectorList = javaRDD.take(100) ;

        for (Vector vv : vectorList){
            System.out.println( vv);
        }

         // Each email is split into words , and each word is mapped to one feature
        // Create LabeledPoint datasets for positive (spam) and negative(ham) examples


       /* JavaRDD<LabeledPoint> positiveExamples = spam.map(new Function<String, LabeledPoint>() {
            public LabeledPoint call(String v1) throws Exception {
                return new LabeledPoint(1, tf.transform(Arrays.asList(v1.split(" ")))) ;
            }
        })  ;

       // print JavaRDD<LabeledPoint> contents
        List<LabeledPoint> pointList = positiveExamples.takeOrdered(20) ;

        for ( LabeledPoint p : pointList){
            System.out.println(p.label());

            System.out.println(p.features().toArray()) ;

            System.out.println("------------------------") ;
        }*/
    }
}
