package akka_akka_streams.homework

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, Graph}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipN}

import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory


object homeworktemplate {
  implicit val system: ActorSystem = ActorSystem("fusion")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val logger: Logger = LoggerFactory
    .getLogger("homeworktemplate")
    .asInstanceOf[Logger]

  private val graph: Graph[ClosedShape.type, NotUsed] =
    GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val input = builder.add(Source(1 to 5))
      val x10 = builder.add(Flow[Int].map(x => x * 10))
      val x2 = builder.add(Flow[Int].map(x => x * 2))
      val x3 = builder.add(Flow[Int].map(x => x * 3))
      val sum = builder.add(Flow[Any].map(x => x.asInstanceOf[Vector[Int]].sum))
      val output = builder.add(Sink.foreach[Any](any => logger.info(any.toString)))
      val broadcast = builder.add(Broadcast[Int](3))
      val zip = builder.add(ZipN[Int](3))

      input ~> broadcast
      broadcast.out(0) ~> x10 ~> zip.in(0)
      broadcast.out(1) ~> x2 ~> zip.in(1)
      broadcast.out(2) ~> x3 ~> zip.in(2)
      zip.out ~> sum
      sum ~> output

      ClosedShape
    }

  def println2(x: Vector[Int]): Unit = {
    val sum = x.sum
    Console.println(sum)
  }

  def main(args: Array[String]): Unit = {
    RunnableGraph.fromGraph(graph).run()

  }
}