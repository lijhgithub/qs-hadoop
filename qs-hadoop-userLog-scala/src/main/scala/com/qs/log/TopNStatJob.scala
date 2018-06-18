package com.qs.log

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
  * 完成top n 的统计
  */
object TopNStatJob {


  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "E:\\hadoop")
    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)

    val spark = SparkSession.builder().appName("TopNStatJob")
      .master("local[2]").getOrCreate()

    var path : String = null
    if (args.length < 1)
      path = "file:///E:\\json\\clean\\"
    else
      path = args(0)

    val accessDF = spark.read.format("parquet").load(path)

//    accessDF.printSchema()
//    accessDF.show(false)

    import spark.implicits._
    //一：统计接口使用的top N



    //1）以DataFrame 的方式进行统计
    val interAccessTopN = accessDF
      .filter($"date" === "20180609" && $"interName" =!= "")
      .groupBy($"date",$"interName")
      .agg(count("interName").as("times"))//agg为聚合函数，跟在group by后
      .orderBy($"times".desc)

    interAccessTopN.show(false)


    //2) 以sql的方式进行统计
    accessDF.createOrReplaceTempView("userLog")
    val sqlTopN = spark.sql("select date,interName,count(interName) as times from userLog " +
      "where date = '20180609' and interName != '' group by date,interName order by times desc")

    sqlTopN.show(false)

    spark.stop()
  }


  /*

  +--------+-------------------+-----+
|date    |interName          |times|
+--------+-------------------+-----+
|20180609|cookieJoinRoom.html|1648 |
|20180609|joinViewUi.html    |1227 |
|20180609|beforeShowLink.html|6    |
|20180609|toLinkPage.html    |4    |
|20180609|joinRoom.html      |2    |
+--------+-------------------+-----+

+--------+-------------------+-----+
|date    |interName          |times|
+--------+-------------------+-----+
|20180609|cookieJoinRoom.html|1648 |
|20180609|joinViewUi.html    |1227 |
|20180609|beforeShowLink.html|6    |
|20180609|toLinkPage.html    |4    |
|20180609|joinRoom.html      |2    |
+--------+-------------------+-----+

   */

}