# Description 

This project is used to classify spam and normal emails which follow MimeMessage email protocol pattern from email [dataset's](http://pan.baidu.com/s/1i4nZndb) by running machine learning algorithm provided by Spark's mllib (The lib for machine learnings). Spark mllib supports many machine learning algorithms , and in this project we only use the Naive-Bayes a kind of classify method to classify the dataset into two datasets.

Cause Naive-Bayes is a Supervised machine learning algorithm, so both testing and training dataset is needed. 
## Project Structure

<pre>
<code>
../log
	|-log.log  # log file used to store debug info which produced by executing the project
 
../resources
	|-log4j.properties   # properties file used to define the log info output pattern
	|-stop_word_set.txt  # stop word set used to filter no use words (like 'a' ,'the','or'...) from dataset
    |-data/
		|-TESTING		 # folder used to store testing data-set
			|-normal	 # normal emails dataset used for testing
			|-spam		 # spam emails dataset used for testing

		|-TRAINING		 # folder used to store training data-set
			|-normal	 # normal emails dataset used for training data-model
			|-spam  	 # spam emails dataset used for training data-model
../src/
	|-.../Redis
	|-.../email          # we abstract the email contents as a email-bean which used as a java bean store email file useful info into attribute fields
			|-EmailWordCounter
				|-NormalWordsTopCounter.java  # used to count topN most frequently appeared in normal email file feature fields
				|-SpamWordsTopCounter.java 	  # count topN most frequently appeared words in spam email file useful feature content 
				|-WordsCounter.java           # WordsCounter is parent class of NormalWordsTopCounter and SpamWordsTopCounter and provide sparks' word counter method 
			|-bean
				|-EmailBean.java
			|-EmailFileReader.java  # read email contents(features) under MIME protocol 


	|-.../spark
			|- FeatureExtractor.java  # read EmailBeans from Redis and extracts the email-beans' attribute fields value into List<LabeledPoint>
			|-NaiveBayesUtil.java  # wrapped Naive-Bayes algorithms api provided by spark mllib to reduce data pattern transfrom steps.
	|-.../Main.java		 # entry main method for project
</code>
</pre>



## how to run the program on my computer ,following steps described below 

* first , clone the source to local system
``
git clone git@github.com:Kylin27/SpamEmailClassifier.git
``
* second, setup your IDEA (java IDE ) File -> New -> Project from Existing Sources

* thrid, setup the redis-server and open the Main.java java source file , click mouse right -> "run Main.main()" 

* finally wait for a moment about  minutes , you will see the result output on console
  and if there is something logic goes wrong , you can refer to the log file under path ../log/log.log 

good luck and have fun ~