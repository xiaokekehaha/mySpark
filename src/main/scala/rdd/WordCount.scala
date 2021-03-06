package rdd

import org.apache.spark.{SparkConf, SparkContext}

/**
  * 使用scala开发本地测试的Spark Wordcount程序
  */
object WordCount {
	def main(args: Array[String]): Unit = {


		/**
		  * 第一步:创建配置对象SparkConf,设置Spark程序寻星时的配置信息
		  * 例如说:通过setMaster来设置程序要链接的Spark程序的Master的URL,
		  * 如果设置为local,则代表Spark程序运行在本地模式,特别适合机器比较差的初学者,1gRAM
		  */
		val conf = new SparkConf //创建Sparkconf对象
		conf.setAppName("My First Spark App!") //设置应用程序名称,在程序运行的监控界面
				.setMaster("local") //此时程序在本地运行,不需要安装spark集群
		//.setMaster("spark://hadoop:7077") //此时程序在集群运行,需要安装spark集群,接下来打包后
		/**
		  * 第二步:创建sparkcontext对象,sparkcontext对象是spark所有对象的唯一入口
		  * 无论是采用scala,java,python,R等,都必须要有一个sparkcontext
		  * sparkcontext核心作用:初始化spark应用程序运行所需要的核心组件,包括DAGScheduler,TaskScheduler,还有SchedulerBackend
		  * 同时还会负责Spark程序往master注册程序等
		  * Sparkcontext是整个Spark应用程序中最为至关重要的一个对象
		  */
		val sc = new SparkContext(conf) //创建sparkcontext对象,通过传入sparkconf实例来定制SPark运行


		/**
		  * 第三步:根据具体的数据来源(HDFS,Hbase,LocalFS,DB,S3等)通过sparkcontext来创建rdd
		  * RDD的创建基本有三种方式:根据外部数据来源(例如HDFS),根据Scala集合,由其他的RDD操作产生
		  * 数据会被RDD划分成为一系列的Partitions,分配到每个Partition数据属于一个Task的处理范畴
		  */
		//val lines = sc.textFile("/opt/single/spark-compiled/README.md", 1) //读取本地文件,并设置为一个partition
		val lines = sc.textFile("src/test/resources/text.txt")
		//val lines = sc.textFile("/user/spark/wc/input/data") //读取HDFS文件,并切分成不同的partition

		/**
		  * 第四步:对初始的rdd进行Transformation级别的处理,例如map,filter等高阶函数的编程,来进行具体的数据计算
		  */
		//4.1:将每一行的字符串拆分成单个单词
		val words = lines.flatMap { line => line.split(" ") } //对每一行的字符串进行单词拆分并把所有行的拆分结果通过flat合并成为一个大的单词集合
		//4.2:在单词产分的基础上,将每个单词实例计数为1,也就是word=>(word,1)
		val pairs = words.map { word => (word, 1) }
		//4.3:在每个单词实例计数为1基础上,统计每个单词的文件出现的总次数
		val wordCounts = pairs.reduceByKey(_ + _) //对相同的key进行value的累加(包括local和reducer级别同时Reducer)
		wordCounts.collect.foreach(wordNumberPair => println(wordNumberPair._1 + " : " + wordNumberPair._2))
		sc.stop()
	}

	//简洁版
	def wc() = {
		val conf = new SparkConf().setMaster("local").setAppName("wc")
		val sc = new SparkContext(conf)
		sc.textFile("src/test/resources/text.txt")
				.flatMap(_.split(' '))
				.map((_, 1))
				.reduceByKey(_ + _).collect()
				//.foreach(println)
	}
}
